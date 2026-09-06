package com.local.bookkeeping

import android.app.Application
import androidx.room.Room
import com.local.bookkeeping.data.database.LedgerDatabase
import com.local.bookkeeping.data.network.ExchangeRateApi
import com.local.bookkeeping.data.repository.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class LedgerApplication : Application() {
 val database by lazy { Room.databaseBuilder(this, LedgerDatabase::class.java, "dollar-ledger.db").build() }
 val settings by lazy { SettingsRepository(this) }
 val ledger by lazy { LedgerRepository(database) }
 val exchange by lazy {
  val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
   .readTimeout(15, TimeUnit.SECONDS).callTimeout(20, TimeUnit.SECONDS).build()
  val api = Retrofit.Builder().baseUrl("https://api.frankfurter.dev/").client(client).build().create(ExchangeRateApi::class.java)
  ExchangeRateRepository(database.dao(), settings, api)
 }
 val backup by lazy { BackupRepository(ledger, settings) }
}
