package com.groundstudio.topohit.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.Sonidos
import com.groundstudio.topohit.Tematicas
import com.groundstudio.topohit.datamodel.Tematica

// Ajustes: temática, música, efectos y cerrar sesión.
// Se puede abrir desde el login (sin sesión) o desde el menú.
class AjustesActivity : PantallaBase()
{
    private lateinit var btnAjustesTemaNormal: TextView
    private lateinit var btnAjustesTemaHalloween: TextView
    private lateinit var btnAjustesTemaNavidad: TextView
    private lateinit var btnAjustesMusica: TextView
    private lateinit var btnAjustesEfectos: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)

        btnAjustesTemaNormal    = findViewById<TextView>(R.id.BtnAjustesTemaNormal)
        btnAjustesTemaHalloween = findViewById<TextView>(R.id.BtnAjustesTemaHalloween)
        btnAjustesTemaNavidad   = findViewById<TextView>(R.id.BtnAjustesTemaNavidad)
        btnAjustesMusica        = findViewById<TextView>(R.id.BtnAjustesMusica)
        btnAjustesEfectos       = findViewById<TextView>(R.id.BtnAjustesEfectos)

        // Si todavía no ha entrado nadie (venimos del login) no hay sesión que cerrar
        if(GestorSesion.getSesion(this) == null)
        {
            findViewById<LinearLayout>(R.id.LayAjustesSesion).visibility = View.GONE
        }
        else
        {
            findViewById<TextView>(R.id.LblAjustesUsuario).text =
                getString(R.string.ajustes_usuario, GestorSesion.getUsername(this))
        }

        findViewById<TextView>(R.id.BtnAjustesAtras).setOnClickListener()
        {
            finish()
        }

        btnAjustesTemaNormal.setOnClickListener()
        {
            cambiarTema(Tematicas.NORMAL)
        }

        btnAjustesTemaHalloween.setOnClickListener()
        {
            cambiarTema(Tematicas.HALLOWEEN)
        }

        btnAjustesTemaNavidad.setOnClickListener()
        {
            cambiarTema(Tematicas.NAVIDAD)
        }

        // Música y efectos: cada toque los enciende o los apaga
        btnAjustesMusica.setOnClickListener()
        {
            GestorAjustes.musica = !GestorAjustes.musica
            if(GestorAjustes.musica)
            {
                Sonidos.sonarMusica()
            }
            else
            {
                Sonidos.pausarMusica()
            }
            GestorAjustes.guardar(this)
            pintar()
        }

        btnAjustesEfectos.setOnClickListener()
        {
            GestorAjustes.efectos = !GestorAjustes.efectos
            GestorAjustes.guardar(this)
            Sonidos.efecto(Sonidos.GOLPE)       // para que se oiga cómo suena
            pintar()
        }

        findViewById<TextView>(R.id.BtnAjustesCerrarSesion).setOnClickListener()
        {
            Avisos.volverAlLogin(this)
        }

        pintar()
    }

    private fun cambiarTema(tema: Tematica)
    {
        GestorAjustes.tema = tema
        GestorAjustes.guardar(this)
        pintar()
    }

    private fun pintar()
    {
        val tema = GestorAjustes.tema
        pintarTitulo(R.id.LblAjustesCabecera)
        findViewById<ImageView>(R.id.ImgAjustesFondo).setImageResource(tema.fondo)
        findViewById<TextView>(R.id.BtnAjustesAtras).setBackgroundResource(tema.circulo)
        findViewById<LinearLayout>(R.id.LayAjustesPanel).setBackgroundResource(tema.panel)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(findViewById(R.id.LblAjustesTematica), findViewById(R.id.LblAjustesSonido),
                                      findViewById(R.id.LblAjustesUsuario))
        for(texto in textos)
        {
            texto.setTextColor(color)
        }

        marcar(btnAjustesTemaNormal, tema == Tematicas.NORMAL)
        marcar(btnAjustesTemaHalloween, tema == Tematicas.HALLOWEEN)
        marcar(btnAjustesTemaNavidad, tema == Tematicas.NAVIDAD)

        // «Música: Sí» a color o «Música: No» medio transparente
        if(GestorAjustes.musica)
        {
            btnAjustesMusica.text = getString(R.string.ajustes_musica_si)
        }
        else
        {
            btnAjustesMusica.text = getString(R.string.ajustes_musica_no)
        }
        marcar(btnAjustesMusica, GestorAjustes.musica)

        if(GestorAjustes.efectos)
        {
            btnAjustesEfectos.text = getString(R.string.ajustes_efectos_si)
        }
        else
        {
            btnAjustesEfectos.text = getString(R.string.ajustes_efectos_no)
        }
        marcar(btnAjustesEfectos, GestorAjustes.efectos)
    }

    private fun marcar(boton: TextView, activo: Boolean)
    {
        if(activo)
        {
            boton.alpha = 1f
        }
        else
        {
            boton.alpha = 0.55f
        }
    }
}
