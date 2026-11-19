package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
<<<<<<< HEAD
=======
import androidx.appcompat.app.AppCompatActivity
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.R
import com.groundstudio.topohit.databinding.ActivityResultadoBinding
import com.groundstudio.topohit.util.Juego

// Pantalla final: Victoria, ¡Se acabó el tiempo! o ¡Se acabaron las vidas!
<<<<<<< HEAD
class ResultadoActivity : PantallaBase() {
=======
class ResultadoActivity : AppCompatActivity() {
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880

    private lateinit var binding: ActivityResultadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val modo = intent.getStringExtra(Constantes.MODO) ?: Juego.CONTRARRELOJ
        val dificultad = intent.getStringExtra(Constantes.DIFICULTAD) ?: "Fácil"
        val nivel = intent.getIntExtra(Constantes.NIVEL, 0)
        val resultado = intent.getStringExtra(Constantes.RESULTADO) ?: Constantes.TIEMPO
        val puntos = intent.getIntExtra(Constantes.PUNTOS, 0)
        val estrellas = intent.getIntExtra(Constantes.ESTRELLAS, 0)
        val objetivo = intent.getIntExtra(Constantes.OBJETIVO, 0)

        // Temática
        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.layPanel.setBackgroundResource(tema.panel)
        binding.btnSiguiente.setBackgroundResource(tema.botonPrincipal)
        binding.btnRepetir.setBackgroundResource(tema.botonSecundario)
        binding.btnMenu.setBackgroundResource(tema.botonSecundario)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        binding.lblTitulo.setTextColor(color)
        binding.lblPuntos.setTextColor(color)
        binding.lblSubtitulo.setTextColor(color)

        // Datos de la partida
        binding.lblPuntos.text = "" + puntos + " puntos"
        binding.lblAciertos.text = "" + intent.getIntExtra(Constantes.ACIERTOS, 0)
        binding.lblFallos.text = "" + intent.getIntExtra(Constantes.FALLOS, 0)
        binding.lblEscapados.text = "" + intent.getIntExtra(Constantes.ESCAPADOS, 0)
        binding.lblBombas.text = "" + intent.getIntExtra(Constantes.BOMBAS, 0)
        binding.lblRacha.text = "" + intent.getIntExtra(Constantes.MEJOR_RACHA, 0)
        if (intent.getBooleanExtra(Constantes.NUEVO_RECORD, false)) {
            binding.lblNuevoRecord.visibility = View.VISIBLE
        }

        val imagenes = listOf<ImageView>(binding.imgEstrella1, binding.imgEstrella2, binding.imgEstrella3)

        // Título, estrellas y dibujo según cómo ha terminado
        if (resultado == Constantes.VICTORIA) {
            binding.imgTopo.setImageResource(tema.topoNormal)
            if (modo == Juego.HISTORIA && estrellas < 3) {
                binding.lblTitulo.text = "¡Nivel superado!"
            } else {
                binding.lblTitulo.text = "¡Objetivo conseguido!"
            }
        } else if (resultado == Constantes.SIN_VIDAS) {
            binding.imgTopo.setImageResource(tema.topoBurlon)
            binding.lblTitulo.text = "¡Se acabaron las vidas!"
        } else {
            binding.imgTopo.setImageResource(tema.topoBurlon)
            binding.lblTitulo.text = "¡Se acabó el tiempo!"
        }

        if (modo == Juego.HISTORIA) {
            for (i in 0..2) {
                if (i < estrellas) {
                    imagenes[i].setImageResource(R.drawable.estrella_llena)
                } else {
                    imagenes[i].setImageResource(R.drawable.estrella_vacia)
                }
            }
            if (resultado != Constantes.VICTORIA) {
                binding.lblSubtitulo.text = "¡Casi! Inténtalo otra vez"
            } else if (objetivo > 0 && estrellas < 3) {
                binding.lblSubtitulo.text = "Consigue " + objetivo + " puntos para las 3 estrellas"
            }
        } else if (modo == Juego.INFINITO) {
            for (img in imagenes) {
                img.setImageResource(R.drawable.corazon_vacio)
            }
            val record = Juego.record(modo, dificultad)
            if (puntos >= record) {
                binding.lblSubtitulo.text = "¡Es tu mejor partida!"
            } else {
                binding.lblSubtitulo.text = "Récord: " + record + " · ¡Te faltan " + (record - puntos) + "!"
            }
        } else {
            // Contrarreloj: sin estrellas
            binding.layEstrellas.visibility = View.GONE
            if (resultado == Constantes.VICTORIA) {
                binding.lblSubtitulo.text = "Objetivo: " + objetivo
            } else {
                binding.lblSubtitulo.text = "¡Casi! Te faltaron " + (objetivo - puntos) + " puntos"
            }
        }

        // Botón "Siguiente" solo en Historia, si se ha superado y hay otro nivel
        val haySiguiente = modo == Juego.HISTORIA && resultado == Constantes.VICTORIA && nivel < Juego.niveles.size
        if (haySiguiente) {
            binding.btnSiguiente.visibility = View.VISIBLE
        } else {
            binding.btnSiguiente.visibility = View.GONE
        }
        if (resultado != Constantes.VICTORIA) {
            binding.btnRepetir.text = getString(R.string.reintentar)
        }

        binding.btnRepetir.setOnClickListener {
            jugar(modo, dificultad, nivel)
        }
        binding.btnSiguiente.setOnClickListener {
            // Volvemos al mapa, que abre el aviso del nivel siguiente
            val intent = Intent(this, MapaHistoriaActivity::class.java)
            intent.putExtra(Constantes.NIVEL, nivel + 1)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun jugar(modo: String, dificultad: String, nivel: Int) {
        val intent = Intent(this, PartidaActivity::class.java)
        intent.putExtra(Constantes.MODO, modo)
        intent.putExtra(Constantes.DIFICULTAD, dificultad)
        intent.putExtra(Constantes.NIVEL, nivel)
        startActivity(intent)
        finish()
    }
}
