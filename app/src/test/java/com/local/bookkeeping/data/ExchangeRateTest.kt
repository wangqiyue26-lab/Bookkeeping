package com.local.bookkeeping.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.data.network.ExchangeRateApi
import com.local.bookkeeping.data.repository.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ExchangeRateTest {
 private lateinit var db: LedgerDatabase
 private lateinit var server: MockWebServer
 private lateinit var prefs: SettingsRepository
 private lateinit var repo: ExchangeRateRepository
 @Before fun setup() = runBlocking {
  val context = ApplicationProvider.getApplicationContext<Context>()
  db = Room.inMemoryDatabaseBuilder(context, LedgerDatabase::class.java).allowMainThreadQueries().build()
  prefs = SettingsRepository(context); prefs.save(LedgerSettings())
  server = MockWebServer(); server.start()
  val api = Retrofit.Builder().baseUrl(server.url("/")).build().create(ExchangeRateApi::class.java)
  repo = ExchangeRateRepository(db.dao(), prefs, api)
 }
 @After fun close() { server.shutdown(); db.close() }
 @Test fun fetchAndReuseFreshCache() = runBlocking {
  server.enqueue(MockResponse().setBody("{\"date\":\"2026-09-06\",\"base\":\"CNY\",\"quote\":\"USD\",\"rate\":0.139}"))
  repo.refresh()
  assertEquals("0.139", db.dao().rateSnapshot()?.rate)
  repo.refresh()
  assertEquals(1, server.requestCount)
 }
 @Test fun failedRefreshPreservesCachedRate() = runBlocking {
  val cached = ExchangeRateCache(rate = "0.139", rateDate = "2026-09-05", lastFetchedAt = 1)
  db.dao().saveRate(cached)
  server.enqueue(MockResponse().setResponseCode(503))
  try { repo.refresh(true); fail("Expected HTTP failure") } catch (_: retrofit2.HttpException) {}
  assertEquals(cached, db.dao().rateSnapshot())
 }
 @Test fun invalidRateDoesNotOverwriteCache() = runBlocking {
  val cached = ExchangeRateCache(rate = "0.139", rateDate = "2026-09-05", lastFetchedAt = 1)
  db.dao().saveRate(cached)
  server.enqueue(MockResponse().setBody("{\"date\":\"2026-09-06\",\"base\":\"CNY\",\"quote\":\"USD\",\"rate\":0}"))
  try { repo.refresh(true); fail("Expected invalid rate") } catch (_: IllegalArgumentException) {}
  assertEquals(cached, db.dao().rateSnapshot())
 }
 @Test fun manualModeNeverCallsNetwork() = runBlocking {
  prefs.save(LedgerSettings(manualRate = "0.14", manualRateEnabled = true, manualRateDate = "2026-09-06"))
  repo.refresh(true)
  assertEquals(0, server.requestCount)
  assertNull(db.dao().rateSnapshot())
 }
}
