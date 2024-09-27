package com.betrybe.currencyview.data.api

import com.betrybe.currencyview.data.models.CurrencyRateResponse
import com.betrybe.currencyview.data.models.CurrencySymbolResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface ApiService {
    @GET("symbols")
    suspend fun getSymbols(): Response<CurrencySymbolResponse>

    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") base: String
    ): Response<CurrencyRateResponse>
}