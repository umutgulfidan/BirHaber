package com.example.birhaberdeneme

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitInstance {
    private const val BASE_URL = "https://newsapi.org/"
    const val API_KEY = "40e5d20fc9ab400ab2093b5356f5d140"
    private fun getInstance() : Retrofit{
        return Retrofit.Builder()
            .baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    }

    val newsApi : NewsApi = getInstance().create(NewsApi::class.java)

}