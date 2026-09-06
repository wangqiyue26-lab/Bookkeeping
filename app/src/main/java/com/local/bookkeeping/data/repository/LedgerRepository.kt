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
  if (dao.accountSnapshot().isEmpty()) defaults()
 }
 private suspend fun defaults() {
  listOf("Checking", "Savings", "Cash").forEachIndexed { i, name ->
   dao.insertAccount(Account(name = name, accountType = name, sortOrder = i))
  }
 }
 suspend fun saveAccount(account: Account) {
  require(account.name.isNotBlank() && account.name.length <= 60) { "Enter an account name (up to 60 characters)." }
  require(account.accountType in Categories.accountTypes)
  require(account.openingBalanceCny in 0..Money.MAX_CENTS)
  if (account.id == 0L) dao.insertAccount(account) else dao.updateAccount(account)
 }
 suspend fun deleteAccount(id: Long) {
  require(dao.deleteEmptyAccount(id) == 1) { "Only accounts with no transactions and zero opening balance can be deleted." }
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
