package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.PuestoRanking
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// GET api/sesiones/ranking: los mejores récords de una dificultad.
// Los parámetros van con @Query y el mismo nombre que en la API (REGLA-0489-072)
interface ApiGetRanking
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@Header envía el token en la cabecera Authorization que pide la API)
    @GET("sesiones/ranking")
    fun getRanking(@Header("Authorization") token: String,
                   @Query("modoId") modoId: Int,
                   @Query("dificultad") dificultad: String,
                   @Query("top") top: Int): Call<List<PuestoRanking>>
}
