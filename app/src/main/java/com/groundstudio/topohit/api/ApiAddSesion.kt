package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.SesionCreada
import com.groundstudio.topohit.datamodel.SesionNueva
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// POST api/sesiones: guarda una partida terminada (o abandonada)
interface ApiAddSesion
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@Header envía el token y @Body el JSON en el cuerpo, como pide la API)
    @POST("sesiones")
    fun addSesion(@Header("Authorization") token: String,
                  @Body sesion: SesionNueva): Call<SesionCreada>
}
