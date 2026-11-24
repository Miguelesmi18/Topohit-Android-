package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.Partida
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

// GET api/sesiones/mias: el historial de partidas del jugador
interface ApiGetPartidas
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@Header envía el token en la cabecera Authorization que pide la API)
    @GET("sesiones/mias")
    fun getPartidas(@Header("Authorization") token: String): Call<List<Partida>>
}
