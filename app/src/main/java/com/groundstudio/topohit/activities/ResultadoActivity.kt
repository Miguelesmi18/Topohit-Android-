package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.Sonidos
import com.groundstudio.topohit.api.ApiAddSesion
import com.groundstudio.topohit.api.ApiGetDificultades
import com.groundstudio.topohit.api.RetrofitAddSesionClient
import com.groundstudio.topohit.api.RetrofitGetDificultadesClient
import com.groundstudio.topohit.datamodel.EstadoDificultad
import com.groundstudio.topohit.datamodel.SesionNueva
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Fin de la partida: «¡Se acabaron las vidas!», los puntos y los datos de la partida.
// Aquí se guarda la partida en la API (POST api/sesiones) y, después, se vuelven a pedir las
// dificultades (REGLA-0489-074: recargar para comprobar) para saber si con esta partida se ha
// abierto la siguiente. Las dos llamadas van una detrás de otra.
class ResultadoActivity : PantallaBase()
{
    // Claves de los extras que recibe esta pantalla (REGLA-0489-042)
    object resultadoConstants
    {
        const val SESION = "SESION"
        const val DIFICULTAD = "DIFICULTAD"
    }

    private lateinit var sesion: SesionNueva
    private lateinit var dificultad: EstadoDificultad
    private lateinit var lblResultadoGuardado: TextView
    private lateinit var lblResultadoDesbloqueo: TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado)

        sesion = intent.getSerializableExtra(resultadoConstants.SESION) as SesionNueva
        dificultad = intent.getSerializableExtra(resultadoConstants.DIFICULTAD) as EstadoDificultad
        lblResultadoGuardado   = findViewById<TextView>(R.id.LblResultadoGuardado)
        lblResultadoDesbloqueo = findViewById<TextView>(R.id.LblResultadoDesbloqueo)

        pintarTema()
        pintarDatos()

        findViewById<TextView>(R.id.BtnResultadoRepetir).setOnClickListener()
        {
            val otra = Intent(this, PartidaActivity::class.java)
            otra.putExtra(PartidaActivity.partidaConstants.DIFICULTAD, dificultad)
            startActivity(otra)
            finish()
        }

        // Vuelve a la pantalla de dificultades, que está debajo
        findViewById<TextView>(R.id.BtnResultadoDificultades).setOnClickListener()
        {
            finish()
        }

        findViewById<TextView>(R.id.BtnResultadoMenu).setOnClickListener()
        {
            val menu = Intent(this, MenuActivity::class.java)
            // Esta parte es de información externa que no sale en los vídeos
            // (CLEAR_TOP vuelve al menú que ya estaba abierto y cierra las pantallas de encima)
            menu.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(menu)
            finish()
        }

        // Hasta que la partida no esté guardada no se puede seguir (si no, la pantalla de
        // dificultades podría pedir los datos antes de que la API los tenga)
        activarBotones(false)
        guardarPartida()
    }

    private fun activarBotones(activos: Boolean)
    {
        findViewById<TextView>(R.id.BtnResultadoRepetir).isEnabled = activos
        findViewById<TextView>(R.id.BtnResultadoDificultades).isEnabled = activos
        findViewById<TextView>(R.id.BtnResultadoMenu).isEnabled = activos
    }

    private fun pintarTema()
    {
        val tema = GestorAjustes.tema
        val color = ContextCompat.getColor(this, tema.colorTexto)
        findViewById<ImageView>(R.id.ImgResultadoFondo).setImageResource(tema.fondo)
        findViewById<ImageView>(R.id.ImgResultadoTopo).setImageResource(tema.topoBurlon)
        findViewById<LinearLayout>(R.id.LayResultadoPanel).setBackgroundResource(tema.panel)
        findViewById<TextView>(R.id.BtnResultadoRepetir).setBackgroundResource(tema.botonPrincipal)
        findViewById<TextView>(R.id.BtnResultadoDificultades).setBackgroundResource(tema.botonSecundario)
        findViewById<TextView>(R.id.BtnResultadoMenu).setBackgroundResource(tema.botonSecundario)
        findViewById<TextView>(R.id.LblResultadoTitulo).setTextColor(color)
        findViewById<TextView>(R.id.LblResultadoPuntos).setTextColor(color)
        findViewById<TextView>(R.id.LblResultadoSubtitulo).setTextColor(color)
        lblResultadoGuardado.setTextColor(color)
    }

    private fun pintarDatos()
    {
        findViewById<TextView>(R.id.LblResultadoPuntos).text = getString(R.string.puntos_cantidad, sesion.finalScore)
        findViewById<TextView>(R.id.LblResultadoAciertos).text = sesion.hits.toString()
        findViewById<TextView>(R.id.LblResultadoFallos).text = sesion.errorsMade.toString()
        findViewById<TextView>(R.id.LblResultadoEscapados).text = sesion.molesEscaped.toString()
        findViewById<TextView>(R.id.LblResultadoDorados).text = sesion.goldenHits.toString()
        findViewById<TextView>(R.id.LblResultadoRacha).text = sesion.maxCombo.toString()

        // Récord: si no había jugado nunca o ha superado el anterior, ¡nuevo récord!
        val lblResultadoSubtitulo = findViewById<TextView>(R.id.LblResultadoSubtitulo)
        val record = dificultad.record
        if(record != null && sesion.finalScore == record)
        {
            lblResultadoSubtitulo.text = getString(R.string.resultado_igualado)
        }
        else if(record == null || sesion.finalScore > record)
        {
            findViewById<TextView>(R.id.LblResultadoNuevoRecord).visibility = View.VISIBLE
            lblResultadoSubtitulo.text = getString(R.string.resultado_mejor)
            Sonidos.efecto(Sonidos.VICTORIA)
        }
        else
        {
            lblResultadoSubtitulo.text = getString(R.string.resultado_record, record, record - sesion.finalScore)
        }
    }

    // POST api/sesiones. Si va bien, se piden otra vez las dificultades para ver si se ha abierto alguna.
    private fun guardarPartida()
    {
        val token = GestorSesion.cabecera(this)
        val sesionApi = RetrofitAddSesionClient.getInstance().create(ApiAddSesion::class.java)

        GlobalScope.launch()
        {
            try
            {
                val call = sesionApi.addSesion(token, sesion)
                val response = call.execute()
                val creada = response.body()

                if(creada != null)
                {
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            lblResultadoGuardado.text = getString(R.string.resultado_guardada)
                        }
                    }
                    comprobarDesbloqueo(token)
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            activarBotones(true)
                        }
                    }
                }
                else
                {
                    // Esta parte es de información externa que no sale en los vídeos
                    // (response.code() es el código HTTP con el que ha respondido la API)
                    val codigo = response.code()
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            lblResultadoGuardado.text = getString(R.string.error_guardar_partida)
                            activarBotones(true)
                            Avisos.errorApi(this@ResultadoActivity, codigo)
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
                        lblResultadoGuardado.text = getString(R.string.error_guardar_partida)
                        activarBotones(true)
                        Avisos.errorConexion(this@ResultadoActivity)
                    }
                }
            }
        }
    }

    // Se llama desde la corrutina de guardarPartida, después del POST (una llamada detrás de otra).
    // Si la dificultad siguiente estaba cerrada por puntos y ahora está abierta, se celebra.
    private fun comprobarDesbloqueo(token: String)
    {
        val dificultadesApi = RetrofitGetDificultadesClient.getInstance().create(ApiGetDificultades::class.java)
        try
        {
            val call = dificultadesApi.getDificultades(token)
            val response = call.execute()
            val estados = response.body()
            if(estados != null)
            {
                // Récord que tenía en esta dificultad ANTES de la partida
                var recordAntes = 0
                val record = dificultad.record
                if(record != null)
                {
                    recordAntes = record
                }

                var abierta = ""
                for(estado in estados)
                {
                    val esLaSiguiente = estado.dificultadAnterior == dificultad.nombre
                    val antesCerrada = recordAntes < estado.puntosDesbloqueo
                    val laAbreEsta = sesion.finalScore >= estado.puntosDesbloqueo
                    if(esLaSiguiente && estado.abierta && antesCerrada && laAbreEsta)
                    {
                        abierta = estado.nombre
                    }
                }
                if(abierta != "")
                {
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            lblResultadoDesbloqueo.text = getString(R.string.resultado_desbloqueada, abierta)
                            lblResultadoDesbloqueo.visibility = View.VISIBLE
                            Sonidos.efecto(Sonidos.COMBO)
                        }
                    }
                }
            }
        }
        catch(e: Exception)
        {
            // Sin conexión: no se puede saber si se ha abierto; lo verá en la pantalla de dificultades
        }
    }
}
