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
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.Cifrado
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.api.ApiLogin
import com.groundstudio.topohit.api.RetrofitLoginClient
import com.groundstudio.topohit.datamodel.LoginPeticion
import com.groundstudio.topohit.datamodel.SesionUsuario
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Pantalla de inicio: usuario y contraseña contra la API (POST api/auth/login).
// Si ya había entrado antes en esta tablet, va directa al menú.
class LoginActivity : PantallaBase()
{
    private lateinit var txtLoginUsuario: EditText
    private lateinit var txtLoginContrasena: EditText
    private lateinit var btnLoginEntrar: TextView
    private lateinit var prgLoginCargando: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(GestorSesion.getSesion(this) != null)
        {
            irAlMenu()
        }
        else
        {
            txtLoginUsuario    = findViewById<EditText>(R.id.TxtLoginUsuario)
            txtLoginContrasena = findViewById<EditText>(R.id.TxtLoginContrasena)
            btnLoginEntrar     = findViewById<TextView>(R.id.BtnLoginEntrar)
            prgLoginCargando   = findViewById<ProgressBar>(R.id.PrgLoginCargando)
            val btnLoginRegistro = findViewById<TextView>(R.id.BtnLoginRegistro)
            val btnLoginAjustes  = findViewById<TextView>(R.id.BtnLoginAjustes)

            btnLoginEntrar.setOnClickListener()
            {
                entrar()
            }

            btnLoginRegistro.setOnClickListener()
            {
                startActivity(Intent(this, RegistroActivity::class.java))
            }

            btnLoginAjustes.setOnClickListener()
            {
                startActivity(Intent(this, AjustesActivity::class.java))
            }
        }
    }

    // Al volver de Ajustes puede haber cambiado la temática
    override fun onResume()
    {
        super.onResume()
        if(GestorSesion.getSesion(this) == null)
        {
            pintarTema()
        }
    }

    private fun pintarTema()
    {
        val tema = GestorAjustes.tema
        val color = ContextCompat.getColor(this, tema.colorTexto)
        findViewById<ImageView>(R.id.ImgLoginFondo).setImageResource(tema.fondo)
        findViewById<ImageView>(R.id.ImgLoginLogo).setImageResource(tema.logo)
        findViewById<ImageView>(R.id.ImgLoginTopo).setImageResource(tema.topoNormal)
        findViewById<LinearLayout>(R.id.LayLoginPanel).setBackgroundResource(tema.panel)
        findViewById<TextView>(R.id.LblLoginTitulo).setTextColor(color)
        findViewById<TextView>(R.id.LblLoginNoCuenta).setTextColor(color)
        val btnLoginRegistro = findViewById<TextView>(R.id.BtnLoginRegistro)
        btnLoginRegistro.setBackgroundResource(tema.botonSecundario)
        btnLoginRegistro.setTextColor(ContextCompat.getColor(this, tema.colorTextoBotonSecundario))
        findViewById<TextView>(R.id.BtnLoginAjustes).setBackgroundResource(tema.circulo)
        btnLoginEntrar.setBackgroundResource(tema.botonPrincipal)
        btnLoginEntrar.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
    }

    private fun entrar()
    {
        val usuario = txtLoginUsuario.text.toString().trim()
        val contrasena = txtLoginContrasena.text.toString()

        if(usuario != "" && contrasena != "")
        {
            cargando(true)
            val loginApi = RetrofitLoginClient.getInstance().create(ApiLogin::class.java)

            // GlobalScope.launch: lo de dentro se ejecuta en segundo plano. Las llamadas a la API tardan y,
            // si se hicieran en el hilo de la pantalla, la app se congelaría (Android no lo deja).
            // Dentro, call.execute() espera la respuesta y response.body() es el JSON ya pasado a objetos (null si hay error).
            GlobalScope.launch()
            {
                try
                {
                    // La contraseña se cifra aquí, dentro de la corrutina: bcrypt tarda un poco
                    // y así la pantalla no se queda congelada
                    val hash = Cifrado.hash(contrasena)
                    val call = loginApi.login(LoginPeticion(usuario, hash))
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
                                GestorSesion.guardar(this@LoginActivity,
                                                     SesionUsuario(respuesta.token, respuesta.idUsuario,
                                                                   respuesta.username, respuesta.rol, respuesta.avatarId))
                                irAlMenu()
                            }
                            else
                            {
                                // 401: usuario o contraseña incorrectos (aquí no hay sesión que caduque)
                                Avisos.toast(this@LoginActivity, R.string.error_login)
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
                            Avisos.errorConexion(this@LoginActivity)
                        }
                    }
                }
            }
        }
        else
        {
            Avisos.toast(this, R.string.error_login_vacio)
        }
    }

    private fun cargando(activo: Boolean)
    {
        if(activo)
        {
            prgLoginCargando.visibility = View.VISIBLE
        }
        else
        {
            prgLoginCargando.visibility = View.GONE
        }
        btnLoginEntrar.isEnabled = !activo
    }

    private fun irAlMenu()
    {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }
}
