package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.R
import com.groundstudio.topohit.databinding.ActivityMapaHistoriaBinding
import com.groundstudio.topohit.databinding.ItemNivelBinding
import com.groundstudio.topohit.util.Juego

// Mapa del modo Historia: 10 niveles en 3 zonas (Campo, Huerto y Mina).
// Cada nivel se desbloquea al sacar 1 estrella o más en el anterior.
class MapaHistoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaHistoriaBinding
    private val niveles = mutableListOf<ItemNivelBinding>()
    private var nivelElegido = 1

    // Posición de cada nivel en el mapa, en tanto por uno del ancho y del alto (sacada del Figma)
    private val posX = listOf(0.11f, 0.22f, 0.33f, 0.44f, 0.55f, 0.66f, 0.77f, 0.865f, 0.935f, 0.905f)
    private val posY = listOf(0.80f, 0.56f, 0.80f, 0.56f, 0.80f, 0.56f, 0.80f, 0.53f, 0.82f, 0.25f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaHistoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.btnAtras.setBackgroundResource(tema.circulo)
        binding.btnCerrarInicio.setBackgroundResource(tema.circulo)
        binding.layPanelInicio.setBackgroundResource(tema.panel)
        binding.btnJugarNivel.setBackgroundResource(tema.botonPrincipal)
        binding.imgTopoAqui.setImageResource(tema.topoNormal)
        val color = ContextCompat.getColor(this, tema.colorTexto)
        binding.lblNivel.setTextColor(color)
        binding.lblObjetivoNivel.setTextColor(color)

        crearMapa()

        binding.btnAtras.setOnClickListener {
            finish()
        }
        binding.btnCerrarInicio.setOnClickListener {
            binding.layInicio.visibility = View.GONE
        }
        binding.btnJugarNivel.setOnClickListener {
            binding.layInicio.visibility = View.GONE
            val intent = Intent(this, PartidaActivity::class.java)
            intent.putExtra(Constantes.MODO, Juego.HISTORIA)
            intent.putExtra(Constantes.NIVEL, nivelElegido)
            startActivity(intent)
        }

        // Si venimos de "Siguiente" en el resultado, abrimos directamente ese nivel
        abrirNivelPedido(intent)
    }

    // Llega aquí cuando el mapa ya estaba abierto y pulsamos "Siguiente"
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        abrirNivelPedido(intent)
    }

    override fun onResume() {
        super.onResume()
        pintarNiveles()
    }

    private fun abrirNivelPedido(intent: Intent?) {
        val pedido = intent?.getIntExtra(Constantes.NIVEL, 0) ?: 0
        if (pedido > 0 && pedido <= Juego.nivelDesbloqueado()) {
            mostrarInicio(pedido)
        }
    }

    // Crea los carteles de zona y los 10 botones de nivel (inflando layouts)
    private fun crearMapa() {
        val zonas = listOf("CAMPO", "HUERTO", "MINA")
        val zonasX = listOf(0.11f, 0.43f, 0.77f)
        val carteles = mutableListOf<TextView>()
        for (z in zonas) {
            val cartel = layoutInflater.inflate(R.layout.item_zona, binding.layMapa, false) as TextView
            cartel.text = z
            binding.layMapa.addView(cartel)
            carteles.add(cartel)
        }

        for (i in 0..Juego.niveles.size - 1) {
            val item = ItemNivelBinding.inflate(layoutInflater, binding.layMapa, false)
            val numero = Juego.niveles[i].numero
            item.btnNivel.setOnClickListener {
                if (numero <= Juego.nivelDesbloqueado()) {
                    mostrarInicio(numero)
                } else {
                    Toast.makeText(this, "Consigue 1 estrella en el nivel anterior para abrir este", Toast.LENGTH_SHORT).show()
                }
            }
            binding.layMapa.addView(item.root)
            niveles.add(item)
        }

        // Cuando el mapa ya tiene tamaño, colocamos cada cosa en su sitio
        binding.layMapa.post {
            val ancho = binding.layMapa.width
            val alto = binding.layMapa.height
            for (i in 0..carteles.size - 1) {
                carteles[i].translationX = zonasX[i] * ancho - carteles[i].width / 2
                carteles[i].translationY = 0f
            }
            for (i in 0..niveles.size - 1) {
                val vista = niveles[i].root
                val px = posX[i % posX.size]
                val py = posY[i % posY.size]
                vista.translationX = px * ancho - vista.width / 2
                vista.translationY = py * alto - niveles[i].btnNivel.height / 2
            }
            colocarTopoAqui()
        }
    }

    // Pinta cada nivel: bloqueado (gris), jugable (naranja) o superado (verde) y sus estrellas
    private fun pintarNiveles() {
        val desbloqueado = Juego.nivelDesbloqueado()
        for (i in 0..niveles.size - 1) {
            val item = niveles[i]
            val nivel = Juego.niveles[i]
            val estrellas = Juego.estrellasDeNivel(nivel.numero)

            if (nivel.objetivo == 0) {
                item.btnNivel.text = "JEFE"
                item.btnNivel.textSize = 20f
            } else {
                item.btnNivel.text = "" + nivel.numero
            }

            if (nivel.numero > desbloqueado) {
                item.btnNivel.setBackgroundResource(R.drawable.circulo_gris)
            } else if (estrellas > 0) {
                item.btnNivel.setBackgroundResource(R.drawable.circulo_verde)
            } else {
                item.btnNivel.setBackgroundResource(R.drawable.circulo_naranja)
            }

            val imagenes = listOf<ImageView>(item.imgEstrella1, item.imgEstrella2, item.imgEstrella3)
            for (e in 0..2) {
                if (e < estrellas) {
                    imagenes[e].setImageResource(R.drawable.estrella_llena)
                } else {
                    imagenes[e].setImageResource(R.drawable.estrella_vacia)
                }
                // Los niveles bloqueados no enseñan estrellas
                if (nivel.numero > desbloqueado) {
                    imagenes[e].visibility = View.INVISIBLE
                } else {
                    imagenes[e].visibility = View.VISIBLE
                }
            }
        }
        binding.lblTotalEstrellas.text = "" + Juego.totalEstrellas() + "/" + (Juego.niveles.size * 3)
        binding.layMapa.post {
            colocarTopoAqui()
        }
    }

    // El topo se pone encima del último nivel desbloqueado ("estás aquí")
    private fun colocarTopoAqui() {
        val indice = Juego.nivelDesbloqueado() - 1
        if (indice < 0 || indice >= niveles.size) {
            return
        }
        val vista = niveles[indice].root
        binding.imgTopoAqui.translationX = vista.translationX + vista.width / 2 - binding.imgTopoAqui.width / 2
        binding.imgTopoAqui.translationY = vista.translationY - binding.imgTopoAqui.height
        binding.imgTopoAqui.bringToFront()
    }

    // Panel "Inicio de nivel": objetivo, estrellas y la novedad del nivel
    private fun mostrarInicio(numero: Int) {
        nivelElegido = numero
        val nivel = Juego.buscarNivel(numero)
        val tema = Juego.tema

        binding.lblNivel.text = "Nivel " + nivel.numero + " · " + nivel.zona
        if (nivel.objetivo == 0) {
            binding.lblObjetivoNivel.text = "Dale " + Juego.GOLPES_JEFE + " golpes al topo gigante en " + nivel.tiempo + " s"
        } else {
            binding.lblObjetivoNivel.text = "Objetivo: " + nivel.objetivo + " puntos en " + nivel.tiempo + " s"
        }

        val estrellas = Juego.estrellasDeNivel(numero)
        val imagenes = listOf<ImageView>(binding.imgInicioEstrella1, binding.imgInicioEstrella2, binding.imgInicioEstrella3)
        for (e in 0..2) {
            if (e < estrellas) {
                imagenes[e].setImageResource(R.drawable.estrella_llena)
            } else {
                imagenes[e].setImageResource(R.drawable.estrella_vacia)
            }
        }

        // La novedad (si el nivel tiene) con su dibujo
        val novedad = nivel.novedad ?: ""
        if (novedad.isEmpty()) {
            binding.layNovedad.visibility = View.GONE
        } else {
            binding.layNovedad.visibility = View.VISIBLE
            binding.lblNovedad.text = novedad
            when (numero) {
                3 -> binding.imgNovedad.setImageResource(tema.topoDorado)
                4 -> binding.imgNovedad.setImageResource(tema.topoEnfadado)
                6 -> binding.imgNovedad.setImageResource(tema.bomba)
                10 -> binding.imgNovedad.setImageResource(tema.topoEnfadadoTirita)
                else -> binding.imgNovedad.setImageResource(tema.topoNormal)
            }
        }
        binding.layInicio.visibility = View.VISIBLE
    }
}
