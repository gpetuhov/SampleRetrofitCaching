package com.gpetuhov.android.samplemoshi.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface QuakeService {

    @GET("query")
    fun getQuakes(
            @Query("format") format: String,
            @Query("limit") limit: String
    ): Call<QuakeResult>
}
