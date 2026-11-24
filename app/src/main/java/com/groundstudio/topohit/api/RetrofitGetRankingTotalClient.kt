package com.groundstudio.topohit.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Cliente Retrofit para ApiGetRankingTotal (REGLA-0489-068: un object por llamada)
object RetrofitGetRankingTotalClient
{
    fun getInstance(): Retrofit
    {
        return Retrofit.Builder().baseUrl(ApiGetRankingTotal.BASE_URL).
                addConverterFactory(GsonConverterFactory.create()).
                build()
    }
}
