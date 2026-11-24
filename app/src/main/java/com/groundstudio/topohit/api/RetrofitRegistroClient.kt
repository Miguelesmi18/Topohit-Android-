package com.groundstudio.topohit.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Cliente Retrofit para ApiRegistro (REGLA-0489-068: un object por llamada)
object RetrofitRegistroClient
{
    fun getInstance(): Retrofit
    {
        return Retrofit.Builder().baseUrl(ApiRegistro.BASE_URL).
                addConverterFactory(GsonConverterFactory.create()).
                build()
    }
}
