package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.PuestoRanking
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// GET api/sesiones/ranking/total: los jugadores con más puntos sumando su récord de cada dificultad
interface ApiGetRankingTotal
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@Header envía el token en la cabecera Authorization que pide la API)
    @GET("sesiones/ranking/total")
    fun getRankingTotal(@Header("Authorization") token: String,
                        @Query("top") top: Int): Call<List<PuestoRanking>>
}
