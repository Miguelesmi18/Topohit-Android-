package com.groundstudio.topohit.api

import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.datamodel.AvatarPeticion
import com.groundstudio.topohit.datamodel.AvatarRespuesta
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT

// PUT api/auth/avatar: el jugador cambia su avatar
interface ApiSetAvatar
{
    companion object
    {
        val BASE_URL = SharedData.PROJECT_ROOT
    }

    // Esta parte es de información externa que no sale en los vídeos
    // (@PUT es el verbo que usa la API para modificar; @Header lleva el token y @Body el JSON)
    @PUT("auth/avatar")
    fun setAvatar(@Header("Authorization") token: String,
                  @Body peticion: AvatarPeticion): Call<AvatarRespuesta>
}
