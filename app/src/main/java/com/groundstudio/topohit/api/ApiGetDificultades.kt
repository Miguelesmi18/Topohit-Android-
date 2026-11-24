package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.EstadoDificultad
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

// GET api/sesiones/dificultades: las 3 dificultades de Infinito del jugador,
// abiertas o con candado (por puntos o porque el tutor las ha bloqueado)
interface ApiGetDificultades
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@Header envía el token en la cabecera Authorization que pide la API)
    @GET("sesiones/dificultades")
    fun getDificultades(@Header("Authorization") token: String): Call<List<EstadoDificultad>>
}
