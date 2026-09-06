package com.local.bookkeeping.data.repository

import androidx.room.withTransaction
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable
data class LedgerBackup(
 val schemaVersion: Int = 1,
 val accounts: List<Account>,
 val transactions: List<LedgerTransaction>,
 val settings: LedgerSettings,
 val exchangeRate: ExchangeRateCache? = null
)

object BackupCodec {
 val json = Json { prettyPrint = true; encodeDefaults = true }
 fun decode(text: String): LedgerBackup = json.decodeFromString<LedgerBackup>(text).also { validate(it) }
 fun validate(b: LedgerBackup) {
  require(b.schemaVersion == 1) { "Unsupported backup version." }
  require(b.accounts.size <= 10000 && b.transactions.size <= 100000) { "Backup is too large." }
  val ids = b.accounts.map { it.id }.toSet()
  require(ids.size == b.accounts.size && ids.all { it > 0 })
  require(b.transactions.map { it.id }.toSet().size == b.transactions.size)
  b.accounts.forEach {
   require(it.name.isNotBlank() && it.name.length <= 60 && it.accountType in Categories.accountTypes)
   require(it.openingBalanceCny in 0..Money.MAX_CENTS)
  }
  b.transactions.forEach {
   require(it.id > 0 && it.accountId in ids && it.type in listOf("Income", "Expense"))
   require(it.amountCny in 1..Money.MAX_CENTS && it.category in Categories.forType(it.type) && it.note.length <= 2000)
   require(it.amountUsd == Money.usdCents(it.amountCny, it.exchangeRate))
   LocalDate.parse(it.transactionDate); LocalDate.parse(it.exchangeRateDate)
  }
  require(b.settings.themeMode in listOf("System", "Light", "Dark"))
  if (b.settings.manualRate.isNotBlank()) Money.validRate(b.settings.manualRate)
  if (b.settings.manualRateEnabled) { Money.validRate(b.settings.manualRate); LocalDate.parse(b.settings.manualRateDate) }
  b.exchangeRate?.let {
   require(it.id == 1 && it.baseCurrency == "CNY" && it.quoteCurrency == "USD" && it.lastFetchedAt >= 0)
   Money.validRate(it.rate); LocalDate.parse(it.rateDate)
  }
 }
 fun csv(b: LedgerBackup): String {
  fun cell(value: String): String {
   val safe = if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'" + value else value
   return "\"" + safe.replace("\"", "\"\"") + "\""
  }
  val header = "Date,Type,Account,Category,Amount CNY,Exchange Rate,Amount USD,Note\r\n"
  return header + b.transactions.joinToString("\r\n") { t ->
   listOf(t.transactionDate, t.type, b.accounts.find { it.id == t.accountId }?.name ?: "", t.category,
    Money.input(t.amountCny), t.exchangeRate, Money.input(t.amountUsd), t.note).joinToString(",") { cell(it) }
  }
 }
}

class BackupRepository(private val ledger: LedgerRepository, private val settings: SettingsRepository) {
 suspend fun snapshot(): LedgerBackup {
  val prefs = settings.settings.first()
  return ledger.db.withTransaction {
   LedgerBackup(accounts = ledger.dao.accountSnapshot(), transactions = ledger.dao.transactionSnapshot(), settings = prefs, exchangeRate = ledger.dao.rateSnapshot())
  }
 }
 suspend fun restore(b: LedgerBackup) {
  BackupCodec.validate(b)
  val previous = settings.settings.first()
  try {
   ledger.db.withTransaction {
    ledger.dao.clearTransactions(); ledger.dao.clearAccounts(); ledger.dao.clearRates()
    b.accounts.forEach { ledger.dao.insertAccount(it) }
    b.transactions.forEach { ledger.dao.insertTransaction(it) }
    b.exchangeRate?.let { ledger.dao.saveRate(it) }
    settings.save(b.settings)
   }
  } catch (e: Exception) {
   settings.save(previous)
   throw e
  }
  ledger.initialize()
 }
 suspend fun exportJson() = BackupCodec.json.encodeToString(snapshot())
 suspend fun exportCsv() = BackupCodec.csv(snapshot())
}
