package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.LoginPeticion
import com.groundstudio.topohit.datamodel.LoginRespuesta
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// POST api/auth/login: entrar con usuario y hash de la contraseña
interface ApiLogin
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@Body: la API recibe un JSON en el cuerpo, no parámetros en la URL)
    @POST("auth/login")
    fun login(@Body peticion: LoginPeticion): Call<LoginRespuesta>
}
