package com.local.bookkeeping.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Money {
 const val MAX_CENTS = 100_000_000_000_000L
 fun cents(input: String, allowZero: Boolean = false): Long {
  require(input.matches(Regex("[0-9]{1,12}(\\.[0-9]{1,2})?"))) { "Enter an amount with up to two decimal places." }
  val value = input.toBigDecimal().movePointRight(2).longValueExact()
  require(value in (if (allowZero) 0L else 1L)..MAX_CENTS) { "Enter a valid positive amount." }
  return value
 }
 fun validRate(input: String): BigDecimal {
  require(input.matches(Regex("[0-9]{1,4}(\\.[0-9]{1,8})?"))) { "Enter a positive rate with up to 8 decimal places." }
  return input.toBigDecimal().also { require(it > BigDecimal.ZERO && it <= BigDecimal("1000")) { "Rate must be greater than zero and no more than 1000." } }
 }
 fun usd(cnyCents: Long, rate: BigDecimal): BigDecimal {
  require(rate.signum() > 0)
  return BigDecimal.valueOf(cnyCents, 2).multiply(rate).setScale(2, RoundingMode.HALF_UP)
 }
 fun usdCents(cnyCents: Long, rate: String) = usd(cnyCents, validRate(rate)).movePointRight(2).longValueExact()
 fun display(cents: Long, usd: Boolean = true): String =
  (if (usd) "$" else "¥") + NumberFormat.getNumberInstance(Locale.US).apply {
   minimumFractionDigits = 2; maximumFractionDigits = 2
  }.format(BigDecimal.valueOf(cents, 2))
 fun input(cents: Long) = BigDecimal.valueOf(cents, 2).toPlainString()
 fun balance(opening: Long, entries: List<Pair<String, Long>>): Long =
  entries.fold(opening) { total, (type, amount) -> Math.addExact(total, if (type == "Income") amount else -amount) }
 fun fresh(fetchedAt: Long, now: Long) = now >= fetchedAt && now - fetchedAt < 6 * 60 * 60 * 1000L
}

object Categories {
 val expense = listOf("Food", "Shopping", "Transportation", "Housing", "Utilities", "Healthcare", "Entertainment", "Travel", "Education", "Family", "Other")
 val income = listOf("Salary", "Bonus", "Investment", "Refund", "Transfer", "Other")
 fun forType(type: String) = if (type == "Income") income else expense
 val accountTypes = listOf("Checking", "Savings", "Cash", "Other")
}
