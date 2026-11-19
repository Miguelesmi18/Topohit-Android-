package com.groundstudio.topohit.api

import com.google.gson.Gson
import com.groundstudio.topohit.data_model.MensajeError
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Cliente Retrofit único para toda la app (vídeo 35).
<<<<<<< HEAD
object RetrofitClient {

    // Dirección de la API. El jugador no la ve ni la puede cambiar.
    // 10.0.2.2 es "el ordenador" visto desde el emulador de Android Studio.
    // Para probar en una tablet de verdad, cambiad aquí la IP por la del PC donde corre la API
    // (por ejemplo "http://192.168.1.20:5110/"): la tablet y el PC tienen que estar en la misma wifi.
    const val URL = "http://10.0.2.2:5110/"

    private var api: ApiTopohit? = null

    // Devuelve la API (la primera vez crea el cliente)
    fun api(): ApiTopohit {
        if (api == null) {
            val retrofit = Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            api = retrofit.create(ApiTopohit::class.java)
=======
// La dirección se puede cambiar en Ajustes (por ejemplo, la IP del ordenador donde corre la API).
object RetrofitClient {

    // 10.0.2.2 es "el ordenador" visto desde el emulador de Android Studio.
    // En una tablet de verdad hay que poner la IP del PC, p. ej. http://192.168.1.20:5110/
    const val URL_POR_DEFECTO = "http://10.0.2.2:5110/"

    var url = URL_POR_DEFECTO
    private var api: ApiTopohit? = null
    private var urlDeLaApi = ""

    // Devuelve la API. Si la dirección ha cambiado, vuelve a crear el cliente.
    fun api(): ApiTopohit {
        if (api == null || urlDeLaApi != url) {
            val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            api = retrofit.create(ApiTopohit::class.java)
            urlDeLaApi = url
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        }
        return api!!
    }

    // Saca el texto de { "mensaje": "..." } cuando la API responde con un error (400, 401, 409...)
    fun mensajeDeError(response: Response<*>): String {
        var texto = "Error " + response.code()
        try {
            val cuerpo = response.errorBody()?.string()
            if (cuerpo != null) {
                val error = Gson().fromJson(cuerpo, MensajeError::class.java)
                if (error?.mensaje != null) {
                    texto = error.mensaje
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return texto
    }
}
