package com.local.bookkeeping.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.data.repository.LedgerRepository
import com.local.bookkeeping.domain.Money
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LedgerDaoTest {
 private lateinit var db: LedgerDatabase
 @Before fun setup() {
  db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), LedgerDatabase::class.java).allowMainThreadQueries().build()
 }
 @After fun close() { db.close() }
 @Test fun defaultsAreIdempotentAndZero() = runTest {
  val repo = LedgerRepository(db)
  repo.initialize(); repo.initialize()
  val accounts = db.dao().accountSnapshot()
  assertEquals(
   listOf("中国银行", "中国工商银行", "北京银行", "中国银行全币种 Visa 白金卡"),
   accounts.map { it.name }
  )
  assertTrue(accounts.all { it.openingBalanceCny == 0L })
  assertTrue(accounts.all { it.creditCardDebtUsdCents == 0L })
  assertEquals("Credit Card", accounts.last().accountType)
 }
 @Test fun legacyUntouchedDefaultsAreUpgraded() = runTest {
  val dao = db.dao()
  dao.insertAccount(Account(name = "Checking", accountType = "Checking", sortOrder = 0))
  dao.insertAccount(Account(name = "Savings", accountType = "Savings", sortOrder = 1))
  dao.insertAccount(Account(name = "Cash", accountType = "Cash", sortOrder = 2))
  LedgerRepository(db).initialize()
  val accounts = dao.accountSnapshot()
  assertEquals(
   listOf("中国银行", "中国工商银行", "北京银行", "中国银行全币种 Visa 白金卡"),
   accounts.map { it.name }
  )
  assertEquals("Credit Card", accounts.last().accountType)
 }
 @Test fun existingVisaAccountIsPromotedToCreditCard() = runTest {
  val dao = db.dao()
  dao.insertAccount(Account(name = "中国银行全币种 Visa 白金卡", accountType = "Other"))
  LedgerRepository(db).initialize()
  assertEquals("Credit Card", dao.accountSnapshot().single().accountType)
 }
 @Test fun creditCardDebtProtectsAccountFromDeletion() = runTest {
  val repo = LedgerRepository(db)
  repo.initialize()
  val card = db.dao().accountSnapshot().last()
  repo.saveAccount(card.copy(creditCardDebtUsdCents = 12500L))
  assertEquals(0, db.dao().deleteEmptyAccount(card.id))
 }
 @Test fun transferCreatesBalancedPair() = runTest {
  val repo = LedgerRepository(db)
  repo.initialize()
  val accounts = db.dao().accountSnapshot().filter { it.accountType != "Credit Card" }
  val from = accounts[0]
  val to = accounts[1]
  repo.saveAccount(from.copy(openingBalanceCny = 100000L))
  repo.saveTransfer(from.id, to.id, 25000L, "0.139", "2026-09-07", "2026-09-07", "")
  val entries = db.dao().transactionSnapshot()
  assertEquals(2, entries.size)
  assertTrue(entries.all { it.category == "Transfer" })
  val fromBalance = Money.balance(100000L, entries.filter { it.accountId == from.id }.map { it.type to it.amountCny })
  val toBalance = Money.balance(0L, entries.filter { it.accountId == to.id }.map { it.type to it.amountCny })
  assertEquals(75000L, fromBalance)
  assertEquals(25000L, toBalance)
  assertEquals(100000L, fromBalance + toBalance)
 }
 @Test fun insertEditDeleteAndProtectAccount() = runTest {
  val repo = LedgerRepository(db)
  repo.initialize()
  val account = db.dao().accountSnapshot().first()
  val entry = LedgerTransaction(accountId = account.id, type = "Expense", amountCny = 10000, amountUsd = 1390, exchangeRate = "0.139", exchangeRateDate = "2026-09-06", category = "Food", transactionDate = "2026-09-06")
  repo.saveTransaction(entry)
  val saved = db.dao().transactionSnapshot().single()
  assertEquals(1390L, saved.amountUsd)
  assertEquals(0, db.dao().deleteEmptyAccount(account.id))
  repo.saveTransaction(saved.copy(amountCny = 20000))
  assertEquals(2780L, db.dao().transactionSnapshot().single().amountUsd)
  assertEquals("0.139", db.dao().transactionSnapshot().single().exchangeRate)
  repo.deleteTransaction(saved.id)
  assertTrue(db.dao().transactionSnapshot().isEmpty())
  repo.deleteAccount(account.id)
  assertEquals(3, db.dao().accountSnapshot().size)
 }
}
