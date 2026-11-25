package com.groundstudio.topohit.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.adapters.RankingAdapter
import com.groundstudio.topohit.api.ApiGetRanking
import com.groundstudio.topohit.api.ApiGetRankingTotal
import com.groundstudio.topohit.api.RetrofitGetRankingClient
import com.groundstudio.topohit.api.RetrofitGetRankingTotalClient
import com.groundstudio.topohit.datamodel.PuestoRanking
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call

// Ranking: los 10 mejores récords de todos los jugadores en cada dificultad de Infinito
// (GET api/sesiones/ranking) y la clasificación total, que suma el récord de cada dificultad
// (GET api/sesiones/ranking/total). Una pestaña para cada una; la fila del jugador va resaltada.
class RankingActivity : PantallaBase()
{
    private lateinit var lstRanking: RecyclerView
    private lateinit var adapter: RankingAdapter
    private lateinit var lblRankingVacio: TextView
    private lateinit var pestanas: List<TextView>
    private var puestos: List<PuestoRanking> = listOf()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        lstRanking      = findViewById<RecyclerView>(R.id.LstRanking)
        lblRankingVacio = findViewById<TextView>(R.id.LblRankingVacio)
        pestanas = listOf(findViewById<TextView>(R.id.BtnRankingFacil),
                          findViewById<TextView>(R.id.BtnRankingNormal),
                          findViewById<TextView>(R.id.BtnRankingDificil),
                          findViewById<TextView>(R.id.BtnRankingTotal))

        val tema = GestorAjustes.tema

        pintarTitulo(R.id.LblRankingCabecera)
        findViewById<ImageView>(R.id.ImgRankingFondo).setImageResource(tema.fondo)
        findViewById<TextView>(R.id.BtnRankingAtras).setBackgroundResource(tema.circulo)
        findViewById<LinearLayout>(R.id.LayRankingPanel).setBackgroundResource(tema.panel)
        lblRankingVacio.setTextColor(ContextCompat.getColor(this, tema.colorTexto))

        adapter = RankingAdapter(this, puestos, GestorSesion.getIdUsuario(this))
        lstRanking.layoutManager = LinearLayoutManager(this)
        lstRanking.adapter = adapter

        for(i in 0..SharedData.DIFICULTADES.size - 1)
        {
            pestanas[i].text = SharedData.DIFICULTADES[i]
        }
        for(i in 0..pestanas.size - 1)
        {
            pestanas[i].setOnClickListener()
            {
                elegirPestana(i)
            }
        }

        findViewById<TextView>(R.id.BtnRankingAtras).setOnClickListener()
        {
            finish()
        }

        elegirPestana(0)
    }

    // La pestaña elegida va con el color principal y las otras con el secundario
    private fun elegirPestana(elegida: Int)
    {
        val tema = GestorAjustes.tema
        for(i in 0..pestanas.size - 1)
        {
            if(i == elegida)
            {
                pestanas[i].setBackgroundResource(tema.botonPrincipal)
                pestanas[i].setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
                pestanas[i].alpha = 1f
            }
            else
            {
                pestanas[i].setBackgroundResource(tema.botonSecundario)
                pestanas[i].setTextColor(ContextCompat.getColor(this, tema.colorTextoBotonSecundario))
                pestanas[i].alpha = 0.75f
            }
        }
        // Las primeras pestañas son las dificultades y la última, el total
        if(elegida < SharedData.DIFICULTADES.size)
        {
            getRanking(SharedData.DIFICULTADES[elegida])
        }
        else
        {
            getRankingTotal()
        }
    }

    private fun getRanking(dificultad: String)
    {
        val token = GestorSesion.cabecera(this)
        val rankingApi = RetrofitGetRankingClient.getInstance().create(ApiGetRanking::class.java)
        val call = rankingApi.getRanking(token, SharedData.MODO_INFINITO, dificultad, SharedData.TOP_RANKING)
        cargarRanking(call, getString(R.string.ranking_vacio))
    }

    private fun getRankingTotal()
    {
        val token = GestorSesion.cabecera(this)
        val rankingApi = RetrofitGetRankingTotalClient.getInstance().create(ApiGetRankingTotal::class.java)
        val call = rankingApi.getRankingTotal(token, SharedData.TOP_RANKING)
        cargarRanking(call, getString(R.string.ranking_total_vacio))
    }

    // Las dos llamadas devuelven la misma lista de puestos: se cargan igual
    private fun cargarRanking(call: Call<List<PuestoRanking>>, textoVacio: String)
    {
        lblRankingVacio.visibility = View.VISIBLE
        lblRankingVacio.text = getString(R.string.cargando)

        // GlobalScope.launch: lo de dentro se ejecuta en segundo plano. Las llamadas a la API tardan y,
        // si se hicieran en el hilo de la pantalla, la app se congelaría (Android no lo deja).
        // Dentro, call.execute() espera la respuesta y response.body() es el JSON ya pasado a objetos (null si hay error).
        GlobalScope.launch()
        {
            try
            {
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
                            puestos = local
                            adapter.setPuestos(puestos)
                            if(puestos.size == 0)
                            {
                                lblRankingVacio.text = textoVacio
                            }
                            else
                            {
                                lblRankingVacio.visibility = View.GONE
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
                            lblRankingVacio.text = getString(R.string.error_cargar)
                            Avisos.errorApi(this@RankingActivity, codigo)
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
                        lblRankingVacio.text = getString(R.string.error_conexion)
                    }
                }
            }
        }
    }
}
