package com.groundstudio.topohit.activities

import androidx.appcompat.app.AppCompatActivity
import com.groundstudio.topohit.util.Sonidos

// Todas las pantallas del juego heredan de esta clase (herencia: vídeo Kotlin09).
// Así la música de fondo suena en todo el juego y se para sola al salir de la app,
// sin repetir el mismo código en cada pantalla.
open class PantallaBase : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        Sonidos.iniciar(applicationContext)
        Sonidos.pantallaVisible()
    }

    override fun onPause() {
        super.onPause()
        Sonidos.pantallaOculta()
    }
}
