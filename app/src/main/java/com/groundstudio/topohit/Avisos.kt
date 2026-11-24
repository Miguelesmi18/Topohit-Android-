package com.groundstudio.topohit

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.groundstudio.topohit.activities.LoginActivity

// Mensajes al jugador. Los errores van con un Toast largo (REGLA-0489-017).
// Se llaman siempre desde runOnUiThread, porque tocan la pantalla (REGLA-0489-069).
object Avisos
{
    fun toast(context: Context, idTexto: Int)
    {
        Toast.makeText(context, context.getString(idTexto), Toast.LENGTH_LONG).show()
    }

    // Toast corto para las cosas que van bien («¡Avatar cambiado!»)
    fun bien(context: Context, idTexto: Int)
    {
        Toast.makeText(context, context.getString(idTexto), Toast.LENGTH_SHORT).show()
    }

    // La API no responde (está apagada, no hay wifi...)
    fun errorConexion(context: Context)
    {
        toast(context, R.string.error_conexion)
    }

    // La API ha respondido con un error: el mensaje depende del código HTTP.
    // Con 401 la sesión ya no vale (el token caduca): se borra y se vuelve al login.
    fun errorApi(activity: AppCompatActivity, codigo: Int)
    {
        var id = R.string.error_desconocido
        when(codigo)
        {
            400 -> id = R.string.error_datos
            401 -> id = R.string.error_sesion
            403 -> id = R.string.error_bloqueada
            404 -> id = R.string.error_no_encontrado
        }
        toast(activity, id)

        if(codigo == 401)
        {
            volverAlLogin(activity)
        }
    }

    // Cierra la sesión y abre el login sin dejar pantallas detrás
    fun volverAlLogin(activity: AppCompatActivity)
    {
        GestorSesion.borrar(activity)
        val intent = Intent(activity, LoginActivity::class.java)
        // Esta parte es de información externa que no sale en los vídeos
        // (las flags vacían la pila de pantallas para que «atrás» no vuelva a una pantalla sin sesión)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}
