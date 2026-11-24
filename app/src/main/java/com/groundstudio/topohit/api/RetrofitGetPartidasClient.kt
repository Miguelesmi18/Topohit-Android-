package com.groundstudio.topohit.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Cliente Retrofit para ApiGetPartidas (REGLA-0489-068: un object por llamada)
object RetrofitGetPartidasClient
{
    fun getInstance(): Retrofit
    {
        return Retrofit.Builder().baseUrl(ApiGetPartidas.BASE_URL).
                addConverterFactory(GsonConverterFactory.create()).
                build()
    }
}
