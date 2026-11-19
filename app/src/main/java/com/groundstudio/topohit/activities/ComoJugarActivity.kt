package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
<<<<<<< HEAD
=======
import androidx.appcompat.app.AppCompatActivity
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.databinding.ActivityComoJugarBinding
import com.groundstudio.topohit.util.Juego

// Cómo jugar: 3 pasos con dibujos
<<<<<<< HEAD
class ComoJugarActivity : PantallaBase() {
=======
class ComoJugarActivity : AppCompatActivity() {
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880

    private lateinit var binding: ActivityComoJugarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComoJugarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        binding.btnAJugar.setBackgroundResource(tema.botonPrincipal)
        binding.layPaso1.setBackgroundResource(tema.panel)
        binding.layPaso2.setBackgroundResource(tema.panel)
        binding.layPaso3.setBackgroundResource(tema.panel)
        binding.imgPaso1a.setImageResource(tema.topoNormal)
        binding.imgPaso1b.setImageResource(tema.topoEnfadado)
        binding.imgPaso2.setImageResource(tema.bomba)
        binding.imgPaso3.setImageResource(tema.topoDorado)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(binding.lblPasoTitulo1, binding.lblPasoTitulo2, binding.lblPasoTitulo3,
            binding.lblPasoTexto1, binding.lblPasoTexto2, binding.lblPasoTexto3)
        for (t in textos) {
            t.setTextColor(color)
        }

        binding.btnAtras.setOnClickListener {
            finish()
        }
        binding.btnAJugar.setOnClickListener {
            startActivity(Intent(this, ModoActivity::class.java))
            finish()
        }
    }
}
