package com.local.bookkeeping.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET

interface ExchangeRateApi {
 @GET("v2/rate/CNY/USD") suspend fun rate(): ResponseBody
}
