package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.Avatares
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.Sonidos
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.Tematicas
import com.groundstudio.topohit.datamodel.Tematica

// Menú principal: ¡JUGAR! (va directo a elegir la dificultad de Infinito), ranking,
// perfil, cómo jugar, ajustes y los 3 botones de temática.
class MenuActivity : PantallaBase()
{
    private lateinit var btnMenuTemaNormal: TextView
    private lateinit var btnMenuTemaHalloween: TextView
    private lateinit var btnMenuTemaNavidad: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        btnMenuTemaNormal    = findViewById<TextView>(R.id.BtnMenuTemaNormal)
        btnMenuTemaHalloween = findViewById<TextView>(R.id.BtnMenuTemaHalloween)
        btnMenuTemaNavidad   = findViewById<TextView>(R.id.BtnMenuTemaNavidad)

        findViewById<TextView>(R.id.BtnMenuJugar).setOnClickListener()
        {
            startActivity(Intent(this, DificultadActivity::class.java))
        }

        findViewById<TextView>(R.id.BtnMenuRanking).setOnClickListener()
        {
            startActivity(Intent(this, RankingActivity::class.java))
        }

        findViewById<TextView>(R.id.BtnMenuPerfil).setOnClickListener()
        {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        findViewById<TextView>(R.id.BtnMenuMusica).setOnClickListener()
        {
            cambiarMusica()
        }

        findViewById<TextView>(R.id.BtnMenuComoJugar).setOnClickListener()
        {
            startActivity(Intent(this, ComoJugarActivity::class.java))
        }

        findViewById<TextView>(R.id.BtnMenuAjustes).setOnClickListener()
        {
            startActivity(Intent(this, AjustesActivity::class.java))
        }

        btnMenuTemaNormal.setOnClickListener()
        {
            cambiarTema(Tematicas.NORMAL)
        }

        btnMenuTemaHalloween.setOnClickListener()
        {
            cambiarTema(Tematicas.HALLOWEEN)
        }

        btnMenuTemaNavidad.setOnClickListener()
        {
            cambiarTema(Tematicas.NAVIDAD)
        }
    }

    // Cada vez que se vuelve al menú se pinta otra vez (puede haber cambiado el avatar o la temática).
    // Si en Ajustes se ha cerrado la sesión, se vuelve al login.
    override fun onResume()
    {
        super.onResume()
        if(GestorSesion.getSesion(this) != null)
        {
            pintar()
        }
        else
        {
            Avisos.volverAlLogin(this)
        }
    }

    // Pone o quita la música con un toque y lo guarda en los ajustes (igual que el botón de Ajustes)
    private fun cambiarMusica()
    {
        GestorAjustes.musica = !GestorAjustes.musica
        GestorAjustes.guardar(this)
        if(GestorAjustes.musica)
        {
            Sonidos.sonarMusica()
        }
        else
        {
            Sonidos.pausarMusica()
        }
        pintarMusica()
    }

    // El botón enseña el altavoz con sonido o tachado, y su descripción dice lo que hará al tocarlo
    private fun pintarMusica()
    {
        val btnMenuMusica = findViewById<TextView>(R.id.BtnMenuMusica)
        if(GestorAjustes.musica)
        {
            btnMenuMusica.text = getString(R.string.icono_musica_si)
            btnMenuMusica.contentDescription = getString(R.string.musica_quitar)
        }
        else
        {
            btnMenuMusica.text = getString(R.string.icono_musica_no)
            btnMenuMusica.contentDescription = getString(R.string.musica_poner)
        }
    }

    private fun cambiarTema(tema: Tematica)
    {
        GestorAjustes.tema = tema
        GestorAjustes.guardar(this)
        Sonidos.cambiarMusica(applicationContext)     // cada temática tiene su canción
        pintar()
    }

    private fun pintar()
    {
        val tema = GestorAjustes.tema
        findViewById<ImageView>(R.id.ImgMenuFondo).setImageResource(tema.fondo)
        findViewById<ImageView>(R.id.ImgMenuLogo).setImageResource(tema.logo)
        val imgMenuAvatar = findViewById<ImageView>(R.id.ImgMenuAvatar)
        imgMenuAvatar.setImageResource(Avatares.dibujo(GestorSesion.getAvatar(this)))
        imgMenuAvatar.setBackgroundResource(tema.circulo)
        val btnMenuJugar = findViewById<TextView>(R.id.BtnMenuJugar)
        btnMenuJugar.setBackgroundResource(tema.botonPrincipal)
        btnMenuJugar.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
        val btnMenuRanking = findViewById<TextView>(R.id.BtnMenuRanking)
        btnMenuRanking.setBackgroundResource(tema.botonSecundario)
        btnMenuRanking.setTextColor(ContextCompat.getColor(this, tema.colorTextoBotonSecundario))
        val btnMenuPerfil = findViewById<TextView>(R.id.BtnMenuPerfil)
        btnMenuPerfil.setBackgroundResource(tema.botonSecundario)
        btnMenuPerfil.setTextColor(ContextCompat.getColor(this, tema.colorTextoBotonSecundario))
        findViewById<TextView>(R.id.BtnMenuComoJugar).setBackgroundResource(tema.circulo)
        findViewById<TextView>(R.id.BtnMenuAjustes).setBackgroundResource(tema.circulo)
        findViewById<TextView>(R.id.BtnMenuMusica).setBackgroundResource(tema.circulo)
        pintarMusica()
        findViewById<LinearLayout>(R.id.LayMenuTematicas).setBackgroundResource(tema.panel)
        findViewById<TextView>(R.id.LblMenuTematica).setTextColor(ContextCompat.getColor(this, tema.colorTexto))
        findViewById<TextView>(R.id.LblMenuHola).text = getString(R.string.menu_hola, GestorSesion.getUsername(this))

        // El botón de la temática actual se ve entero y con ✓; los otros, un poco transparentes
        marcarTema(btnMenuTemaNormal, R.string.normal, tema == Tematicas.NORMAL)
        marcarTema(btnMenuTemaHalloween, R.string.halloween, tema == Tematicas.HALLOWEEN)
        marcarTema(btnMenuTemaNavidad, R.string.navidad, tema == Tematicas.NAVIDAD)
    }

    private fun marcarTema(boton: TextView, idTexto: Int, actual: Boolean)
    {
        if(actual)
        {
            boton.alpha = 1f
            boton.text = getString(R.string.tema_elegido, getString(idTexto))
        }
        else
        {
            boton.alpha = 0.6f
            boton.text = getString(idTexto)
        }
    }
}
