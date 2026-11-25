package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.Cifrado
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.adapters.AvataresAdapter
import com.groundstudio.topohit.api.ApiRegistro
import com.groundstudio.topohit.api.RetrofitRegistroClient
import com.groundstudio.topohit.datamodel.RegistroPeticion
import com.groundstudio.topohit.datamodel.SesionUsuario
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Crear cuenta (POST api/auth/registro) eligiendo avatar.
// Si va bien, la API ya devuelve el token y se entra directamente en el menú.
class RegistroActivity : PantallaBase()
{
    private lateinit var txtRegistroUsuario: EditText
    private lateinit var txtRegistroContrasena: EditText
    private lateinit var txtRegistroRepetir: EditText
    private lateinit var btnRegistroCrear: TextView
    private lateinit var prgRegistroCargando: ProgressBar
    private lateinit var lstRegistroAvatares: RecyclerView
    private lateinit var adapter: AvataresAdapter

    // Mínimos que también comprueba la API
    private val MINIMO_USUARIO = 3
    private val MINIMO_CONTRASENA = 4

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        txtRegistroUsuario    = findViewById<EditText>(R.id.TxtRegistroUsuario)
        txtRegistroContrasena = findViewById<EditText>(R.id.TxtRegistroContrasena)
        txtRegistroRepetir    = findViewById<EditText>(R.id.TxtRegistroRepetir)
        btnRegistroCrear      = findViewById<TextView>(R.id.BtnRegistroCrear)
        prgRegistroCargando   = findViewById<ProgressBar>(R.id.PrgRegistroCargando)
        lstRegistroAvatares   = findViewById<RecyclerView>(R.id.LstRegistroAvatares)
        val btnRegistroAtras  = findViewById<TextView>(R.id.BtnRegistroAtras)

        pintarTema()

        // Rejilla de 6 columnas: dos filas de 6 avatares
        // Esta parte es de información externa que no sale en los vídeos
        // (GridLayoutManager: el vídeo 19 solo nombra la «grid», no la programa)
        adapter = AvataresAdapter(this, 1)
        lstRegistroAvatares.layoutManager = GridLayoutManager(this, 6)
        lstRegistroAvatares.adapter = adapter

        adapter.setOnClickListener()
        {
            // getChildAdapterPosition(it): la posición en la lista de la fila que se ha pulsado (it es la fila)
            adapter.setElegido(lstRegistroAvatares.getChildAdapterPosition(it) + 1)
        }

        btnRegistroAtras.setOnClickListener()
        {
            finish()
        }

        btnRegistroCrear.setOnClickListener()
        {
            crearCuenta()
        }
    }

    private fun pintarTema()
    {
        val tema = GestorAjustes.tema
        pintarTitulo(R.id.LblRegistroCabecera)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        findViewById<ImageView>(R.id.ImgRegistroFondo).setImageResource(tema.fondo)
        findViewById<LinearLayout>(R.id.LayRegistroDatos).setBackgroundResource(tema.panel)
        findViewById<LinearLayout>(R.id.LayRegistroAvatar).setBackgroundResource(tema.panel)
        findViewById<TextView>(R.id.BtnRegistroAtras).setBackgroundResource(tema.circulo)
        findViewById<TextView>(R.id.LblRegistroAvatar).setTextColor(color)
        findViewById<TextView>(R.id.LblRegistroNota).setTextColor(color)
        findViewById<TextView>(R.id.LblRegistroNotaAvatar).setTextColor(color)
        btnRegistroCrear.setBackgroundResource(tema.botonPrincipal)
        btnRegistroCrear.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
    }

    // Comprueba los datos antes de llamar a la API. Devuelve el texto del error o 0 si está bien.
    private fun comprobar(usuario: String, contrasena: String, repetir: String): Int
    {
        var error = 0
        if(usuario.length < MINIMO_USUARIO)
        {
            error = R.string.error_usuario_corto
        }
        else if(contrasena.length < MINIMO_CONTRASENA)
        {
            error = R.string.error_contrasena_corta
        }
        else if(contrasena != repetir)
        {
            error = R.string.error_contrasenas_distintas
        }
        return error
    }

    private fun crearCuenta()
    {
        val usuario = txtRegistroUsuario.text.toString().trim()
        val contrasena = txtRegistroContrasena.text.toString()
        val repetir = txtRegistroRepetir.text.toString()
        val avatar = adapter.getElegido()

        val error = comprobar(usuario, contrasena, repetir)
        if(error == 0)
        {
            cargando(true)
            val registroApi = RetrofitRegistroClient.getInstance().create(ApiRegistro::class.java)

            // GlobalScope.launch: lo de dentro se ejecuta en segundo plano. Las llamadas a la API tardan y,
            // si se hicieran en el hilo de la pantalla, la app se congelaría (Android no lo deja).
            // Dentro, call.execute() espera la respuesta y response.body() es el JSON ya pasado a objetos (null si hay error).
            GlobalScope.launch()
            {
                try
                {
                    val hash = Cifrado.hash(contrasena)
                    val call = registroApi.registro(RegistroPeticion(usuario, hash, avatar))
                    val response = call.execute()
                    val respuesta = response.body()

                    // runOnUiThread: las vistas solo se pueden cambiar desde el hilo de la pantalla, no desde el launch.
                    // pantallaActiva() comprueba que el niño no ha salido de la pantalla mientras llegaba la respuesta.
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            cargando(false)
                            if(respuesta != null)
                            {
                                GestorSesion.guardar(this@RegistroActivity,
                                                     SesionUsuario(respuesta.token, respuesta.idUsuario,
                                                                   respuesta.username, respuesta.rol, respuesta.avatarId))
                                Avisos.bien(this@RegistroActivity, R.string.cuenta_creada)
                                val intent = Intent(this@RegistroActivity, MenuActivity::class.java)
                                // Esta parte es de información externa que no sale en los vídeos
                                // (las flags cierran también el login que había debajo)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            else
                            {
                                // Los datos ya se han comprobado: si la API dice que no, es que el usuario ya existe
                                Avisos.toast(this@RegistroActivity, R.string.error_usuario_existe)
                            }
                        }
                    }
                }
                catch(e: Exception)
                {
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            cargando(false)
                            Avisos.errorConexion(this@RegistroActivity)
                        }
                    }
                }
            }
        }
        else
        {
            Avisos.toast(this, error)
        }
    }

    private fun cargando(activo: Boolean)
    {
        if(activo)
        {
            prgRegistroCargando.visibility = View.VISIBLE
        }
        else
        {
            prgRegistroCargando.visibility = View.GONE
        }
        btnRegistroCrear.isEnabled = !activo
    }
}
