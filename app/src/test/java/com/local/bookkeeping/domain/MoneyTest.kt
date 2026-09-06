package com.local.bookkeeping.domain

import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class MoneyTest {
 @Test fun convertsCnyToUsd() { assertEquals(BigDecimal("13.90"), Money.usd(10000, BigDecimal("0.139"))) }
 @Test fun roundsHalfUp() { assertEquals(BigDecimal("0.01"), Money.usd(1, BigDecimal("0.5"))) }
 @Test fun incomeIncreasesBalance() { assertEquals(12000L, Money.balance(10000, listOf("Income" to 2000L))) }
 @Test fun expenseDecreasesBalance() { assertEquals(8000L, Money.balance(10000, listOf("Expense" to 2000L))) }
 @Test fun openingBalance() { assertEquals(10000L, Money.balance(10000, emptyList())) }
 @Test fun multipleAndCurrentConversion() {
  val cny = Money.balance(1000000, listOf("Income" to 200000L, "Expense" to 50000L))
  assertEquals(1150000L, cny)
  assertEquals(BigDecimal("1598.50"), Money.usd(cny, BigDecimal("0.139")))
 }
 @Test fun historyAndEditUseOriginalRate() {
  val old = Money.usdCents(100000, "0.139")
  assertEquals(13900L, old)
  assertEquals(14200L, Money.usdCents(100000, "0.142"))
  assertEquals(27800L, Money.usdCents(200000, "0.139"))
  assertEquals(13900L, old)
 }
 @Test fun cachedRateExpiresAfterSixHours() {
  assertTrue(Money.fresh(1000, 2000))
  assertFalse(Money.fresh(1000, 1000 + 21600000))
  assertFalse(Money.fresh(2000, 1000))
 }
 @Test(expected = IllegalArgumentException::class) fun rejectZero() { Money.cents("0") }
 @Test(expected = IllegalArgumentException::class) fun rejectExtraDecimals() { Money.cents("12.001") }
 @Test(expected = IllegalArgumentException::class) fun rejectNegative() { Money.cents("-1") }
 @Test fun decimalInputIsExact() { assertEquals(10025L, Money.cents("100.25")) }
}
