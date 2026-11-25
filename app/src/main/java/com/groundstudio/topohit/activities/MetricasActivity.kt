package com.groundstudio.topohit.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.adapters.GraficasAdapter
import com.groundstudio.topohit.api.ApiGetPartidas
import com.groundstudio.topohit.api.RetrofitGetPartidasClient
import com.groundstudio.topohit.datamodel.Grafica
import com.groundstudio.topohit.datamodel.MetricasJugador
import com.groundstudio.topohit.datamodel.ModeloAbandono
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Mis estadísticas (la pestaña «Mètriques» del reto de Métricas): las métricas propias del niño.
// 1. Pide sus partidas a la API (GET api/sesiones/mias).
// 2. Se las pasa en JSON al script de Python estadisticas_jugador.py (Chaquopy).
// 3. Python devuelve los números y las gráficas de Matplotlib en base64, y aquí se pintan.
// 4. Después, modelo_abandono.py entrena el árbol de decisión (¿volverá a jugar?) con las sesiones
//    simuladas y devuelve accuracy, precision, recall, ROC-AUC y sus gráficas, que van detrás.
// Las estadísticas de todos los niños están en el escritorio.
class MetricasActivity : PantallaBase()
{
    private lateinit var lstMetricasGraficas: RecyclerView
    private lateinit var adapter: GraficasAdapter
    private lateinit var lblMetricasEstado: TextView
    private lateinit var btnMetricasReintentar: TextView
    private var graficas: List<Grafica> = listOf()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metricas)

        lstMetricasGraficas   = findViewById<RecyclerView>(R.id.LstMetricasGraficas)
        lblMetricasEstado     = findViewById<TextView>(R.id.LblMetricasEstado)
        btnMetricasReintentar = findViewById<TextView>(R.id.BtnMetricasReintentar)

        pintarTema()

        // Esta parte es de información externa que no sale en los vídeos
        // (GridLayoutManager: dos gráficas por fila; el vídeo 19 solo nombra la «grid»)
        adapter = GraficasAdapter(this, graficas)
        lstMetricasGraficas.layoutManager = GridLayoutManager(this, 2)
        lstMetricasGraficas.adapter = adapter

        // Esta parte es de información externa que no sale en los vídeos
        // (Chaquopy: Python se arranca una sola vez antes de llamar a un script)
        if(!Python.isStarted())
        {
            Python.start(AndroidPlatform(this))
        }

        findViewById<TextView>(R.id.BtnMetricasAtras).setOnClickListener()
        {
            finish()
        }

        btnMetricasReintentar.setOnClickListener()
        {
            getEstadisticas()
        }

        getEstadisticas()
    }

    private fun pintarTema()
    {
        val tema = GestorAjustes.tema
        pintarTitulo(R.id.LblMetricasCabecera)
        findViewById<ImageView>(R.id.ImgMetricasFondo).setImageResource(tema.fondo)
        findViewById<TextView>(R.id.BtnMetricasAtras).setBackgroundResource(tema.circulo)
        findViewById<LinearLayout>(R.id.LayMetricasPanel).setBackgroundResource(tema.panel)
        btnMetricasReintentar.setBackgroundResource(tema.botonPrincipal)
        btnMetricasReintentar.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
        lblMetricasEstado.setTextColor(ContextCompat.getColor(this, tema.colorTexto))
    }

    // GET api/sesiones/mias y, con sus partidas, el script de Python
    private fun getEstadisticas()
    {
        btnMetricasReintentar.visibility = View.GONE
        lblMetricasEstado.visibility = View.VISIBLE
        lblMetricasEstado.text = getString(R.string.preparando_estadisticas)
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
                    val metricas = calcular(Gson().toJson(local))
                    val modelo = calcularModelo()
                    // runOnUiThread: las vistas solo se pueden cambiar desde el hilo de la pantalla, no desde el launch.
                    // pantallaActiva() comprueba que el niño no ha salido de la pantalla mientras llegaba la respuesta.
                    runOnUiThread()
                    {
                        if(pantallaActiva())
                        {
                            if(metricas != null && modelo != null)
                            {
                                pintarMetricas(metricas, modelo)
                            }
                            else
                            {
                                mostrarError(getString(R.string.error_estadisticas))
                            }
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
                            mostrarError(getString(R.string.error_cargar))
                            Avisos.errorApi(this@MetricasActivity, codigo)
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
                        mostrarError(getString(R.string.error_conexion))
                    }
                }
            }
        }
    }

    // Llama a generar() de estadisticas_jugador.py con las partidas en JSON.
    // Devuelve null si Python falla (así la pantalla deja reintentar).
    private fun calcular(jsonPartidas: String): MetricasJugador?
    {
        var metricas: MetricasJugador? = null
        try
        {
            // Esta parte es de información externa que no sale en los vídeos
            // (Chaquopy: getModule carga el script y callAttr llama a una de sus funciones)
            val modulo = Python.getInstance().getModule("estadisticas_jugador")
            val resultado = modulo.callAttr("generar", jsonPartidas).toString()
            metricas = Gson().fromJson(resultado, MetricasJugador::class.java)
        }
        catch(e: Exception)
        {
            metricas = null
        }
        return metricas
    }

    // Llama a generar() de modelo_abandono.py. Devuelve null si Python falla.
    private fun calcularModelo(): ModeloAbandono?
    {
        var modelo: ModeloAbandono? = null
        try
        {
            // Esta parte es de información externa que no sale en los vídeos
            // (Chaquopy: getModule carga el script y callAttr llama a una de sus funciones)
            val modulo = Python.getInstance().getModule("modelo_abandono")
            val resultado = modulo.callAttr("generar").toString()
            modelo = Gson().fromJson(resultado, ModeloAbandono::class.java)
        }
        catch(e: Exception)
        {
            modelo = null
        }
        return modelo
    }

    private fun pintarMetricas(metricas: MetricasJugador, modelo: ModeloAbandono)
    {
        // Primero las gráficas del niño y detrás las del modelo
        val todas = mutableListOf<Grafica>()
        todas.addAll(metricas.graficas)
        todas.addAll(modelo.graficas)
        graficas = todas
        adapter.setGraficas(graficas)

        findViewById<TextView>(R.id.LblMetricasAccuracy).text = modelo.accuracy.toString()
        findViewById<TextView>(R.id.LblMetricasPrecisionModelo).text = modelo.precision.toString()
        findViewById<TextView>(R.id.LblMetricasRecall).text = modelo.recall.toString()
        findViewById<TextView>(R.id.LblMetricasRocAuc).text = modelo.rocAuc.toString()

        if(metricas.partidas == 0)
        {
            lblMetricasEstado.text = getString(R.string.perfil_sin_partidas)
        }
        else
        {
            lblMetricasEstado.visibility = View.GONE
            findViewById<TextView>(R.id.LblMetricasPartidas).text = metricas.partidas.toString()
            findViewById<TextView>(R.id.LblMetricasRecord).text = texto(metricas.mejorPuntuacion)
            findViewById<TextView>(R.id.LblMetricasMedia).text = texto(metricas.puntuacionMedia)
            findViewById<TextView>(R.id.LblMetricasDuracion).text = texto(metricas.duracionMedia)
            findViewById<TextView>(R.id.LblMetricasDias).text = texto(metricas.diasJugados)
            val precision = metricas.precision
            if(precision != null)
            {
                findViewById<TextView>(R.id.LblMetricasPrecision).text = getString(R.string.porcentaje, precision)
            }
            val ayudas = metricas.ayudasPorPartida
            if(ayudas != null)
            {
                findViewById<TextView>(R.id.LblMetricasAyudas).text = ayudas.toString()
            }
        }
    }

    // Un número de Python, o la raya de «sin dato» si no ha llegado
    private fun texto(valor: Int?): String
    {
        var resultado = getString(R.string.sin_dato)
        if(valor != null)
        {
            resultado = valor.toString()
        }
        return resultado
    }

    private fun mostrarError(mensaje: String)
    {
        lblMetricasEstado.visibility = View.VISIBLE
        lblMetricasEstado.text = mensaje
        btnMetricasReintentar.visibility = View.VISIBLE
    }
}
