package com.local.bookkeeping.data

import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.data.repository.*
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class BackupTest {
 private fun backup() = LedgerBackup(
  accounts = listOf(Account(id = 1, name = "Cash", accountType = "Cash")),
  transactions = listOf(LedgerTransaction(id = 1, accountId = 1, type = "Expense", amountCny = 10000, amountUsd = 1390,
   exchangeRate = "0.139", exchangeRateDate = "2026-09-06", category = "Food", transactionDate = "2026-09-06", note = "=SUM(A1)")),
  settings = LedgerSettings(manualRate = "0.139", manualRateEnabled = true, manualRateDate = "2026-09-06")
 )
 @Test fun roundTripPreservesHistory() {
  val source = backup()
  assertEquals(source, BackupCodec.decode(BackupCodec.json.encodeToString(source)))
 }
 @Test(expected = IllegalArgumentException::class) fun rejectUnsupportedSchema() { BackupCodec.validate(backup().copy(schemaVersion = 2)) }
 @Test(expected = IllegalArgumentException::class) fun rejectOrphanTransactions() { BackupCodec.validate(backup().copy(accounts = emptyList())) }
 @Test(expected = IllegalArgumentException::class) fun rejectAlteredUsdAmount() {
  val b = backup()
  BackupCodec.validate(b.copy(transactions = b.transactions.map { it.copy(amountUsd = 1400) }))
 }
 @Test fun csvEscapesSpreadsheetFormula() {
  assertTrue(BackupCodec.csv(backup()).contains("\"'=SUM(A1)\""))
 }
}
