package com.local.bookkeeping.data.repository

import androidx.room.withTransaction
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.domain.*
import java.time.LocalDate

class LedgerRepository(val db: LedgerDatabase) {
 val dao = db.dao()
 val accounts = dao.accounts()
 val transactions = dao.transactions()
 val rate = dao.rate()

 suspend fun initialize() = db.withTransaction {
  val existing = dao.accountSnapshot()
  when {
   existing.isEmpty() -> defaults()
   shouldUpgradeLegacyDefaults(existing) -> upgradeLegacyDefaults(existing)
  }
  ensureVisaCreditCardType()
 }

 private val defaultAccountSpecs = listOf(
  Triple("中国银行", "Checking", 0),
  Triple("中国工商银行", "Savings", 1),
  Triple("北京银行", "Checking", 2),
  Triple("中国银行全币种 Visa 白金卡", "Credit Card", 3)
 )

 private suspend fun defaults() {
  defaultAccountSpecs.forEach { (name, type, order) ->
   dao.insertAccount(Account(name = name, accountType = type, sortOrder = order))
  }
 }

 private suspend fun shouldUpgradeLegacyDefaults(existing: List<Account>): Boolean {
  if (existing.size != 3 || dao.transactionSnapshot().isNotEmpty()) return false
  if (existing.any { it.openingBalanceCny != 0L || it.isArchived }) return false
  return existing.sortedBy { it.sortOrder }.map { it.name } == listOf("Checking", "Savings", "Cash")
 }

 private suspend fun upgradeLegacyDefaults(existing: List<Account>) {
  val sorted = existing.sortedBy { it.sortOrder }
  defaultAccountSpecs.take(3).forEachIndexed { index, (name, type, order) ->
   dao.updateAccount(sorted[index].copy(name = name, accountType = type, sortOrder = order))
  }
  val (name, type, order) = defaultAccountSpecs[3]
  dao.insertAccount(Account(name = name, accountType = type, sortOrder = order))
 }

 private suspend fun ensureVisaCreditCardType() {
  dao.accountSnapshot().forEach { account ->
   val visaPlatinum = account.name.contains("全币种", ignoreCase = true) ||
    (account.name.contains("visa", ignoreCase = true) && account.name.contains("中国银行"))
   if (visaPlatinum && account.accountType != "Credit Card") {
    dao.updateAccount(account.copy(accountType = "Credit Card"))
   }
  }
 }

 suspend fun saveAccount(account: Account) {
  require(account.name.isNotBlank() && account.name.length <= 60) { "Enter an account name (up to 60 characters)." }
  require(account.accountType in Categories.accountTypes)
  require(account.openingBalanceCny in 0..Money.MAX_CENTS)
  require(account.creditCardDebtUsdCents in 0..Money.MAX_CENTS) { "Enter a valid USD debt amount." }
  if (account.id == 0L) dao.insertAccount(account) else dao.updateAccount(account)
 }
 suspend fun deleteAccount(id: Long) {
  require(dao.deleteEmptyAccount(id) == 1) { "Only accounts with no transactions, zero opening balance, and zero credit-card debt can be deleted." }
 }
 suspend fun saveTransaction(entry: LedgerTransaction) = db.withTransaction {
  require(entry.type in listOf("Income", "Expense"))
  require(entry.amountCny in 1..Money.MAX_CENTS) { "Enter a positive amount." }
  require(entry.category in Categories.forType(entry.type))
  require(entry.note.length <= 2000) { "Note must be at most 2000 characters." }
  LocalDate.parse(entry.transactionDate)
  LocalDate.parse(entry.exchangeRateDate)
  require(dao.accountSnapshot().any { it.id == entry.accountId && !it.isArchived }) { "Choose an active account." }
  val checked = entry.copy(amountUsd = Money.usdCents(entry.amountCny, entry.exchangeRate), updatedAt = System.currentTimeMillis())
  if (checked.id == 0L) dao.insertTransaction(checked) else dao.updateTransaction(checked)
 }
 suspend fun deleteTransaction(id: Long) = dao.deleteTransaction(id)
 suspend fun reset() = db.withTransaction {
  dao.clearTransactions(); dao.clearAccounts(); defaults()
 }
}
