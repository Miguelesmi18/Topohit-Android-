package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.groundstudio.topohit.adapters.HistorialAdapter
import com.groundstudio.topohit.api.RetrofitClient
import com.groundstudio.topohit.data_model.AvatarPeticion
import com.groundstudio.topohit.data_model.Sesion
import com.groundstudio.topohit.databinding.ActivityPerfilBinding
import com.groundstudio.topohit.util.Almacen
import com.groundstudio.topohit.util.Avatares
import com.groundstudio.topohit.util.Juego
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Perfil: avatar, resumen y el historial de partidas (GET api/sesiones/mias)
class PerfilActivity : PantallaBase() {

    private lateinit var binding: ActivityPerfilBinding
    private var avatarElegido = 1          // CAMBIO 19/11: id del avatar (1 a 12)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        binding.imgAvatar.setBackgroundResource(tema.circulo)
        binding.layPanelJugador.setBackgroundResource(tema.panel)
        binding.layPanelHistorial.setBackgroundResource(tema.panel)
        binding.layPanelAvatares.setBackgroundResource(tema.panel)
        binding.btnCambiarAvatar.setBackgroundResource(tema.botonSecundario)
        binding.btnGuardarAvatar.setBackgroundResource(tema.botonPrincipal)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(binding.lblNombre, binding.lblRol, binding.lblHistorial,
            binding.lblVacio, binding.lblElige)
        for (t in textos) {
            t.setTextColor(color)
        }

        binding.lblNombre.text = Juego.username
        binding.lblRol.text = "Rol: " + Juego.rol
        binding.imgAvatar.setImageResource(Avatares.dibujo(Juego.avatar))
        binding.lblEstrellas.text = "" + Juego.totalEstrellas() + "/" + (Juego.niveles.size * 3)

        binding.lstHistorial.layoutManager = LinearLayoutManager(this)

        binding.btnAtras.setOnClickListener {
            finish()
        }
        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        // Cambiar avatar: se abre la rejilla y al guardar se envía a la API (CAMBIO 19/11)
        avatarElegido = Juego.avatar
        val imagenes = Avatares.imagenes(binding.selAvatares)
        for (i in 0..imagenes.size - 1) {
            imagenes[i].setOnClickListener {
                avatarElegido = i + 1              // la imagen i es el avatar i + 1
                Avatares.pintar(binding.selAvatares, avatarElegido)
            }
        }
        binding.btnCambiarAvatar.setOnClickListener {
            Avatares.pintar(binding.selAvatares, avatarElegido)
            binding.layAvatares.visibility = View.VISIBLE
        }
        binding.btnGuardarAvatar.setOnClickListener {
            guardarAvatar()
        }

        cargarHistorial()
    }

    // CAMBIO 19/11: PUT api/auth/avatar. Si la API dice que sí, se cambia también en la tablet
    @OptIn(DelicateCoroutinesApi::class)
    private fun guardarAvatar() {
        binding.btnGuardarAvatar.isEnabled = false
        GlobalScope.launch {
            try {
                val peticion = AvatarPeticion(avatarElegido)
                val response = RetrofitClient.api().cambiarAvatar(Juego.cabeceraToken(), peticion).execute()
                val respuesta = response.body()
                runOnUiThread {
                    binding.btnGuardarAvatar.isEnabled = true
                    if (response.isSuccessful && respuesta != null) {
                        Juego.avatar = respuesta.avatarId
                        Almacen.guardarSesion(this@PerfilActivity)
                        binding.imgAvatar.setImageResource(Avatares.dibujo(Juego.avatar))
                        binding.layAvatares.visibility = View.GONE
                        Toast.makeText(this@PerfilActivity, "¡Avatar cambiado!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PerfilActivity, RetrofitClient.mensajeDeError(response), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.btnGuardarAvatar.isEnabled = true
                    Toast.makeText(this@PerfilActivity, "No se puede conectar con la API", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cerrarSesion() {
        Almacen.borrarSesion(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun cargarHistorial() {
        GlobalScope.launch {
            try {
                val response = RetrofitClient.api().getMisPartidas(Juego.cabeceraToken()).execute()
                val partidas = response.body()
                runOnUiThread {
                    if (response.isSuccessful && partidas != null) {
                        mostrarHistorial(partidas)
                    } else {
                        binding.lblVacio.text = RetrofitClient.mensajeDeError(response)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.lblVacio.text = "No se puede conectar con la API"
                }
            }
        }
    }

    private fun mostrarHistorial(partidas: List<Sesion>) {
        if (partidas.isEmpty()) {
            binding.lblVacio.text = "Todavía no has jugado ninguna partida"
        } else {
            binding.lblVacio.visibility = View.GONE
        }
        binding.lstHistorial.adapter = HistorialAdapter(partidas)

        // Resumen de arriba
        var recordCr = 0
        var recordInf = 0
        var aciertos = 0
        var intentos = 0
        for (p in partidas) {
            val modo = p.modo ?: ""
            if (modo.equals(Juego.CONTRARRELOJ, true) && p.finalScore > recordCr) {
                recordCr = p.finalScore
            }
            if (modo.equals(Juego.INFINITO, true) && p.finalScore > recordInf) {
                recordInf = p.finalScore
            }
            aciertos = aciertos + p.hits
            intentos = intentos + p.nAttempts
        }
        binding.lblPartidas.text = "" + partidas.size
        binding.lblRecordCr.text = "" + recordCr
        binding.lblRecordInf.text = "" + recordInf
        if (intentos > 0) {
            binding.lblPrecision.text = "" + (aciertos * 100 / intentos) + " %"
        } else {
            binding.lblPrecision.text = "-"
        }
    }
}
