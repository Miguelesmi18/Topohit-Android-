package com.groundstudio.topohit.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.Sonidos

// Todas las pantallas del juego heredan de esta clase (herencia, REGLA-0489-095 y 096).
// Así los ajustes se cargan y la música suena en todo el juego (y se para al salir de la app)
// sin repetir el mismo código en cada pantalla.
open class PantallaBase : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        GestorAjustes.cargar(applicationContext)
        Sonidos.iniciar(applicationContext)
    }

    override fun onResume()
    {
        super.onResume()
        Sonidos.pantallaVisible()
    }

    // ¿Sigue abierta la pantalla? Las respuestas de la API llegan desde una corrutina y pueden
    // llegar cuando el niño ya ha salido: entonces no se toca la pantalla.
    // Esta parte es de información externa que no sale en los vídeos
    // (isFinishing e isDestroyed son propiedades de Activity que no se ven en clase)
    fun pantallaActiva(): Boolean = !isFinishing && !isDestroyed

    override fun onPause()
    {
        super.onPause()
        Sonidos.pantallaOculta()
    }
}
