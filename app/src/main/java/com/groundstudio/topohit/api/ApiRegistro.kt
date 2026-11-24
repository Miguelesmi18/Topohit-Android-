package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.LoginRespuesta
import com.groundstudio.topohit.datamodel.RegistroPeticion
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// POST api/auth/registro: crear cuenta (devuelve lo mismo que el login)
interface ApiRegistro
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@Body: la API recibe un JSON en el cuerpo, no parámetros en la URL)
    @POST("auth/registro")
    fun registro(@Body peticion: RegistroPeticion): Call<LoginRespuesta>
}
