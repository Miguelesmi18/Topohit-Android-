package com.groundstudio.topohit.activities

import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
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

    // El título de la pantalla (encima del cielo del fondo) con el color y la sombra de la temática
    fun pintarTitulo(id: Int)
    {
        val tema = GestorAjustes.tema
        val titulo = findViewById<TextView>(id)
        titulo.setTextColor(ContextCompat.getColor(this, tema.colorTitulo))
        // Esta parte es de información externa que no sale en los vídeos
        // (setShadowLayer: cambiar desde el código la sombra que en el layout es shadowColor)
        titulo.setShadowLayer(8f, 0f, 0f, ContextCompat.getColor(this, tema.colorSombraTitulo))
    }

    override fun onPause()
    {
        super.onPause()
        Sonidos.pantallaOculta()
    }
}
