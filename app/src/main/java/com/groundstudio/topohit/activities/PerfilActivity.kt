package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.Avatares
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.adapters.AvataresAdapter
import com.groundstudio.topohit.adapters.HistorialAdapter
import com.groundstudio.topohit.api.ApiGetPartidas
import com.groundstudio.topohit.api.ApiSetAvatar
import com.groundstudio.topohit.api.RetrofitGetPartidasClient
import com.groundstudio.topohit.api.RetrofitSetAvatarClient
import com.groundstudio.topohit.datamodel.AvatarPeticion
import com.groundstudio.topohit.datamodel.Partida
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Perfil: avatar, resumen (partidas, récord, precisión y tiempo jugado) y el historial de
// partidas de Infinito (GET api/sesiones/mias). Desde aquí se cambia el avatar (PUT api/auth/avatar).
class PerfilActivity : PantallaBase()
{
    private lateinit var lstPerfilHistorial: RecyclerView
    private lateinit var lstPerfilAvatares: RecyclerView
    private lateinit var historialAdapter: HistorialAdapter
    private lateinit var avataresAdapter: AvataresAdapter
    private lateinit var lblPerfilVacio: TextView
    private lateinit var layPerfilAvatares: FrameLayout
    private lateinit var btnPerfilGuardarAvatar: TextView
    private var partidas: List<Partida> = listOf()

    // El modo que se enseña en el historial (las partidas antiguas de otros modos no salen)
    private val MODO_INFINITO = "Infinito"

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        lstPerfilHistorial     = findViewById<RecyclerView>(R.id.LstPerfilHistorial)
        lstPerfilAvatares      = findViewById<RecyclerView>(R.id.LstPerfilAvatares)
        lblPerfilVacio         = findViewById<TextView>(R.id.LblPerfilVacio)
        layPerfilAvatares      = findViewById<FrameLayout>(R.id.LayPerfilAvatares)
        btnPerfilGuardarAvatar = findViewById<TextView>(R.id.BtnPerfilGuardarAvatar)

        pintarTema()
        findViewById<TextView>(R.id.LblPerfilNombre).text = GestorSesion.getUsername(this)
        findViewById<ImageView>(R.id.ImgPerfilAvatar).setImageResource(Avatares.dibujo(GestorSesion.getAvatar(this)))

        historialAdapter = HistorialAdapter(this, partidas)
        lstPerfilHistorial.layoutManager = LinearLayoutManager(this)
        lstPerfilHistorial.adapter = historialAdapter

        // Rejilla para cambiar el avatar
        // Esta parte es de información externa que no sale en los vídeos
        // (GridLayoutManager: el vídeo 19 solo nombra la «grid», no la programa)
        avataresAdapter = AvataresAdapter(this, GestorSesion.getAvatar(this))
        lstPerfilAvatares.layoutManager = GridLayoutManager(this, 6)
        lstPerfilAvatares.adapter = avataresAdapter

        avataresAdapter.setOnClickListener()
        {
            // getChildAdapterPosition(it): la posición en la lista de la fila que se ha pulsado (it es la fila)
            avataresAdapter.setElegido(lstPerfilAvatares.getChildAdapterPosition(it) + 1)
        }

        findViewById<TextView>(R.id.BtnPerfilAtras).setOnClickListener()
        {
            finish()
        }

        findViewById<TextView>(R.id.BtnPerfilEstadisticas).setOnClickListener()
        {
            startActivity(Intent(this, MetricasActivity::class.java))
        }

        findViewById<TextView>(R.id.BtnPerfilCambiarAvatar).setOnClickListener()
        {
            avataresAdapter.setElegido(GestorSesion.getAvatar(this))
            layPerfilAvatares.visibility = View.VISIBLE
        }

        findViewById<TextView>(R.id.BtnPerfilCerrarAvatares).setOnClickListener()
        {
            layPerfilAvatares.visibility = View.GONE
        }

        btnPerfilGuardarAvatar.setOnClickListener()
        {
            guardarAvatar()
        }

        findViewById<TextView>(R.id.BtnPerfilCerrarSesion).setOnClickListener()
        {
            Avisos.volverAlLogin(this)
        }

        getPartidas()
    }

    private fun pintarTema()
    {
        val tema = GestorAjustes.tema
        val color = ContextCompat.getColor(this, tema.colorTexto)
        findViewById<ImageView>(R.id.ImgPerfilFondo).setImageResource(tema.fondo)
        findViewById<TextView>(R.id.BtnPerfilAtras).setBackgroundResource(tema.circulo)
        findViewById<ImageView>(R.id.ImgPerfilAvatar).setBackgroundResource(tema.circulo)
        findViewById<LinearLayout>(R.id.LayPerfilJugador).setBackgroundResource(tema.panel)
        findViewById<LinearLayout>(R.id.LayPerfilHistorial).setBackgroundResource(tema.panel)
        findViewById<LinearLayout>(R.id.LayPerfilPanelAvatares).setBackgroundResource(tema.panel)
        val btnPerfilCambiarAvatar = findViewById<TextView>(R.id.BtnPerfilCambiarAvatar)
        btnPerfilCambiarAvatar.setBackgroundResource(tema.botonSecundario)
        btnPerfilCambiarAvatar.setTextColor(ContextCompat.getColor(this, tema.colorTextoBotonSecundario))
        val btnPerfilEstadisticas = findViewById<TextView>(R.id.BtnPerfilEstadisticas)
        btnPerfilEstadisticas.setBackgroundResource(tema.botonPrincipal)
        btnPerfilEstadisticas.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
        btnPerfilGuardarAvatar.setBackgroundResource(tema.botonPrincipal)
        btnPerfilGuardarAvatar.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
        val textos = listOf<TextView>(findViewById(R.id.LblPerfilNombre), findViewById(R.id.LblPerfilHistorialTitulo),
                                      findViewById(R.id.LblPerfilElige), lblPerfilVacio)
        for(texto in textos)
        {
            texto.setTextColor(color)
        }
    }

    // PUT api/auth/avatar. Si la API dice que sí, se cambia también en la tablet (sesion.txt).
    private fun guardarAvatar()
    {
        btnPerfilGuardarAvatar.isEnabled = false
        val token = GestorSesion.cabecera(this)
        val elegido = avataresAdapter.getElegido()
        val avatarApi = RetrofitSetAvatarClient.getInstance().create(ApiSetAvatar::class.java)

        // GlobalScope.launch: lo de dentro se ejecuta en segundo plano. Las llamadas a la API tardan y,
        // si se hicieran en el hilo de la pantalla, la app se congelaría (Android no lo deja).
        // Dentro, call.execute() espera la respuesta y response.body() es el JSON ya pasado a objetos (null si hay error).
        GlobalScope.launch()
        {
            try
            {
                val call = avatarApi.setAvatar(token, AvatarPeticion(elegido))
                val response = call.execute()
                val respuesta = response.body()

                if(respuesta != null)
                {
                    // runOnUiThread: las vistas solo se pueden cambiar desde el hilo de la pantalla, no desde el launch.
                    // pantallaActiva() comprueba que el niño no ha salido de la pantalla mientras llegaba la respuesta.
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            btnPerfilGuardarAvatar.isEnabled = true
                            GestorSesion.cambiarAvatar(this@PerfilActivity, respuesta.avatarId)
                            findViewById<ImageView>(R.id.ImgPerfilAvatar).setImageResource(Avatares.dibujo(respuesta.avatarId))
                            layPerfilAvatares.visibility = View.GONE
                            Avisos.bien(this@PerfilActivity, R.string.avatar_cambiado)
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
                            btnPerfilGuardarAvatar.isEnabled = true
                            Avisos.errorApi(this@PerfilActivity, codigo)
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
                        btnPerfilGuardarAvatar.isEnabled = true
                        Avisos.errorConexion(this@PerfilActivity)
                    }
                }
            }
        }
    }

    // GET api/sesiones/mias: el historial (solo las de Infinito) y el resumen de arriba
    private fun getPartidas()
    {
        val token = GestorSesion.cabecera(this)
        val partidasApi = RetrofitGetPartidasClient.getInstance().create(ApiGetPartidas::class.java)

        // GlobalScope.launch: lo de dentro se ejecuta en segundo plano. Las llamadas a la API tardan y,
        // si se hicieran en el hilo de la pantalla, la app se congelaría (Android no lo deja).
        // Dentro, call.execute() espera la respuesta y response.body() es el JSON ya pasado a objetos (null si hay error).
        GlobalScope.launch()
        {
            try
            {
                val call = partidasApi.getPartidas(token)
                val response = call.execute()
                val local = response.body()

                if(local != null)
                {
                    val infinito = mutableListOf<Partida>()
                    for(partida in local)
                    {
                        if(partida.modo == MODO_INFINITO)
                        {
                            infinito.add(partida)
                        }
                    }
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            partidas = infinito
                            historialAdapter.setPartidas(partidas)
                            pintarResumen()
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
                            lblPerfilVacio.text = getString(R.string.error_cargar)
                            Avisos.errorApi(this@PerfilActivity, codigo)
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
                        lblPerfilVacio.text = getString(R.string.error_conexion)
                    }
                }
            }
        }
    }

    private fun pintarResumen()
    {
        if(partidas.size == 0)
        {
            lblPerfilVacio.text = getString(R.string.perfil_sin_partidas)
        }
        else
        {
            lblPerfilVacio.visibility = View.GONE
        }

        var record = 0
        var aciertos = 0
        var intentos = 0
        var segundos = 0
        for(partida in partidas)
        {
            if(!partida.abandoned && partida.finalScore > record)
            {
                record = partida.finalScore
            }
            aciertos = aciertos + partida.hits
            intentos = intentos + partida.nAttempts
            segundos = segundos + partida.sessionLength
        }

        findViewById<TextView>(R.id.LblPerfilPartidas).text = partidas.size.toString()
        findViewById<TextView>(R.id.LblPerfilRecord).text = record.toString()
        findViewById<TextView>(R.id.LblPerfilMinutos).text = (segundos / 60).toString()
        if(intentos > 0)
        {
            findViewById<TextView>(R.id.LblPerfilPrecision).text = getString(R.string.porcentaje, aciertos * 100 / intentos)
        }
        else
        {
            findViewById<TextView>(R.id.LblPerfilPrecision).text = getString(R.string.sin_dato)
        }
    }
}
