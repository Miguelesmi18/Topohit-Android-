package com.groundstudio.topohit.activities

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.groundstudio.topohit.R
import com.groundstudio.topohit.adapters.RecordAdapter
import com.groundstudio.topohit.data_model.FilaRecord
import com.groundstudio.topohit.databinding.ActivityRecordsBinding
import com.groundstudio.topohit.util.Juego

// Mis récords: la mejor puntuación de cada modo y dificultad, y las estrellas de cada nivel.
// (El Figma tiene un ranking global, pero la API todavía no tiene una ruta para el top 10 de todos.)
class RecordsActivity : PantallaBase() {

    private lateinit var binding: ActivityRecordsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        binding.layPanel.setBackgroundResource(tema.panel)

        binding.lstRecords.layoutManager = LinearLayoutManager(this)

        binding.btnAtras.setOnClickListener {
            finish()
        }
        binding.btnTabContrarreloj.setOnClickListener {
            mostrar(Juego.CONTRARRELOJ)
        }
        binding.btnTabInfinito.setOnClickListener {
            mostrar(Juego.INFINITO)
        }
        binding.btnTabHistoria.setOnClickListener {
            mostrar(Juego.HISTORIA)
        }
        mostrar(Juego.CONTRARRELOJ)
    }

    private fun mostrar(modo: String) {
        // La pestaña elegida va con el color principal y las otras con el secundario
        marcar(binding.btnTabContrarreloj, modo == Juego.CONTRARRELOJ)
        marcar(binding.btnTabInfinito, modo == Juego.INFINITO)
        marcar(binding.btnTabHistoria, modo == Juego.HISTORIA)

        val filas = mutableListOf<FilaRecord>()
        if (modo == Juego.CONTRARRELOJ) {
            for (d in Juego.contrarreloj) {
                filas.add(FilaRecord(d.nombre, "" + d.agujeros + " agujeros · objetivo " + d.objetivo,
                    Juego.record(modo, d.nombre), R.drawable.martillo))
            }
        } else if (modo == Juego.INFINITO) {
            for (d in Juego.infinito) {
                filas.add(FilaRecord(d.nombre, "" + d.agujeros + " agujeros · " + d.vidas + " vidas",
                    Juego.record(modo, d.nombre), R.drawable.corazon_lleno))
            }
        } else {
            for (n in Juego.niveles) {
                val estrellas = Juego.estrellasDeNivel(n.numero)
                var texto = ""
                for (e in 1..3) {
                    if (e <= estrellas) {
                        texto = texto + "★"
                    } else {
                        texto = texto + "☆"
                    }
                }
                var icono = R.drawable.estrella_vacia
                if (estrellas > 0) {
                    icono = R.drawable.estrella_llena
                }
                filas.add(FilaRecord("Nivel " + n.numero + " · " + n.zona, texto,
                    Juego.record(modo, "Nivel " + n.numero), icono))
            }
        }
        binding.lstRecords.adapter = RecordAdapter(filas)
    }

    private fun marcar(boton: TextView, elegida: Boolean) {
        if (elegida) {
            boton.setBackgroundResource(Juego.tema.botonPrincipal)
            boton.alpha = 1f
        } else {
            boton.setBackgroundResource(Juego.tema.botonSecundario)
            boton.alpha = 0.75f
        }
    }
}
