package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.data_model.Tematica
import com.groundstudio.topohit.data_model.Tematicas
import com.groundstudio.topohit.databinding.ActivityAjustesBinding
import com.groundstudio.topohit.util.Almacen
import com.groundstudio.topohit.util.Juego
import com.groundstudio.topohit.util.Sonidos

// Ajustes: temática, sonido y cerrar sesión
class AjustesActivity : PantallaBase() {

    private lateinit var binding: ActivityAjustesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si todavía no ha entrado nadie (venimos del login) no hay sesión que cerrar
        if (Juego.username.isEmpty()) {
            binding.laySesion.visibility = View.GONE
        } else {
            binding.lblUsuario.text = "Has entrado como " + Juego.username
        }

        binding.btnAtras.setOnClickListener {
            finish()
        }
        binding.btnTemaNormal.setOnClickListener {
            cambiarTema(Tematicas.NORMAL)
        }
        binding.btnTemaHalloween.setOnClickListener {
            cambiarTema(Tematicas.HALLOWEEN)
        }
        binding.btnTemaNavidad.setOnClickListener {
            cambiarTema(Tematicas.NAVIDAD)
        }
        // Música y efectos: cada toque los enciende o los apaga
        binding.btnMusica.setOnClickListener {
            Sonidos.musicaActivada = !Sonidos.musicaActivada
            if (Sonidos.musicaActivada) {
                Sonidos.sonarMusica()
            } else {
                Sonidos.pausarMusica()
            }
            Almacen.guardarAjustes(this)
            pintar()
        }
        binding.btnEfectos.setOnClickListener {
            Sonidos.efectosActivados = !Sonidos.efectosActivados
            Almacen.guardarAjustes(this)
            Sonidos.efecto(Sonidos.GOLPE)       // para que se oiga cómo suena
            pintar()
        }
        binding.btnCerrarSesion.setOnClickListener {
            Almacen.borrarSesion(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        pintar()
    }

    private fun cambiarTema(tema: Tematica) {
        Juego.tema = tema
        Almacen.guardarAjustes(this)
        pintar()
    }

    private fun pintar() {
        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        binding.layPanel.setBackgroundResource(tema.panel)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(binding.lblTematica, binding.lblSonido, binding.lblUsuario)
        for (t in textos) {
            t.setTextColor(color)
        }
        marcar(binding.btnTemaNormal, tema == Tematicas.NORMAL)
        marcar(binding.btnTemaHalloween, tema == Tematicas.HALLOWEEN)
        marcar(binding.btnTemaNavidad, tema == Tematicas.NAVIDAD)

        // Botones de sonido: "Música: Sí" a color o "Música: No" medio transparente
        if (Sonidos.musicaActivada) {
            binding.btnMusica.text = "Música: Sí"
        } else {
            binding.btnMusica.text = "Música: No"
        }
        marcar(binding.btnMusica, Sonidos.musicaActivada)
        if (Sonidos.efectosActivados) {
            binding.btnEfectos.text = "Efectos: Sí"
        } else {
            binding.btnEfectos.text = "Efectos: No"
        }
        marcar(binding.btnEfectos, Sonidos.efectosActivados)
    }

    private fun marcar(boton: TextView, actual: Boolean) {
        if (actual) {
            boton.alpha = 1f
        } else {
            boton.alpha = 0.55f
        }
    }
}
