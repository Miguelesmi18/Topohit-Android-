package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.api.RetrofitClient
import com.groundstudio.topohit.data_model.LoginPeticion
import com.groundstudio.topohit.databinding.ActivityLoginBinding
import com.groundstudio.topohit.util.Almacen
import com.groundstudio.topohit.util.Juego
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Pantalla de inicio: usuario y contraseña contra la API (POST api/auth/login)
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Almacen.cargarAjustes(this)
        Juego.cargarConfiguracion()

        // Si ya había entrado antes, vamos directos al menú
        if (Almacen.cargarSesion(this)) {
            irAlMenu()
            return
        }

        binding.btnEntrar.setOnClickListener {
            entrar()
        }
        binding.btnRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
        binding.btnAjustes.setOnClickListener {
            startActivity(Intent(this, AjustesActivity::class.java))
        }
        binding.btnSinConexion.setOnClickListener {
            Juego.cerrarSesion()
            Juego.invitado = true
            Juego.username = "Invitado"
            irAlMenu()
        }
    }

    // Al volver de Ajustes puede haber cambiado la temática
    override fun onResume() {
        super.onResume()
        aplicarTema()
    }

    private fun aplicarTema() {
        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.imgLogo.setImageResource(tema.logo)
        binding.imgTopo.setImageResource(tema.topoNormal)
        binding.layPanel.setBackgroundResource(tema.panel)
        binding.btnEntrar.setBackgroundResource(tema.botonPrincipal)
        binding.btnRegistro.setBackgroundResource(tema.botonSecundario)
        binding.btnAjustes.setBackgroundResource(tema.circulo)
        binding.lblTitulo.setTextColor(ContextCompat.getColor(this, tema.colorTexto))
        binding.lblNoCuenta.setTextColor(ContextCompat.getColor(this, tema.colorTexto))
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun entrar() {
        val usuario = binding.txtUsuario.text.toString().trim()
        val contrasena = binding.txtContrasena.text.toString()
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Escribe tu usuario y tu contraseña", Toast.LENGTH_LONG).show()
            return
        }

        cargando(true)
        GlobalScope.launch {
            try {
                val response = RetrofitClient.api().login(LoginPeticion(usuario, contrasena)).execute()
                val respuesta = response.body()
                runOnUiThread {
                    cargando(false)
                    if (response.isSuccessful && respuesta != null) {
                        Juego.token = respuesta.token
                        Juego.idUsuario = respuesta.idUsuario
                        Juego.username = respuesta.username
                        Juego.rol = respuesta.rol
                        Juego.invitado = false
                        Juego.avatar = Almacen.cargarAvatar(this@LoginActivity, respuesta.username)
                        Almacen.guardarSesion(this@LoginActivity)
                        irAlMenu()
                    } else {
                        Toast.makeText(this@LoginActivity, RetrofitClient.mensajeDeError(response), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    cargando(false)
                    Toast.makeText(this@LoginActivity,
                        "No se puede conectar con la API (" + RetrofitClient.url + "). Revisa la dirección en Ajustes o prueba sin conexión.",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cargando(si: Boolean) {
        if (si) {
            binding.prgCargando.visibility = View.VISIBLE
        } else {
            binding.prgCargando.visibility = View.GONE
        }
        binding.btnEntrar.isEnabled = !si
    }

    private fun irAlMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }
}
