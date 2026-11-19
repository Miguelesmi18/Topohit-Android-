package com.groundstudio.topohit.api

import com.groundstudio.topohit.data_model.DificultadContrarreloj
import com.groundstudio.topohit.data_model.DificultadInfinito
import com.groundstudio.topohit.data_model.AvatarPeticion
import com.groundstudio.topohit.data_model.AvatarRespuesta
import com.groundstudio.topohit.data_model.LoginPeticion
import com.groundstudio.topohit.data_model.LoginRespuesta
import com.groundstudio.topohit.data_model.NivelHistoria
import com.groundstudio.topohit.data_model.Progreso
import com.groundstudio.topohit.data_model.RegistroPeticion
import com.groundstudio.topohit.data_model.Sesion
import com.groundstudio.topohit.data_model.SesionCreada
import com.groundstudio.topohit.data_model.SesionNueva
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

// Las llamadas a la API de Topohit (vídeos 35-38).
// [No visto en clase] @Body: nuestra API recibe un JSON en el cuerpo de la petición,
//   así que en vez de @Query pasamos el objeto entero y GSON lo convierte a JSON.
// [No visto en clase] @Header: las rutas protegidas piden "Authorization: Bearer <token>".
interface ApiTopohit {

    @POST("api/auth/login")
    fun login(@Body peticion: LoginPeticion): Call<LoginRespuesta>

    // CAMBIO 19/11: el registro también envía el avatar elegido
    @POST("api/auth/registro")
    fun registro(@Body peticion: RegistroPeticion): Call<LoginRespuesta>

    // CAMBIO 19/11: el jugador cambia su avatar (se guarda en USUARIO.avatar_id)
    // [No visto en clase] @PUT: igual que @POST, pero es el verbo que usa la API para modificar
    @PUT("api/auth/avatar")
    fun cambiarAvatar(@Header("Authorization") token: String, @Body peticion: AvatarPeticion): Call<AvatarRespuesta>

    @GET("api/modos/historia")
    fun getNiveles(): Call<List<NivelHistoria>>

    @GET("api/modos/contrarreloj")
    fun getContrarreloj(): Call<List<DificultadContrarreloj>>

    @GET("api/modos/infinito")
    fun getInfinito(): Call<List<DificultadInfinito>>

    @POST("api/sesiones")
    fun guardarPartida(@Header("Authorization") token: String, @Body sesion: SesionNueva): Call<SesionCreada>

    @GET("api/sesiones/progreso")
    fun getProgreso(@Header("Authorization") token: String): Call<Progreso>

    @GET("api/sesiones/mias")
    fun getMisPartidas(@Header("Authorization") token: String): Call<List<Sesion>>
}
