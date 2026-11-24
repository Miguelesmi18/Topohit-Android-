package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.Sonidos
import com.groundstudio.topohit.adapters.DificultadesAdapter
import com.groundstudio.topohit.api.ApiGetDificultades
import com.groundstudio.topohit.api.RetrofitGetDificultadesClient
import com.groundstudio.topohit.datamodel.EstadoDificultad
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Elegir la dificultad del modo Infinito: Fácil, Normal o Difícil.
// Lo que está abierto y lo que lleva candado lo decide la API (GET api/sesiones/dificultades):
// Normal se abre con X puntos en Fácil, Difícil con Y puntos en Normal, y el tutor puede
// bloquear cualquiera desde el escritorio.
class DificultadActivity : PantallaBase()
{
    private lateinit var lstDificultades: RecyclerView
    private lateinit var adapter: DificultadesAdapter
    private lateinit var lblDificultadVacio: TextView
    private lateinit var prgDificultadCargando: ProgressBar
    private var dificultades: List<EstadoDificultad> = listOf()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dificultad)

        lstDificultades       = findViewById<RecyclerView>(R.id.LstDificultades)
        lblDificultadVacio    = findViewById<TextView>(R.id.LblDificultadVacio)
        prgDificultadCargando = findViewById<ProgressBar>(R.id.PrgDificultadCargando)
        val btnDificultadAtras = findViewById<TextView>(R.id.BtnDificultadAtras)

        val tema = GestorAjustes.tema
        findViewById<ImageView>(R.id.ImgDificultadFondo).setImageResource(tema.fondo)
        btnDificultadAtras.setBackgroundResource(tema.circulo)

        // Las 3 dificultades una al lado de otra
        // Esta parte es de información externa que no sale en los vídeos
        // (GridLayoutManager: el vídeo 19 solo nombra la «grid», no la programa)
        adapter = DificultadesAdapter(this, dificultades)
        lstDificultades.layoutManager = GridLayoutManager(this, 3)
        lstDificultades.adapter = adapter

        adapter.setOnClickListener()
        {
            // getChildAdapterPosition(it): la posición en la lista de la fila que se ha pulsado (it es la fila)
            elegir(lstDificultades.getChildAdapterPosition(it))
        }

        btnDificultadAtras.setOnClickListener()
        {
            finish()
        }

        lblDificultadVacio.setOnClickListener()
        {
            getDificultades()
        }
    }

    // En onResume para que el récord y los candados se actualicen al volver de una partida
    override fun onResume()
    {
        super.onResume()
        getDificultades()
    }

    private fun elegir(posicion: Int)
    {
        if(posicion >= 0 && posicion < dificultades.size)
        {
            val elegida = dificultades[posicion]
            if(elegida.abierta)
            {
                val intent = Intent(this, PartidaActivity::class.java)
                intent.putExtra(PartidaActivity.partidaConstants.DIFICULTAD, elegida)
                startActivity(intent)
            }
            else
            {
                // Con candado: suena el «fallo» y se recuerda qué falta (está escrito debajo del candado)
                Sonidos.efecto(Sonidos.FALLO)
                Avisos.toast(this, R.string.dificultad_cerrada)
            }
        }
    }

    fun getDificultades()
    {
        prgDificultadCargando.visibility = View.VISIBLE
        lblDificultadVacio.visibility = View.GONE
        val token = GestorSesion.cabecera(this)
        val dificultadesApi = RetrofitGetDificultadesClient.getInstance().create(ApiGetDificultades::class.java)

        // GlobalScope.launch: lo de dentro se ejecuta en segundo plano. Las llamadas a la API tardan y,
        // si se hicieran en el hilo de la pantalla, la app se congelaría (Android no lo deja).
        // Dentro, call.execute() espera la respuesta y response.body() es el JSON ya pasado a objetos (null si hay error).
        GlobalScope.launch()
        {
            try
            {
                val call = dificultadesApi.getDificultades(token)
                val response = call.execute()
                val local = response.body()

                if(local != null)
                {
                    // runOnUiThread: las vistas solo se pueden cambiar desde el hilo de la pantalla, no desde el launch.
                    // pantallaActiva() comprueba que el niño no ha salido de la pantalla mientras llegaba la respuesta.
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            prgDificultadCargando.visibility = View.GONE
                            dificultades = local
                            adapter.setDificultades(dificultades)
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
                            prgDificultadCargando.visibility = View.GONE
                            lblDificultadVacio.visibility = View.VISIBLE
                            Avisos.errorApi(this@DificultadActivity, codigo)
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
                        prgDificultadCargando.visibility = View.GONE
                        lblDificultadVacio.visibility = View.VISIBLE
                        Avisos.errorConexion(this@DificultadActivity)
                    }
                }
            }
        }
    }
}
