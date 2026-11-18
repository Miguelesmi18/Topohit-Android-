package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.api.RetrofitClient
import com.groundstudio.topohit.data_model.Tematica
import com.groundstudio.topohit.data_model.Tematicas
import com.groundstudio.topohit.databinding.ActivityMenuBinding
import com.groundstudio.topohit.util.Almacen
import com.groundstudio.topohit.util.Avatares
import com.groundstudio.topohit.util.Juego
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Menú principal: jugar, récords, perfil y los 3 botones de temática
class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnJugar.setOnClickListener {
            startActivity(Intent(this, ModoActivity::class.java))
        }
        binding.btnRecords.setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
        }
        binding.btnPerfil.setOnClickListener {
            if (Juego.invitado) {
                Toast.makeText(this, "Sin conexión no hay perfil. ¡Crea una cuenta!", Toast.LENGTH_LONG).show()
            } else {
                startActivity(Intent(this, PerfilActivity::class.java))
            }
        }
        binding.btnComoJugar.setOnClickListener {
            startActivity(Intent(this, ComoJugarActivity::class.java))
        }
        binding.btnAjustes.setOnClickListener {
            startActivity(Intent(this, AjustesActivity::class.java))
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
    }

    // Cada vez que volvemos al menú: pintamos y pedimos el progreso a la API
    override fun onResume() {
        super.onResume()
        // Si en Ajustes se ha cerrado la sesión, volvemos al login
        if (Juego.username.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        pintar()
        cargarProgreso()
    }

    private fun cambiarTema(tema: Tematica) {
        Juego.tema = tema
        Almacen.guardarAjustes(this)
        pintar()
    }

    private fun pintar() {
        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.imgLogo.setImageResource(tema.logo)
        binding.imgAvatar.setImageResource(Avatares.dibujo(Juego.avatar))
        binding.imgAvatar.setBackgroundResource(tema.circulo)
        binding.btnJugar.setBackgroundResource(tema.botonPrincipal)
        binding.btnRecords.setBackgroundResource(tema.botonSecundario)
        binding.btnPerfil.setBackgroundResource(tema.botonSecundario)
        binding.btnComoJugar.setBackgroundResource(tema.circulo)
        binding.btnAjustes.setBackgroundResource(tema.circulo)
        binding.layTematicas.setBackgroundResource(tema.panel)
        binding.lblTematica.setTextColor(ContextCompat.getColor(this, tema.colorTexto))

        // El botón de la temática actual se ve entero; los otros, un poco transparentes
        marcarTema(binding.btnTemaNormal, tema == Tematicas.NORMAL)
        marcarTema(binding.btnTemaHalloween, tema == Tematicas.HALLOWEEN)
        marcarTema(binding.btnTemaNavidad, tema == Tematicas.NAVIDAD)

        binding.lblHola.text = "¡Hola, " + Juego.username + "!"
        binding.lblRecord.text = "Récord " + Juego.mejorRecord() + " · " + Juego.totalEstrellas() + " estrellas"
    }

    private fun marcarTema(boton: TextView, actual: Boolean) {
        if (actual) {
            boton.alpha = 1f
            boton.text = "✓ " + boton.text.toString().replace("✓ ", "")
        } else {
            boton.alpha = 0.6f
            boton.text = boton.text.toString().replace("✓ ", "")
        }
    }

    // GET api/sesiones/progreso: estrellas, niveles desbloqueados y récords
    @OptIn(DelicateCoroutinesApi::class)
    private fun cargarProgreso() {
        if (Juego.invitado) {
            return
        }
        GlobalScope.launch {
            try {
                val response = RetrofitClient.api().getProgreso(Juego.cabeceraToken()).execute()
                val progreso = response.body()
                runOnUiThread {
                    if (response.isSuccessful && progreso != null) {
                        Juego.progreso = progreso
                        pintar()
                    } else if (response.code() == 401) {
                        // El token ha caducado: hay que volver a entrar
                        Toast.makeText(this@MenuActivity, "La sesión ha caducado. Vuelve a entrar.", Toast.LENGTH_LONG).show()
                        Almacen.borrarSesion(this@MenuActivity)
                        startActivity(Intent(this@MenuActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()     // sin conexión: se usa el progreso que ya tenemos
            }
        }
    }
}
