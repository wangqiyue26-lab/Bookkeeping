package com.local.bookkeeping.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.data.repository.LedgerRepository
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
  assertEquals(listOf("Checking", "Savings", "Cash"), db.dao().accountSnapshot().map { it.name })
  assertTrue(db.dao().accountSnapshot().all { it.openingBalanceCny == 0L })
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
  assertEquals(2, db.dao().accountSnapshot().size)
 }
}
