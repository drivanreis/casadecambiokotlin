package com.betrybe.currencyview.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenApiService {
    private const val API_KEY = "20EyGYInJopKHhX7HzFTsiWv41CbMtdF"

    // Interceptor para adicionar o API Key no cabeçalho de cada requisição
    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder: Request.Builder = original.newBuilder()
            .header("apikey", API_KEY)
        val request: Request = requestBuilder.build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.apilayer.com/exchangerates_data/")
            .client(client)  // Inclui o client com o interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
