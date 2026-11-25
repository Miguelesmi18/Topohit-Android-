package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.R
import com.groundstudio.topohit.ReglasJuego

// Cómo jugar: 3 pasos con dibujos de la temática elegida
class ComoJugarActivity : PantallaBase()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_como_jugar)

        val tema = GestorAjustes.tema
        findViewById<ImageView>(R.id.ImgComoJugarFondo).setImageResource(tema.fondo)
        findViewById<TextView>(R.id.BtnComoJugarAtras).setBackgroundResource(tema.circulo)
        val btnComoJugarJugar = findViewById<TextView>(R.id.BtnComoJugarJugar)
        btnComoJugarJugar.setBackgroundResource(tema.botonPrincipal)
        btnComoJugarJugar.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
        findViewById<LinearLayout>(R.id.LayComoJugarPaso1).setBackgroundResource(tema.panel)
        findViewById<LinearLayout>(R.id.LayComoJugarPaso2).setBackgroundResource(tema.panel)
        findViewById<LinearLayout>(R.id.LayComoJugarPaso3).setBackgroundResource(tema.panel)
        findViewById<ImageView>(R.id.ImgComoJugarPaso1a).setImageResource(tema.topoNormal)
        findViewById<ImageView>(R.id.ImgComoJugarPaso1b).setImageResource(tema.topoEnfadado)
        findViewById<ImageView>(R.id.ImgComoJugarPaso2).setImageResource(tema.bomba)
        findViewById<ImageView>(R.id.ImgComoJugarPaso3).setImageResource(tema.topoDorado)

        findViewById<TextView>(R.id.LblComoJugarTexto1).text =
            getString(R.string.como_paso1_texto, ReglasJuego.PUNTOS_NORMAL, ReglasJuego.PUNTOS_ENFADADO)
        findViewById<TextView>(R.id.LblComoJugarTexto3).text =
            getString(R.string.como_paso3_texto, ReglasJuego.PUNTOS_DORADO, ReglasJuego.ACIERTOS_PARA_COMBO)

        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(findViewById(R.id.LblComoJugarTitulo1), findViewById(R.id.LblComoJugarTitulo2),
                                      findViewById(R.id.LblComoJugarTitulo3), findViewById(R.id.LblComoJugarTexto1),
                                      findViewById(R.id.LblComoJugarTexto2), findViewById(R.id.LblComoJugarTexto3))
        for(texto in textos)
        {
            texto.setTextColor(color)
        }

        findViewById<TextView>(R.id.BtnComoJugarAtras).setOnClickListener()
        {
            finish()
        }

        findViewById<TextView>(R.id.BtnComoJugarJugar).setOnClickListener()
        {
            startActivity(Intent(this, DificultadActivity::class.java))
            finish()
        }
    }
}
