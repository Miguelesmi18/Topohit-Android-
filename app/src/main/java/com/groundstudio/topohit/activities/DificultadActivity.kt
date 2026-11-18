package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.R
import com.groundstudio.topohit.databinding.ActivityDificultadBinding
import com.groundstudio.topohit.util.Juego

// Elegir la dificultad de Contrarreloj (Fácil, Medio, Difícil) o de Infinito (Fácil, Normal, Difícil)
class DificultadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDificultadBinding
    private var modo = Juego.CONTRARRELOJ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDificultadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modo = intent.getStringExtra(Constantes.MODO) ?: Juego.CONTRARRELOJ
        binding.lblTitulo.text = modo

        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        val tarjetas = listOf(binding.layTarjeta1, binding.layTarjeta2, binding.layTarjeta3)
        for (t in tarjetas) {
            t.setBackgroundResource(tema.panel)
        }
        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(binding.lblTitulo1, binding.lblTitulo2, binding.lblTitulo3,
            binding.lblInfo1, binding.lblInfo2, binding.lblInfo3,
            binding.lblExtra1, binding.lblExtra2, binding.lblExtra3)
        for (t in textos) {
            t.setTextColor(color)
        }

        binding.btnAtras.setOnClickListener {
            finish()
        }
    }

    // En onResume para que el récord se actualice al volver de una partida
    override fun onResume() {
        super.onResume()
        val vistas = listOf(binding.layVista1, binding.layVista2, binding.layVista3)
        val titulos = listOf(binding.lblTitulo1, binding.lblTitulo2, binding.lblTitulo3)
        val infos = listOf(binding.lblInfo1, binding.lblInfo2, binding.lblInfo3)
        val extras = listOf(binding.lblExtra1, binding.lblExtra2, binding.lblExtra3)
        val records = listOf(binding.lblRecord1, binding.lblRecord2, binding.lblRecord3)
        val tarjetas = listOf(binding.layTarjeta1, binding.layTarjeta2, binding.layTarjeta3)

        for (i in 0..2) {
            val nombre: String
            val agujeros: Int
            if (modo == Juego.CONTRARRELOJ) {
                val d = Juego.contrarreloj[i]
                nombre = d.nombre
                agujeros = d.agujeros
                infos[i].text = "" + d.agujeros + " agujeros · " + d.tiempo + " s"
                extras[i].text = "Objetivo " + d.objetivo
            } else {
                val d = Juego.infinito[i]
                nombre = d.nombre
                agujeros = d.agujeros
                infos[i].text = "" + d.agujeros + " agujeros"
                var texto = ""
                for (v in 1..d.vidas) {
                    texto = texto + "❤"
                }
                if (i == 2) {
                    texto = texto + "  + más bombas"
                }
                extras[i].text = texto
            }
            titulos[i].text = nombre.uppercase()
            records[i].text = "Récord " + Juego.record(modo, nombre)
            pintarAgujeros(vistas[i], agujeros)

            val dificultad = nombre
            tarjetas[i].setOnClickListener {
                val intent = Intent(this, PartidaActivity::class.java)
                intent.putExtra(Constantes.MODO, modo)
                intent.putExtra(Constantes.DIFICULTAD, dificultad)
                startActivity(intent)
            }
        }
    }

    // Vista previa: filas de 3 agujeros pequeños (inflando layouts, como en las listas personalizadas)
    private fun pintarAgujeros(contenedor: LinearLayout, agujeros: Int) {
        contenedor.removeAllViews()
        val filas = agujeros / 3
        for (f in 1..filas) {
            val fila = layoutInflater.inflate(R.layout.fila_mini, contenedor, false) as LinearLayout
            for (c in 1..3) {
                val agujero = layoutInflater.inflate(R.layout.item_agujero_mini, fila, false)
                fila.addView(agujero)
            }
            contenedor.addView(fila)
        }
    }
}
