package com.local.bookkeeping.domain

import java.math.BigDecimal
import java.math.RoundingMode

object Money {
 fun usd(cnyCents: Long, rate: BigDecimal): BigDecimal {
  require(rate.signum() > 0)
  return BigDecimal.valueOf(cnyCents, 2).multiply(rate).setScale(2, RoundingMode.HALF_UP)
 }
}
