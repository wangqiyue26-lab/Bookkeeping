package com.local.bookkeeping.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "accounts")
data class Account(
 @PrimaryKey(autoGenerate = true) val id: Long = 0,
 val name: String,
 val accountType: String,
 val openingBalanceCny: Long = 0,
 val createdAt: Long = System.currentTimeMillis(),
 val sortOrder: Int = 0,
 val isArchived: Boolean = false
)

@Serializable
@Entity(tableName = "transactions", foreignKeys = [
 ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.RESTRICT)
], indices = [Index("accountId"), Index("transactionDate")])
data class LedgerTransaction(
 @PrimaryKey(autoGenerate = true) val id: Long = 0,
 val accountId: Long,
 val type: String,
 val amountCny: Long,
 val amountUsd: Long,
 val exchangeRate: String,
 val exchangeRateDate: String,
 val category: String,
 val note: String = "",
 val transactionDate: String,
 val createdAt: Long = System.currentTimeMillis(),
 val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "exchange_rate_cache")
data class ExchangeRateCache(
 @PrimaryKey val id: Int = 1,
 val baseCurrency: String = "CNY",
 val quoteCurrency: String = "USD",
 val rate: String,
 val rateDate: String,
 val lastFetchedAt: Long,
 val source: String = "Frankfurter v2"
)

@Dao
interface LedgerDao {
 @Query("SELECT * FROM accounts ORDER BY sortOrder, id") fun accounts(): Flow<List<Account>>
 @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC") fun transactions(): Flow<List<LedgerTransaction>>
 @Query("SELECT * FROM exchange_rate_cache WHERE id = 1") fun rate(): Flow<ExchangeRateCache?>
 @Query("SELECT * FROM accounts ORDER BY sortOrder, id") suspend fun accountSnapshot(): List<Account>
 @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC") suspend fun transactionSnapshot(): List<LedgerTransaction>
 @Query("SELECT * FROM exchange_rate_cache WHERE id = 1") suspend fun rateSnapshot(): ExchangeRateCache?
 @Insert suspend fun insertAccount(account: Account): Long
 @Update suspend fun updateAccount(account: Account)
 @Query("DELETE FROM accounts WHERE id = :id AND NOT EXISTS (SELECT 1 FROM transactions WHERE accountId = :id) AND openingBalanceCny = 0") suspend fun deleteEmptyAccount(id: Long): Int
 @Insert suspend fun insertTransaction(entry: LedgerTransaction): Long
 @Update suspend fun updateTransaction(entry: LedgerTransaction)
 @Query("DELETE FROM transactions WHERE id = :id") suspend fun deleteTransaction(id: Long)
 @Upsert suspend fun saveRate(rate: ExchangeRateCache)
 @Query("DELETE FROM transactions") suspend fun clearTransactions()
 @Query("DELETE FROM accounts") suspend fun clearAccounts()
 @Query("DELETE FROM exchange_rate_cache") suspend fun clearRates()
}

@Database(entities = [Account::class, LedgerTransaction::class, ExchangeRateCache::class], version = 1, exportSchema = false)
abstract class LedgerDatabase : RoomDatabase() {
 abstract fun dao(): LedgerDao
}
