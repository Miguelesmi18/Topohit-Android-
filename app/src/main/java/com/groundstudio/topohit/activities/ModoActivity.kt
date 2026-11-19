package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
<<<<<<< HEAD
=======
import androidx.appcompat.app.AppCompatActivity
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.databinding.ActivityModoBinding
import com.groundstudio.topohit.util.Juego

// Selección de modo: Historia, Contrarreloj o Infinito
<<<<<<< HEAD
class ModoActivity : PantallaBase() {
=======
class ModoActivity : AppCompatActivity() {
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880

    private lateinit var binding: ActivityModoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        binding.imgModo1.setImageResource(tema.topoNormal)
        binding.imgModo2.setImageResource(tema.topoGolpeado)
        binding.imgModo3.setImageResource(tema.topoDorado)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(binding.lblTitulo1, binding.lblTitulo2, binding.lblTitulo3,
            binding.lblDesc1, binding.lblDesc2, binding.lblDesc3)
        for (t in textos) {
            t.setTextColor(color)
        }
        binding.layTarjeta1.setBackgroundResource(tema.panel)
        binding.layTarjeta2.setBackgroundResource(tema.panel)
        binding.layTarjeta3.setBackgroundResource(tema.panel)
        binding.btnElegir1.setBackgroundResource(tema.botonPrincipal)
        binding.btnElegir2.setBackgroundResource(tema.botonPrincipal)
        binding.btnElegir3.setBackgroundResource(tema.botonPrincipal)

        binding.btnAtras.setOnClickListener {
            finish()
        }
        // Toda la tarjeta y su botón llevan al mismo sitio
        binding.layTarjeta1.setOnClickListener { abrirHistoria() }
        binding.btnElegir1.setOnClickListener { abrirHistoria() }
        binding.layTarjeta2.setOnClickListener { abrirDificultad(Juego.CONTRARRELOJ) }
        binding.btnElegir2.setOnClickListener { abrirDificultad(Juego.CONTRARRELOJ) }
        binding.layTarjeta3.setOnClickListener { abrirDificultad(Juego.INFINITO) }
        binding.btnElegir3.setOnClickListener { abrirDificultad(Juego.INFINITO) }
    }

    private fun abrirHistoria() {
        startActivity(Intent(this, MapaHistoriaActivity::class.java))
    }

    private fun abrirDificultad(modo: String) {
        val intent = Intent(this, DificultadActivity::class.java)
        intent.putExtra(Constantes.MODO, modo)
        startActivity(intent)
    }
}
