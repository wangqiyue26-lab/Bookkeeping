package com.local.bookkeeping.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
 @Test fun convertsCnyToUsd() {
  assertEquals(BigDecimal("13.90"), Money.usd(10000, BigDecimal("0.139")))
 }
 @Test fun roundsHalfUp() {
  assertEquals(BigDecimal("0.01"), Money.usd(1, BigDecimal("0.5")))
 }
}
