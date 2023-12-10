package com.example.birhaberdeneme

import com.example.birhaberdeneme.RetrofitInstance.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApi {
    @GET("V2/top-headlines")
    suspend fun getAll(@Query("country") countryCode : String = "tr",
                       @Query("page") pageNumber:Int =1,
                       @Query("apiKey") apiKey : String = API_KEY) : Response<NewsResult>

    @GET("V2/everything")
    suspend fun searchByKeywords(@Query("language") language: String = "tr",
                                 @Query("page") pageNumber: Int = 1,
                                 @Query("q") keyWords:String? = null,
                                 @Query("sortBy") sortBy:String="publishedAt",
                                 @Query("searchIn") searchIn : String= "title,description",
                                 @Query("apiKey")apiKey: String = API_KEY) : Response<NewsResult>

}