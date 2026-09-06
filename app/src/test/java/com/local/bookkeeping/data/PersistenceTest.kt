package com.local.bookkeeping.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.data.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PersistenceTest {
 @Test fun dataSurvivesDatabaseReopenAndBackupRestore() = runBlocking {
  val context = ApplicationProvider.getApplicationContext<Context>()
  val name = "persistence-test.db"
  context.deleteDatabase(name)
  var db = Room.databaseBuilder(context, LedgerDatabase::class.java, name).allowMainThreadQueries().build()
  try {
   var ledger = LedgerRepository(db)
   ledger.initialize()
   val account = db.dao().accountSnapshot().first()
   ledger.saveAccount(account.copy(openingBalanceCny = 1000000))
   ledger.saveTransaction(LedgerTransaction(accountId = account.id, type = "Income", amountCny = 200000, amountUsd = 27800,
    exchangeRate = "0.139", exchangeRateDate = "2026-09-06", category = "Salary", transactionDate = "2026-09-06"))
   db.close()
   db = Room.databaseBuilder(context, LedgerDatabase::class.java, name).allowMainThreadQueries().build()
   ledger = LedgerRepository(db)
   assertEquals(1000000L, db.dao().accountSnapshot().first().openingBalanceCny)
   assertEquals(27800L, db.dao().transactionSnapshot().single().amountUsd)
   val settings = SettingsRepository(context)
   settings.save(LedgerSettings(manualRate = "0.142", manualRateEnabled = true, manualRateDate = "2026-09-06"))
   val backups = BackupRepository(ledger, settings)
   val saved = backups.snapshot()
   ledger.reset()
   assertTrue(db.dao().transactionSnapshot().isEmpty())
   backups.restore(saved)
   assertEquals(saved.transactions, db.dao().transactionSnapshot())
   assertEquals(saved.accounts, db.dao().accountSnapshot())
   assertEquals(saved.settings, settings.settings.first())
  } finally { db.close(); context.deleteDatabase(name) }
 }
}
