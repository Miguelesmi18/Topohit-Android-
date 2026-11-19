package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
<<<<<<< HEAD
=======
import androidx.appcompat.app.AppCompatActivity
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.R
import com.groundstudio.topohit.api.RetrofitClient
import com.groundstudio.topohit.data_model.Agujero
import com.groundstudio.topohit.data_model.SesionNueva
import com.groundstudio.topohit.databinding.ActivityPartidaBinding
import com.groundstudio.topohit.databinding.FilaAyudaBinding
import com.groundstudio.topohit.databinding.ItemAgujeroBinding
import com.groundstudio.topohit.util.Juego
<<<<<<< HEAD
import com.groundstudio.topohit.util.Sonidos
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

// La partida: el tablero de agujeros, los topos que salen y la barra de arriba.
//
// [No visto en clase] Handler + postDelayed: para que el juego "se mueva" necesitamos
// repetir una función cada 40 ms (el bucle del juego). Un Handler ejecuta un Runnable
// en el hilo de la pantalla después de X milisegundos; al final de cada vuelta lo
// volvemos a programar. Así podemos tocar las vistas sin runOnUiThread.
// [No visto en clase] view.animate(): una animación cortita para que el topo "salte" del agujero.
<<<<<<< HEAD
class PartidaActivity : PantallaBase() {
=======
class PartidaActivity : AppCompatActivity() {
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880

    private lateinit var binding: ActivityPartidaBinding

    // ---------- Estados de la partida ----------
    private val CUENTA_ATRAS = 0
    private val JUGANDO = 1
    private val PAUSA = 2
    private val TERMINADA = 3
    private var estado = CUENTA_ATRAS

    // ---------- Configuración (sale del modo, la dificultad o el nivel) ----------
    private var modo = Juego.CONTRARRELOJ
    private var dificultad = "Fácil"
    private var nivel = 0
    private var numAgujeros = 3
    private var duracionMs = 60000L           // 0 = sin tiempo (Infinito)
    private var objetivo = 300
    private var visibleBaseMs = 1500L         // cuánto se ve cada topo
    private var saleCadaBaseMs = 1200L        // cada cuánto sale uno
    private var toposALaVez = 1
    private var probEnfadado = 0              // de cada 100 apariciones
    private var probBomba = 0
    private var probDorado = 0
    private var vidasIniciales = 0
    private var incrementoPct = 0             // Infinito: % que acelera cada segundo
    private var esJefe = false                // nivel 10

    // ---------- Lo que pasa durante la partida ----------
    private val agujeros = mutableListOf<Agujero>()
    private var reloj = 0L                    // ms jugados (no cuenta la pausa ni la cuenta atrás)
    private var cuentaAtras = 3500L
    private var ultimoTick = 0L
    private var proximaSalida = 0L
    private var visibleActual = 1500L
    private var saleCadaActual = 1200L
    private var mensajeHasta = 0L             // hasta cuándo se ve el mensaje del centro

    private var puntos = 0
    private var vidas = 0
    private var aciertos = 0                  // toques que golpean un topo (hits)
    private var fallos = 0                    // toques en agujero vacío + bombas (errorsMade)
    private var escapados = 0
    private var bombasTocadas = 0
    private var racha = 0
    private var mejorRacha = 0
    private var doradosGolpeados = 0
    private var sumaReaccion = 0L
    private var numReacciones = 0

    // Jefe
    private var golpesJefe = Juego.GOLPES_JEFE
    private var proximoSalto = 0L

    // ---------- Bucle del juego ----------
    private val handler = Handler(Looper.getMainLooper())
    private val bucle = object : Runnable {
        override fun run() {
            tick()
            if (estado != TERMINADA) {
                handler.postDelayed(this, 40)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modo = intent.getStringExtra(Constantes.MODO) ?: Juego.CONTRARRELOJ
        dificultad = intent.getStringExtra(Constantes.DIFICULTAD) ?: "Fácil"
        nivel = intent.getIntExtra(Constantes.NIVEL, 0)

        configurar()
        aplicarTema()
        crearTablero()
        prepararHud()
        prepararAyuda()

        binding.btnPausa.setOnClickListener {
            pausar(false)
        }
        binding.btnAyuda.setOnClickListener {
            pausar(true)
        }
        binding.btnContinuar.setOnClickListener {
            reanudar()
        }
        binding.btnCerrarAyuda.setOnClickListener {
            reanudar()
        }
        binding.btnReiniciar.setOnClickListener {
            enviarPartida(true)       // salir a medias cuenta como abandono
            estado = TERMINADA
            val otra = Intent(this, PartidaActivity::class.java)
            otra.putExtra(Constantes.MODO, modo)
            otra.putExtra(Constantes.DIFICULTAD, dificultad)
            otra.putExtra(Constantes.NIVEL, nivel)
            startActivity(otra)
            finish()
        }
        binding.btnSalir.setOnClickListener {
            enviarPartida(true)
            estado = TERMINADA
            val menu = Intent(this, MenuActivity::class.java)
            menu.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(menu)
            finish()
        }

        // El botón "atrás" de la tablet abre (o cierra) la pausa en vez de salir del juego.
        // [No visto en clase] OnBackPressedCallback es la forma actual de capturar el botón atrás.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (estado == PAUSA) {
                    reanudar()
                } else if (estado != TERMINADA) {
                    pausar(false)
                }
            }
        })

        ultimoTick = SystemClock.elapsedRealtime()
        handler.post(bucle)
    }

    // Si la tablet se apaga o se sale de la app, pausamos
    override fun onPause() {
        super.onPause()
        if (estado == JUGANDO || estado == CUENTA_ATRAS) {
            pausar(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(bucle)
    }

    // =====================================================================
    // CONFIGURACIÓN
    // =====================================================================

    private fun configurar() {
        if (modo == Juego.CONTRARRELOJ) {
            val d = Juego.buscarContrarreloj(dificultad)
            numAgujeros = d.agujeros
            duracionMs = d.tiempo * 1000L
            objetivo = d.objetivo
            visibleBaseMs = d.tiempoVisibleMs.toLong()
            saleCadaBaseMs = d.saleCadaMs.toLong()
            toposALaVez = d.toposALaVez
            // Las bombas aparecen a partir de Medio (GDD)
            when (dificultad) {
                "Fácil" -> { probEnfadado = 15; probBomba = 0; probDorado = 3 }
                "Medio" -> { probEnfadado = 20; probBomba = 12; probDorado = 3 }
                else -> { probEnfadado = 25; probBomba = 18; probDorado = 3 }
            }
        } else if (modo == Juego.INFINITO) {
            val d = Juego.buscarInfinito(dificultad)
            numAgujeros = d.agujeros
            duracionMs = 0
            vidasIniciales = d.vidas
            incrementoPct = d.incremento
            // Empieza a la velocidad de Fácil y va acelerando
            val facil = Juego.contrarreloj[0]
            visibleBaseMs = facil.tiempoVisibleMs.toLong()
            saleCadaBaseMs = facil.saleCadaMs.toLong()
            when (dificultad) {
                "Fácil" -> { toposALaVez = 1; probEnfadado = 15; probBomba = 0; probDorado = 4 }
                "Normal" -> { toposALaVez = 2; probEnfadado = 20; probBomba = 12; probDorado = 4 }
                else -> { toposALaVez = 3; probEnfadado = 25; probBomba = 22; probDorado = 4 }
            }
        } else {
            // HISTORIA
            val n = Juego.buscarNivel(nivel)
            numAgujeros = n.agujeros
            duracionMs = n.tiempo * 1000L
            objetivo = n.objetivo
            esJefe = n.objetivo == 0
            // Velocidad: niveles 1-3 como Fácil, 4-7 como Medio y 8-10 como Difícil
            var velocidad = Juego.contrarreloj[0]
            if (nivel >= 8) {
                velocidad = Juego.contrarreloj[2]
            } else if (nivel >= 4) {
                velocidad = Juego.contrarreloj[1]
            }
            visibleBaseMs = velocidad.tiempoVisibleMs.toLong()
            saleCadaBaseMs = velocidad.saleCadaMs.toLong()
            toposALaVez = velocidad.toposALaVez
            // Novedades de cada nivel (GDD): dorado en el 3, enfadado en el 4, bombas en el 6
            if (nivel >= 3) probDorado = 4
            if (nivel >= 4) probEnfadado = 20
            if (nivel >= 6) probBomba = 12
        }
        vidas = vidasIniciales
        visibleActual = visibleBaseMs
        saleCadaActual = saleCadaBaseMs
    }

    private fun aplicarTema() {
        val tema = Juego.tema
        binding.imgFondo.setImageResource(tema.fondo)
        binding.layHud.setBackgroundResource(tema.panel)
        binding.layPanelPausa.setBackgroundResource(tema.panel)
        binding.layPanelAyuda.setBackgroundResource(tema.panel)
        binding.btnPausa.setBackgroundResource(tema.circulo)
        binding.btnAyuda.setBackgroundResource(tema.circulo)
        binding.btnContinuar.setBackgroundResource(tema.botonSecundario)
        binding.btnCerrarAyuda.setBackgroundResource(tema.botonSecundario)
        binding.btnReiniciar.setBackgroundResource(tema.botonPrincipal)
        binding.imgTopoPausa.setImageResource(tema.topoNormal)

        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(binding.lblPuntos, binding.lblEtiquetaTiempo, binding.lblTiempo,
            binding.lblVidas, binding.lblVelocidad, binding.lblEtiquetaObjetivo, binding.lblObjetivo,
            binding.lblPausa, binding.lblAyuda)
        for (t in textos) {
            t.setTextColor(color)
        }
    }

    // Crea las filas de 3 agujeros (inflando item_agujero.xml, como en las listas personalizadas)
    private fun crearTablero() {
        binding.layTablero.removeAllViews()
        agujeros.clear()
        val filas = numAgujeros / 3
        if (filas == 1) {
            binding.layTablero.addView(layoutInflater.inflate(R.layout.espacio, binding.layTablero, false))
        }
        for (f in 1..filas) {
            val fila = layoutInflater.inflate(R.layout.fila_tablero, binding.layTablero, false) as LinearLayout
            for (c in 1..3) {
                val item = ItemAgujeroBinding.inflate(layoutInflater, fila, false)
                val agujero = Agujero(item)
                item.layAgujero.setOnClickListener {
                    tocar(agujero)
                }
                fila.addView(item.root)
                agujeros.add(agujero)
            }
            binding.layTablero.addView(fila)
        }
        if (filas == 1) {
            binding.layTablero.addView(layoutInflater.inflate(R.layout.espacio, binding.layTablero, false))
        }
    }

    private fun prepararHud() {
        if (modo == Juego.INFINITO) {
            binding.layTiempo.visibility = View.GONE
            binding.layVidas.visibility = View.VISIBLE
            binding.lblEtiquetaObjetivo.text = getString(R.string.record)
            binding.lblObjetivo.text = "" + Juego.record(modo, dificultad)
        } else if (esJefe) {
            binding.lblEtiquetaObjetivo.text = "JEFE"
            binding.lblObjetivo.text = "" + golpesJefe + " golpes"
        } else {
            binding.lblObjetivo.text = "" + objetivo
        }
<<<<<<< HEAD
=======
        binding.vProgreso.pivotX = 0f
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        actualizarHud()
    }

    private fun prepararAyuda() {
        val tema = Juego.tema
        pintarFilaAyuda(binding.filaNormal, tema.topoNormal, "Topo", "+" + Juego.PUNTOS_NORMAL + " puntos")
        pintarFilaAyuda(binding.filaEnfadado, tema.topoEnfadado, "Enfadado", "2 golpes · +" + Juego.PUNTOS_ENFADADO)
        if (modo == Juego.INFINITO) {
            pintarFilaAyuda(binding.filaDorado, tema.topoDorado, "Dorado", "+" + Juego.PUNTOS_DORADO + " · ¡y +1 vida!")
            pintarFilaAyuda(binding.filaBomba, tema.bomba, "Bomba", "¡No la toques! −1 vida")
        } else {
            pintarFilaAyuda(binding.filaDorado, tema.topoDorado, "Dorado", "+" + Juego.PUNTOS_DORADO + " puntos")
            pintarFilaAyuda(binding.filaBomba, tema.bomba, "Bomba", "¡No la toques! " + Juego.PUNTOS_BOMBA + " puntos")
        }
    }

    private fun pintarFilaAyuda(fila: FilaAyudaBinding, dibujo: Int, nombre: String, descripcion: String) {
        fila.imgFila.setImageResource(dibujo)
        fila.lblNombre.text = nombre
        fila.lblDescripcion.text = descripcion
    }

    // =====================================================================
    // BUCLE DEL JUEGO (se ejecuta cada 40 ms)
    // =====================================================================

    private fun tick() {
        val ahora = SystemClock.elapsedRealtime()
        val pasado = ahora - ultimoTick
        ultimoTick = ahora

        if (estado == CUENTA_ATRAS) {
            cuentaAtras = cuentaAtras - pasado
            binding.lblMensaje.visibility = View.VISIBLE
            if (cuentaAtras > 2500) {
<<<<<<< HEAD
                ponerCuenta("3")
            } else if (cuentaAtras > 1500) {
                ponerCuenta("2")
            } else if (cuentaAtras > 500) {
                ponerCuenta("1")
            } else if (cuentaAtras > 0) {
                ponerCuenta("¡YA!")
=======
                binding.lblMensaje.text = "3"
            } else if (cuentaAtras > 1500) {
                binding.lblMensaje.text = "2"
            } else if (cuentaAtras > 500) {
                binding.lblMensaje.text = "1"
            } else if (cuentaAtras > 0) {
                binding.lblMensaje.text = "¡YA!"
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
            } else {
                binding.lblMensaje.visibility = View.GONE
                estado = JUGANDO
                proximaSalida = 0
            }
            return
        }
        if (estado != JUGANDO) {
            return
        }

        reloj = reloj + pasado

        // Infinito: cada segundo los topos van un poco más rápido
        if (modo == Juego.INFINITO) {
            val segundos = (reloj / 1000).toInt()
            val factor = Math.pow(1.0 - incrementoPct / 100.0, segundos.toDouble())
            visibleActual = Math.max(450L, (visibleBaseMs * factor).toLong())
            saleCadaActual = Math.max(350L, (saleCadaBaseMs * factor).toLong())
            binding.lblVelocidad.text = "Velocidad x" + String.format("%.1f", 1.0 / factor)
        }

        // 1. Topos que se esconden y golpes que terminan
        for (a in agujeros) {
            if (a.estado == Agujero.GOLPEADO && reloj >= a.fin) {
                vaciar(a)
            } else if (a.estado == Agujero.FUERA && a.tipo != Agujero.JEFE && reloj >= a.fin) {
                if (a.tipo != Agujero.BOMBA) {
                    // Un topo se ha escapado: se rompe la racha
                    escapados++
                    racha = 0
                    if (modo == Juego.INFINITO) {
                        perderVida()
                    }
                }
                vaciar(a)
            } else if (a.tipo == Agujero.JEFE && a.estado == Agujero.FUERA && reloj >= a.fin) {
                // El jefe vuelve a su cara de enfadado después del golpe
                a.vista.imgTopo.setImageResource(Juego.tema.topoEnfadado)
                a.vista.imgEfecto.visibility = View.INVISIBLE
                a.vista.lblPuntos.visibility = View.INVISIBLE
                a.fin = Long.MAX_VALUE
            }
        }
        if (estado == TERMINADA) {
            return
        }

        // 2. Sacar topos nuevos
        if (esJefe) {
            moverJefe()
        } else if (reloj >= proximaSalida && contarFuera() < toposALaVez) {
            sacarTopo()
            proximaSalida = reloj + saleCadaActual
        }

        // 3. Mensaje del centro ("¡COMBO x2!")
        if (mensajeHasta > 0 && reloj >= mensajeHasta) {
            binding.lblMensaje.visibility = View.GONE
            mensajeHasta = 0
        }

        actualizarHud()

        // 4. ¿Se ha acabado el tiempo?
        if (duracionMs > 0 && reloj >= duracionMs) {
            if (modo == Juego.CONTRARRELOJ) {
                if (puntos >= objetivo) {
                    terminar(Constantes.VICTORIA)
                } else {
                    terminar(Constantes.TIEMPO)
                }
            } else if (esJefe) {
                terminar(Constantes.TIEMPO)
            } else {
                // Historia: con 1 estrella (45 % del objetivo) ya se supera el nivel
                if (Juego.calcularEstrellas(objetivo, puntos) >= 1) {
                    terminar(Constantes.VICTORIA)
                } else {
                    terminar(Constantes.TIEMPO)
                }
            }
        }
    }

<<<<<<< HEAD
    // Cambia el número de la cuenta atrás y suena un "bip" cada vez que cambia
    private fun ponerCuenta(texto: String) {
        if (binding.lblMensaje.text.toString() == texto) {
            return
        }
        binding.lblMensaje.text = texto
        if (texto == "¡YA!") {
            Sonidos.efecto(Sonidos.YA)
        } else {
            Sonidos.efecto(Sonidos.CUENTA)
        }
    }

=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
    private fun contarFuera(): Int {
        var total = 0
        for (a in agujeros) {
            if (a.estado != Agujero.VACIO) {
                total++
            }
        }
        return total
    }

    // Elige un agujero libre al azar y saca un topo (o una bomba)
    private fun sacarTopo() {
        val libres = mutableListOf<Agujero>()
        for (a in agujeros) {
            if (a.estado == Agujero.VACIO) {
                libres.add(a)
            }
        }
        if (libres.isEmpty()) {
            return
        }
        val a = libres[Random.nextInt(libres.size)]

        val r = Random.nextInt(100)
        if (r < probDorado) {
            a.tipo = Agujero.DORADO
        } else if (r < probDorado + probBomba) {
            a.tipo = Agujero.BOMBA
        } else if (r < probDorado + probBomba + probEnfadado) {
            a.tipo = Agujero.ENFADADO
        } else {
            a.tipo = Agujero.NORMAL
        }

        val tema = Juego.tema
        when (a.tipo) {
            Agujero.DORADO -> { a.golpesQuedan = 1; a.vista.imgTopo.setImageResource(tema.topoDorado) }
            Agujero.BOMBA -> { a.golpesQuedan = 1; a.vista.imgTopo.setImageResource(tema.bomba) }
            Agujero.ENFADADO -> { a.golpesQuedan = 2; a.vista.imgTopo.setImageResource(tema.topoEnfadado) }
            else -> { a.golpesQuedan = 1; a.vista.imgTopo.setImageResource(tema.topoNormal) }
        }

        var visible = visibleActual
        if (a.tipo == Agujero.DORADO) {
            visible = visibleActual * 8 / 10        // el dorado se escapa antes
        } else if (a.tipo == Agujero.ENFADADO) {
            visible = visibleActual * 13 / 10       // le damos un poco más para los 2 golpes
        }
        a.estado = Agujero.FUERA
        a.golpeado = false
        a.salio = reloj
        a.fin = reloj + visible
        mostrarTopo(a)
    }

    // Animación: el topo sube desde el agujero
    private fun mostrarTopo(a: Agujero) {
        val img = a.vista.imgTopo
        img.scaleX = 1f
        img.scaleY = 1f
        img.translationY = img.height * 0.5f
        img.visibility = View.VISIBLE
        img.animate().translationY(0f).setDuration(120).start()
        a.vista.imgEfecto.visibility = View.INVISIBLE
        a.vista.lblPuntos.visibility = View.INVISIBLE
    }

    private fun vaciar(a: Agujero) {
        a.estado = Agujero.VACIO
        a.vista.imgTopo.visibility = View.INVISIBLE
        a.vista.imgEfecto.visibility = View.INVISIBLE
        a.vista.lblPuntos.visibility = View.INVISIBLE
    }

    // ---------- Jefe (nivel 10): un topo gigante que salta de agujero en agujero ----------

    private fun moverJefe() {
        if (reloj < proximoSalto) {
            return
        }
        var actual: Agujero? = null
        for (a in agujeros) {
            if (a.tipo == Agujero.JEFE && a.estado == Agujero.FUERA) {
                actual = a
            }
        }
        // Elegimos otro agujero distinto al de ahora
        var destino = agujeros[Random.nextInt(agujeros.size)]
        while (destino == actual) {
            destino = agujeros[Random.nextInt(agujeros.size)]
        }
        if (actual != null) {
            vaciar(actual)
            actual.tipo = Agujero.NORMAL
        }
        destino.tipo = Agujero.JEFE
        destino.estado = Agujero.FUERA
        destino.golpeado = false
        destino.salio = reloj
        destino.fin = Long.MAX_VALUE
        destino.vista.imgTopo.setImageResource(Juego.tema.topoEnfadado)
        mostrarTopo(destino)
        destino.vista.imgTopo.scaleX = 1.3f
        destino.vista.imgTopo.scaleY = 1.3f
        // Cuantos menos golpes le quedan, más rápido salta
        proximoSalto = reloj + 700 + golpesJefe * 60
    }

    // =====================================================================
    // TOQUES
    // =====================================================================

    private fun tocar(a: Agujero) {
        if (estado != JUGANDO) {
            return
        }

        // Agujero vacío (o un topo ya golpeado): fallo. No resta puntos, pero rompe la racha.
        if (a.estado != Agujero.FUERA) {
            fallos++
            racha = 0
<<<<<<< HEAD
            Sonidos.efecto(Sonidos.FALLO)
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
            actualizarHud()
            return
        }

        // Bomba: cuenta como error
        if (a.tipo == Agujero.BOMBA) {
            fallos++
            bombasTocadas++
            racha = 0
<<<<<<< HEAD
            Sonidos.efecto(Sonidos.BOMBA)
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
            a.estado = Agujero.GOLPEADO
            a.fin = reloj + 600
            a.vista.imgTopo.setImageResource(Juego.tema.explosion)
            if (modo == Juego.INFINITO) {
                mostrarPuntos(a, "−1 ❤")
                perderVida()
            } else {
                puntos = Math.max(0, puntos + Juego.PUNTOS_BOMBA)
                mostrarPuntos(a, "" + Juego.PUNTOS_BOMBA)
            }
            actualizarHud()
            return
        }

        // Es un topo: acierto
        aciertos++
        racha++
        if (racha > mejorRacha) {
            mejorRacha = racha
        }
        if (racha == Juego.ACIERTOS_PARA_COMBO) {
            mostrarMensaje("¡COMBO x" + Juego.MULTIPLICADOR_COMBO + "!")
<<<<<<< HEAD
            Sonidos.efecto(Sonidos.COMBO)
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        }
        // Tiempo de reacción: desde que sale hasta el primer golpe
        if (!a.golpeado) {
            a.golpeado = true
            sumaReaccion = sumaReaccion + (reloj - a.salio)
            numReacciones++
        }
        a.golpesQuedan--

        if (a.tipo == Agujero.JEFE) {
<<<<<<< HEAD
            Sonidos.efecto(Sonidos.GOLPE)
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
            golpearJefe(a)
            actualizarHud()
            return
        }

        if (a.golpesQuedan > 0) {
            // Enfadado después del primer golpe: se pone la tirita y aguanta un poquito más
            a.vista.imgTopo.setImageResource(Juego.tema.topoEnfadadoTirita)
<<<<<<< HEAD
            Sonidos.efecto(Sonidos.GOLPE)
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
            a.fin = a.fin + 400
            actualizarHud()
            return
        }

        // Topo derrotado: puntos (dobles si hay combo)
        var base = Juego.PUNTOS_NORMAL
        if (a.tipo == Agujero.ENFADADO) {
            base = Juego.PUNTOS_ENFADADO
        } else if (a.tipo == Agujero.DORADO) {
            base = Juego.PUNTOS_DORADO
            doradosGolpeados++
            if (modo == Juego.INFINITO && vidas < vidasIniciales) {
                vidas++
            }
        }
        val ganados = base * multiplicador()
        puntos = puntos + ganados
<<<<<<< HEAD
        if (a.tipo == Agujero.DORADO) {
            Sonidos.efecto(Sonidos.DORADO)
        } else {
            Sonidos.efecto(Sonidos.GOLPE)
        }
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880

        a.estado = Agujero.GOLPEADO
        a.fin = reloj + 450
        a.vista.imgTopo.setImageResource(Juego.tema.topoGolpeado)
        a.vista.imgEfecto.visibility = View.VISIBLE
        mostrarPuntos(a, "+" + ganados)
        actualizarHud()
    }

    private fun golpearJefe(a: Agujero) {
        golpesJefe--
        puntos = puntos + Juego.PUNTOS_GOLPE_JEFE * multiplicador()
        a.vista.imgTopo.setImageResource(Juego.tema.topoGolpeado)
        a.vista.imgEfecto.visibility = View.VISIBLE
        mostrarPuntos(a, "" + golpesJefe)
        a.fin = reloj + 250
        binding.lblObjetivo.text = "" + golpesJefe + " golpes"
        if (golpesJefe <= 0) {
            puntos = puntos + Juego.PUNTOS_JEFE_VENCIDO
            terminar(Constantes.VICTORIA)
        } else {
            // Al recibir un golpe salta antes a otro agujero
            proximoSalto = Math.min(proximoSalto, reloj + 400)
        }
    }

    private fun multiplicador(): Int {
        if (racha >= Juego.ACIERTOS_PARA_COMBO) {
            return Juego.MULTIPLICADOR_COMBO
        }
        return 1
    }

    private fun perderVida() {
<<<<<<< HEAD
        Sonidos.efecto(Sonidos.VIDA)
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        vidas--
        if (vidas <= 0) {
            vidas = 0
            terminar(Constantes.SIN_VIDAS)
        }
    }

    private fun mostrarPuntos(a: Agujero, texto: String) {
        a.vista.lblPuntos.text = texto
        a.vista.lblPuntos.visibility = View.VISIBLE
    }

    private fun mostrarMensaje(texto: String) {
        binding.lblMensaje.text = texto
        binding.lblMensaje.visibility = View.VISIBLE
        mensajeHasta = reloj + 900
    }

    // =====================================================================
    // BARRA DE ARRIBA
    // =====================================================================

    private fun actualizarHud() {
        binding.lblPuntos.text = "" + puntos

        if (racha >= Juego.ACIERTOS_PARA_COMBO) {
            binding.lblCombo.text = "COMBO x" + Juego.MULTIPLICADOR_COMBO
        } else {
            binding.lblCombo.text = "Racha " + racha
        }

        if (modo == Juego.INFINITO) {
            binding.lblVidas.text = "x" + vidas
            if (vidas > 0) {
                binding.imgCorazon.setImageResource(R.drawable.corazon_lleno)
            } else {
                binding.imgCorazon.setImageResource(R.drawable.corazon_vacio)
            }
        } else {
            var quedan = duracionMs - reloj
            if (quedan < 0) {
                quedan = 0
            }
<<<<<<< HEAD
            // El número y la barra salen del mismo valor (quedan), así siempre van a la par
            binding.lblTiempo.text = "" + ((quedan + 999) / 1000) + " s"
            pintarBarra(quedan.toFloat() / duracionMs.toFloat())
        }
    }

    // La barra verde mide lo que queda de tiempo: le cambiamos el ancho (layoutParams).
    // Antes se encogía con scaleX y, como la barra tiene borde y esquinas redondas, el borde
    // también se encogía y parecía que la barra se acababa antes que el tiempo.
    private fun pintarBarra(fraccion: Float) {
        val fondo = binding.vProgreso.parent as View
        val anchoTotal = fondo.width
        if (anchoTotal == 0) {
            return          // la pantalla todavía no se ha dibujado: la barra sigue llena
        }
        val ancho = (anchoTotal * fraccion).toInt()
        val params = binding.vProgreso.layoutParams
        if (params.width != ancho) {
            params.width = ancho
            binding.vProgreso.layoutParams = params
        }
        if (ancho <= 0) {
            binding.vProgreso.visibility = View.INVISIBLE
        } else {
            binding.vProgreso.visibility = View.VISIBLE
=======
            binding.lblTiempo.text = "" + ((quedan + 999) / 1000) + " s"
            // La barra verde se encoge (scaleX va de 1 a 0)
            binding.vProgreso.scaleX = quedan.toFloat() / duracionMs.toFloat()
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        }
    }

    // =====================================================================
    // PAUSA Y AYUDA
    // =====================================================================

    private fun pausar(conAyuda: Boolean) {
        if (estado == TERMINADA) {
            return
        }
        estado = PAUSA
        binding.lblMensaje.visibility = View.GONE
        if (conAyuda) {
            binding.layAyuda.visibility = View.VISIBLE
            binding.layPausa.visibility = View.GONE
        } else {
            binding.layPausa.visibility = View.VISIBLE
            binding.layAyuda.visibility = View.GONE
        }
    }

    private fun reanudar() {
        binding.layPausa.visibility = View.GONE
        binding.layAyuda.visibility = View.GONE
        ultimoTick = SystemClock.elapsedRealtime()
        // Si se pausó durante la cuenta atrás, la repetimos entera
        if (reloj == 0L) {
            cuentaAtras = 3500
            estado = CUENTA_ATRAS
        } else {
            estado = JUGANDO
        }
    }

    // =====================================================================
    // FIN DE LA PARTIDA
    // =====================================================================

    private fun terminar(resultado: String) {
        if (estado == TERMINADA) {
            return
        }
        estado = TERMINADA
        handler.removeCallbacks(bucle)
<<<<<<< HEAD
        if (resultado == Constantes.VICTORIA) {
            Sonidos.efecto(Sonidos.VICTORIA)
        } else {
            Sonidos.efecto(Sonidos.FIN)
        }
=======
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880

        var estrellas = 0
        if (modo == Juego.HISTORIA) {
            if (esJefe) {
                if (resultado == Constantes.VICTORIA) {
                    estrellas = 3
                }
            } else {
                estrellas = Juego.calcularEstrellas(objetivo, puntos)
            }
        }

        // Apuntamos la partida en el progreso de la tablet y la enviamos a la API
        var dificultadGuardada: String? = dificultad
        if (modo == Juego.HISTORIA) {
            dificultadGuardada = null
        }
        val nuevoRecord = Juego.apuntarPartida(modo, dificultadGuardada, nivel, puntos, estrellas)
        enviarPartida(false)

        val intent = Intent(this, ResultadoActivity::class.java)
        intent.putExtra(Constantes.MODO, modo)
        intent.putExtra(Constantes.DIFICULTAD, dificultad)
        intent.putExtra(Constantes.NIVEL, nivel)
        intent.putExtra(Constantes.RESULTADO, resultado)
        intent.putExtra(Constantes.PUNTOS, puntos)
        intent.putExtra(Constantes.ESTRELLAS, estrellas)
        intent.putExtra(Constantes.ACIERTOS, aciertos)
        intent.putExtra(Constantes.FALLOS, fallos)
        intent.putExtra(Constantes.ESCAPADOS, escapados)
        intent.putExtra(Constantes.BOMBAS, bombasTocadas)
        intent.putExtra(Constantes.MEJOR_RACHA, mejorRacha)
        intent.putExtra(Constantes.NUEVO_RECORD, nuevoRecord)
        intent.putExtra(Constantes.OBJETIVO, objetivo)
        startActivity(intent)
        finish()
    }

    // POST api/sesiones con los datos de la partida (para el escritorio y las métricas)
    @OptIn(DelicateCoroutinesApi::class)
    private fun enviarPartida(abandonada: Boolean) {
<<<<<<< HEAD
=======
        if (Juego.invitado) {
            return              // sin cuenta no se guarda nada
        }
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        val segundos = (reloj / 1000).toInt()
        if (abandonada && segundos < 1) {
            return              // ni siquiera ha empezado a jugar
        }

        var reaccion: Int? = null
        if (numReacciones > 0) {
            reaccion = (sumaReaccion / numReacciones).toInt()
        }
        // En Historia se envía el nivel y no la dificultad; en los otros modos, al revés
        var dificultadApi: String? = dificultad
        var nivelApi: Int? = null
        if (modo == Juego.HISTORIA) {
            dificultadApi = null
            nivelApi = nivel
        }

        val sesion = SesionNueva(
            Juego.idModo(modo),
            Math.max(1, segundos),
            fallos,
            puntos,
            dificultadApi,
            nivelApi,
            aciertos + fallos,          // nAttempts = hits + errorsMade (regla de la API)
            escapados,
            aciertos,
            mejorRacha,
            doradosGolpeados,
            abandonada,
            reaccion
        )
        val token = Juego.cabeceraToken()
        val contexto = applicationContext
        GlobalScope.launch {
            try {
                val response = RetrofitClient.api().guardarPartida(token, sesion).execute()
                if (!response.isSuccessful) {
                    val mensaje = RetrofitClient.mensajeDeError(response)
                    runOnUiThread {
                        Toast.makeText(contexto, "No se ha guardado la partida: " + mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(contexto, "Sin conexión: la partida no se ha guardado", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
