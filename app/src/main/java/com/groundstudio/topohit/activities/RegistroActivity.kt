package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.api.RetrofitClient
import com.groundstudio.topohit.data_model.RegistroPeticion
import com.groundstudio.topohit.databinding.ActivityRegistroBinding
import com.groundstudio.topohit.util.Almacen
import com.groundstudio.topohit.util.Cifrado
import com.groundstudio.topohit.util.Avatares
import com.groundstudio.topohit.util.Juego
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Crear cuenta (POST api/auth/registro). Si va bien, la API ya devuelve el token y entramos.
class RegistroActivity : PantallaBase() {

    private lateinit var binding: ActivityRegistroBinding
    private var avatarElegido = 1          // CAMBIO 19/11: id del avatar (1 a 12)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.layPanelDatos.setBackgroundResource(tema.panel)
        binding.layPanelAvatar.setBackgroundResource(tema.panel)
        binding.btnCrear.setBackgroundResource(tema.botonPrincipal)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        binding.lblAvatar.setTextColor(color)
        binding.lblNota.setTextColor(color)
        binding.lblNotaAvatar.setTextColor(color)

        // Rejilla de avatares: al tocar uno se marca
        Avatares.pintar(binding.selAvatares, avatarElegido)
        val imagenes = Avatares.imagenes(binding.selAvatares)
        for (i in 0..imagenes.size - 1) {
            imagenes[i].setOnClickListener {
                avatarElegido = i + 1              // la imagen i es el avatar i + 1
                Avatares.pintar(binding.selAvatares, avatarElegido)
            }
        }

        binding.btnAtras.setOnClickListener {
            finish()
        }
        binding.btnCrear.setOnClickListener {
            crearCuenta()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun crearCuenta() {
        val usuario = binding.txtUsuario.text.toString().trim()
        val contrasena = binding.txtContrasena.text.toString()
        val repetir = binding.txtRepetir.text.toString()

        // Las mismas reglas que comprueba la API
        if (usuario.length < 3) {
            Toast.makeText(this, "El usuario tiene que tener 3 letras o más", Toast.LENGTH_LONG).show()
            return
        }
        if (contrasena.length < 4) {
            Toast.makeText(this, "La contraseña tiene que tener 4 caracteres o más", Toast.LENGTH_LONG).show()
            return
        }
        if (contrasena != repetir) {
            Toast.makeText(this, "Las contraseñas no son iguales", Toast.LENGTH_LONG).show()
            return
        }

        binding.prgCargando.visibility = View.VISIBLE
        binding.btnCrear.isEnabled = false
        GlobalScope.launch {
            try {
                // La contraseña se cifra aquí, dentro de la corrutina: bcrypt tarda un poco
                // y así la pantalla no se queda congelada
                val hash = Cifrado.hash(contrasena)
                val response = RetrofitClient.api().registro(RegistroPeticion(usuario, hash, avatarElegido)).execute()
                val respuesta = response.body()
                runOnUiThread {
                    binding.prgCargando.visibility = View.GONE
                    binding.btnCrear.isEnabled = true
                    if (response.isSuccessful && respuesta != null) {
                        Juego.token = respuesta.token
                        Juego.idUsuario = respuesta.idUsuario
                        Juego.username = respuesta.username
                        Juego.rol = respuesta.rol
                        Juego.avatar = respuesta.avatarId    // CAMBIO 19/11: el que ha guardado la API
                        Juego.progreso = null
                        Almacen.guardarSesion(this@RegistroActivity)
                        Toast.makeText(this@RegistroActivity, "¡Cuenta creada!", Toast.LENGTH_SHORT).show()
                        // Vamos al menú y cerramos el login que había debajo
                        val intent = Intent(this@RegistroActivity, MenuActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@RegistroActivity, RetrofitClient.mensajeDeError(response), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.prgCargando.visibility = View.GONE
                    binding.btnCrear.isEnabled = true
                    Toast.makeText(this@RegistroActivity, "No se puede conectar con la API", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
