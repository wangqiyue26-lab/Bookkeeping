package com.local.bookkeeping.data.repository

import com.local.bookkeeping.data.database.*
import com.local.bookkeeping.data.network.ExchangeRateApi
import com.local.bookkeeping.domain.Money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.math.RoundingMode

class ExchangeRateRepository(private val dao: LedgerDao, private val settings: SettingsRepository, private val api: ExchangeRateApi) {
 private val mutex = Mutex()
 suspend fun refresh(force: Boolean = false) = mutex.withLock {
  if (settings.settings.first().manualRateEnabled) return@withLock
  val now = System.currentTimeMillis()
  if (!force && dao.rateSnapshot()?.let { Money.fresh(it.lastFetchedAt, now) } == true) return@withLock
  val body = api.rate().use { it.string() }
  val json = Json.parseToJsonElement(body).jsonObject
  require(json["base"]?.jsonPrimitive?.content == "CNY" && json["quote"]?.jsonPrimitive?.content == "USD")
  val raw = json.getValue("rate").jsonPrimitive.content.toBigDecimal()
  val rate = raw.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
  Money.validRate(rate)
  val date = json.getValue("date").jsonPrimitive.content
  LocalDate.parse(date)
  dao.saveRate(ExchangeRateCache(rate = rate, rateDate = date, lastFetchedAt = now))
 }
}
