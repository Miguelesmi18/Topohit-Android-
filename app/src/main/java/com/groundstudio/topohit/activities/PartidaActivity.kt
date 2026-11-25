package com.groundstudio.topohit.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.groundstudio.topohit.Avisos
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.GestorSesion
import com.groundstudio.topohit.R
import com.groundstudio.topohit.ReglasJuego
import com.groundstudio.topohit.SharedData
import com.groundstudio.topohit.Sonidos
import com.groundstudio.topohit.api.ApiAddSesion
import com.groundstudio.topohit.api.RetrofitAddSesionClient
import com.groundstudio.topohit.datamodel.Agujero
import com.groundstudio.topohit.datamodel.EstadoDificultad
import com.groundstudio.topohit.datamodel.SesionNueva
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Random

// La partida del modo Infinito: el tablero de 3, 6 o 9 agujeros, los topos que salen y la barra de arriba.
// No hay tiempo: se juega hasta perder todas las vidas. Se pierde una vida al tocar una bomba
// y cada vez que un topo se escapa sin golpearlo. Cada segundo los topos van un poco más rápido
// (el % de «incremento» de la dificultad). El topo dorado da +1 vida.
//
// Esta parte es de información externa que no sale en los vídeos
// (Handler + postDelayed: el juego tiene que repetir una función cada 40 ms, el «bucle del juego».
// El Handler la ejecuta en el hilo de la pantalla, así que no hace falta runOnUiThread.
// SystemClock.elapsedRealtime() mide el tiempo para que la pausa no cuente).
class PartidaActivity : PantallaBase()
{
    // Clave del extra que recibe esta pantalla (REGLA-0489-042)
    object partidaConstants
    {
        const val DIFICULTAD = "DIFICULTAD"
    }

    // ---------- Estados de la partida ----------
    private val CUENTA_ATRAS = 0
    private val JUGANDO = 1
    private val PAUSA = 2
    private val TERMINADA = 3
    private var estado = CUENTA_ATRAS

    // ---------- Vistas ----------
    private lateinit var lblPartidaPuntos: TextView
    private lateinit var lblPartidaVidas: TextView
    private lateinit var imgPartidaCorazon: ImageView
    private lateinit var lblPartidaVelocidad: TextView
    private lateinit var lblPartidaCombo: TextView
    private lateinit var lblPartidaMensaje: TextView
    private lateinit var layPartidaPausa: FrameLayout
    private lateinit var layPartidaAyuda: FrameLayout

    // ---------- Configuración (sale de la dificultad que manda la API) ----------
    private lateinit var dificultad: EstadoDificultad
    private var toposALaVez = 1
    private var probEnfadado = 0
    private var probBomba = 0
    private var probDorado = 0

    // ---------- Lo que pasa durante la partida ----------
    private val agujeros = mutableListOf<Agujero>()
    private val random = Random()
    private var reloj = 0L                    // ms jugados (no cuenta la pausa ni la cuenta atrás)
    private var cuentaAtras = ReglasJuego.CUENTA_ATRAS_MS
    private var ultimoTick = 0L
    private var proximaSalida = 0L
    private var visibleActual = ReglasJuego.VISIBLE_BASE_MS
    private var saleCadaActual = ReglasJuego.SALE_CADA_BASE_MS
    private var mensajeHasta = 0L             // hasta cuándo se ve el mensaje del centro

    private var puntos = 0
    private var vidas = 0
    private var aciertos = 0                  // toques que golpean un topo (hits)
    private var fallos = 0                    // toques en agujero vacío + bombas (errorsMade)
    private var escapados = 0
    private var racha = 0
    private var mejorRacha = 0
    private var doradosGolpeados = 0
    private var ayudas = 0                    // veces que pulsa la ayuda ? (helpClicks)
    private var sumaReaccion = 0L
    private var numReacciones = 0

    // ---------- Bucle del juego ----------
    private val handler = Handler(Looper.getMainLooper())
    private val bucle = object : Runnable
    {
        override fun run()
        {
            tick()
            if(estado != TERMINADA)
            {
                handler.postDelayed(this, 40)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partida)

        // getSerializableExtra: recoge el objeto que ha enviado la pantalla anterior en el Intent (as Tipo lo convierte)
        dificultad = intent.getSerializableExtra(partidaConstants.DIFICULTAD) as EstadoDificultad

        lblPartidaPuntos    = findViewById<TextView>(R.id.LblPartidaPuntos)
        lblPartidaVidas     = findViewById<TextView>(R.id.LblPartidaVidas)
        imgPartidaCorazon   = findViewById<ImageView>(R.id.ImgPartidaCorazon)
        lblPartidaVelocidad = findViewById<TextView>(R.id.LblPartidaVelocidad)
        lblPartidaCombo     = findViewById<TextView>(R.id.LblPartidaCombo)
        lblPartidaMensaje   = findViewById<TextView>(R.id.LblPartidaMensaje)
        layPartidaPausa     = findViewById<FrameLayout>(R.id.LayPartidaPausa)
        layPartidaAyuda     = findViewById<FrameLayout>(R.id.LayPartidaAyuda)

        configurar()
        pintarTema()
        crearTablero()
        prepararAyuda()
        actualizarHud()

        findViewById<TextView>(R.id.BtnPartidaPausa).setOnClickListener()
        {
            pausar(false)
        }

        findViewById<TextView>(R.id.BtnPartidaAyuda).setOnClickListener()
        {
            pausar(true)
        }

        findViewById<TextView>(R.id.BtnPartidaContinuar).setOnClickListener()
        {
            reanudar()
        }

        findViewById<TextView>(R.id.BtnPartidaCerrarAyuda).setOnClickListener()
        {
            reanudar()
        }

        // Reiniciar: la partida a medias se guarda como abandonada y empieza otra igual
        findViewById<TextView>(R.id.BtnPartidaReiniciar).setOnClickListener()
        {
            abandonar(true)
        }

        // Salir: se vuelve a la pantalla de dificultades (que es la que abrió esta)
        findViewById<TextView>(R.id.BtnPartidaSalir).setOnClickListener()
        {
            abandonar(false)
        }

        // Esta parte es de información externa que no sale en los vídeos
        // (OnBackPressedCallback: el botón «atrás» de la tablet abre la pausa en vez de salir)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if(estado == PAUSA)
                {
                    reanudar()
                }
                else if(estado != TERMINADA)
                {
                    pausar(false)
                }
            }
        })

        ultimoTick = SystemClock.elapsedRealtime()
        handler.post(bucle)
    }

    // Si la tablet se apaga o se sale de la app, se pausa
    override fun onPause()
    {
        super.onPause()
        if(estado == JUGANDO || estado == CUENTA_ATRAS)
        {
            pausar(false)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        handler.removeCallbacks(bucle)
    }

    // =====================================================================
    // PREPARAR LA PARTIDA
    // =====================================================================

    private fun configurar()
    {
        vidas = dificultad.vidas
        toposALaVez = ReglasJuego.toposALaVez(dificultad.orden)
        probEnfadado = ReglasJuego.probEnfadado(dificultad.orden)
        probBomba = ReglasJuego.probBomba(dificultad.orden)
        probDorado = ReglasJuego.probDorado()

        var record = 0
        val anterior = dificultad.record
        if(anterior != null)
        {
            record = anterior
        }
        findViewById<TextView>(R.id.LblPartidaRecord).text = record.toString()
        findViewById<TextView>(R.id.LblPartidaDificultad).text = dificultad.nombre
    }

    private fun pintarTema()
    {
        val tema = GestorAjustes.tema
        findViewById<ImageView>(R.id.ImgPartidaFondo).setImageResource(tema.fondo)
        findViewById<LinearLayout>(R.id.LayPartidaHud).setBackgroundResource(tema.panel)
        findViewById<LinearLayout>(R.id.LayPartidaPanelPausa).setBackgroundResource(tema.panel)
        findViewById<LinearLayout>(R.id.LayPartidaPanelAyuda).setBackgroundResource(tema.panel)
        findViewById<TextView>(R.id.BtnPartidaPausa).setBackgroundResource(tema.circulo)
        findViewById<TextView>(R.id.BtnPartidaAyuda).setBackgroundResource(tema.circulo)
        findViewById<TextView>(R.id.BtnPartidaContinuar).setBackgroundResource(tema.botonSecundario)
        findViewById<TextView>(R.id.BtnPartidaCerrarAyuda).setBackgroundResource(tema.botonSecundario)
        val btnPartidaReiniciar = findViewById<TextView>(R.id.BtnPartidaReiniciar)
        btnPartidaReiniciar.setBackgroundResource(tema.botonPrincipal)
        btnPartidaReiniciar.setTextColor(ContextCompat.getColor(this, tema.colorTextoBoton))
        findViewById<ImageView>(R.id.ImgPartidaTopoPausa).setImageResource(tema.topoNormal)

        val color = ContextCompat.getColor(this, tema.colorTexto)
        val textos = listOf<TextView>(lblPartidaPuntos, lblPartidaVidas, lblPartidaVelocidad,
                                      findViewById(R.id.LblPartidaEtiquetaVidas),
                                      findViewById(R.id.LblPartidaEtiquetaRecord),
                                      findViewById(R.id.LblPartidaRecord),
                                      findViewById(R.id.LblPartidaDificultad),
                                      findViewById(R.id.LblPartidaPausa),
                                      findViewById(R.id.LblPartidaAyuda),
                                      findViewById(R.id.LblPartidaAyudaNormal),
                                      findViewById(R.id.LblPartidaAyudaEnfadado),
                                      findViewById(R.id.LblPartidaAyudaDorado),
                                      findViewById(R.id.LblPartidaAyudaBomba))
        for(texto in textos)
        {
            texto.setTextColor(color)
        }
    }

    // Crea las filas de 3 agujeros inflando item_agujero.xml (como en las listas personalizadas)
    // Esta parte es de información externa que no sale en los vídeos
    // (addView: las vistas infladas se añaden a un LinearLayout desde el código, sin adapter,
    // porque el número de agujeros depende de la dificultad)
    private fun crearTablero()
    {
        val layPartidaTablero = findViewById<LinearLayout>(R.id.LayPartidaTablero)
        layPartidaTablero.removeAllViews()
        agujeros.clear()

        val filas = dificultad.agujeros / 3
        // Con una sola fila se pone un hueco arriba y otro abajo para que quede centrada
        if(filas == 1)
        {
            layPartidaTablero.addView(layoutInflater.inflate(R.layout.espacio, layPartidaTablero, false))
        }
        for(f in 1..filas)
        {
            val fila = layoutInflater.inflate(R.layout.fila_tablero, layPartidaTablero, false) as LinearLayout
            for(c in 1..3)
            {
                val vista = layoutInflater.inflate(R.layout.item_agujero, fila, false)
                val agujero = Agujero(vista)
                vista.setOnClickListener()
                {
                    tocar(agujero)
                }
                fila.addView(vista)
                agujeros.add(agujero)
            }
            layPartidaTablero.addView(fila)
        }
        if(filas == 1)
        {
            layPartidaTablero.addView(layoutInflater.inflate(R.layout.espacio, layPartidaTablero, false))
        }
    }

    // La leyenda de la ayuda: qué hace cada topo con los dibujos de la temática
    private fun prepararAyuda()
    {
        val tema = GestorAjustes.tema
        findViewById<ImageView>(R.id.ImgPartidaAyudaNormal).setImageResource(tema.topoNormal)
        findViewById<ImageView>(R.id.ImgPartidaAyudaEnfadado).setImageResource(tema.topoEnfadado)
        findViewById<ImageView>(R.id.ImgPartidaAyudaDorado).setImageResource(tema.topoDorado)
        findViewById<ImageView>(R.id.ImgPartidaAyudaBomba).setImageResource(tema.bomba)
        findViewById<TextView>(R.id.LblPartidaAyudaNormal).text =
            getString(R.string.ayuda_normal, ReglasJuego.PUNTOS_NORMAL)
        findViewById<TextView>(R.id.LblPartidaAyudaEnfadado).text =
            getString(R.string.ayuda_enfadado, ReglasJuego.PUNTOS_ENFADADO)
        findViewById<TextView>(R.id.LblPartidaAyudaDorado).text =
            getString(R.string.ayuda_dorado, ReglasJuego.PUNTOS_DORADO)
    }

    // =====================================================================
    // BUCLE DEL JUEGO (se ejecuta cada 40 ms)
    // =====================================================================

    private fun tick()
    {
        val ahora = SystemClock.elapsedRealtime()
        val pasado = ahora - ultimoTick
        ultimoTick = ahora

        if(estado == CUENTA_ATRAS)
        {
            avanzarCuentaAtras(pasado)
        }
        else if(estado == JUGANDO)
        {
            reloj = reloj + pasado
            acelerar()
            esconderTopos()
            if(estado == JUGANDO)
            {
                if(reloj >= proximaSalida && contarFuera() < toposALaVez)
                {
                    sacarTopo()
                    proximaSalida = reloj + saleCadaActual
                }
                // El mensaje del centro («¡COMBO x2!») se quita solo
                if(mensajeHasta > 0 && reloj >= mensajeHasta)
                {
                    lblPartidaMensaje.visibility = View.GONE
                    mensajeHasta = 0
                }
                actualizarHud()
            }
        }
    }

    // 3, 2, 1, ¡YA! antes de empezar
    private fun avanzarCuentaAtras(pasado: Long)
    {
        cuentaAtras = cuentaAtras - pasado
        lblPartidaMensaje.visibility = View.VISIBLE
        if(cuentaAtras > 2500)
        {
            ponerCuenta(getString(R.string.cuenta_3))
        }
        else if(cuentaAtras > 1500)
        {
            ponerCuenta(getString(R.string.cuenta_2))
        }
        else if(cuentaAtras > 500)
        {
            ponerCuenta(getString(R.string.cuenta_1))
        }
        else if(cuentaAtras > 0)
        {
            ponerCuenta(getString(R.string.cuenta_ya))
        }
        else
        {
            lblPartidaMensaje.visibility = View.GONE
            estado = JUGANDO
            proximaSalida = 0
        }
    }

    // Cambia el número de la cuenta atrás y suena un «bip» cada vez que cambia
    private fun ponerCuenta(texto: String)
    {
        if(lblPartidaMensaje.text.toString() != texto)
        {
            lblPartidaMensaje.text = texto
            if(texto == getString(R.string.cuenta_ya))
            {
                Sonidos.efecto(Sonidos.YA)
            }
            else
            {
                Sonidos.efecto(Sonidos.CUENTA)
            }
        }
    }

    // Cada segundo los topos se ven un poco menos y salen un poco antes:
    // tiempo = tiempo inicial x (1 - incremento/100) elevado a los segundos jugados
    // Esta parte es de información externa que no sale en los vídeos
    // (Math.pow y Math.max de Java para calcular la potencia y no bajar del mínimo)
    private fun acelerar()
    {
        val segundos = (reloj / 1000).toInt()
        val factor = Math.pow(1.0 - dificultad.incremento / 100.0, segundos.toDouble())
        visibleActual = Math.max(ReglasJuego.VISIBLE_MINIMO_MS, (ReglasJuego.VISIBLE_BASE_MS * factor).toLong())
        saleCadaActual = Math.max(ReglasJuego.SALE_CADA_MINIMO_MS, (ReglasJuego.SALE_CADA_BASE_MS * factor).toLong())
        lblPartidaVelocidad.text = getString(R.string.partida_velocidad, 1.0 / factor)
    }

    // Topos que se esconden (se han escapado) y golpes que terminan
    private fun esconderTopos()
    {
        for(a in agujeros)
        {
            if(a.estado == Agujero.GOLPEADO && reloj >= a.fin)
            {
                vaciar(a)
            }
            else if(a.estado == Agujero.FUERA && reloj >= a.fin)
            {
                if(a.tipo != Agujero.BOMBA && estado == JUGANDO)
                {
                    // Un topo se ha escapado: se rompe la racha, se pierde una vida
                    // y se burla un momento antes de esconderse (así el niño ve qué ha pasado)
                    escapados++
                    racha = 0
                    a.estado = Agujero.GOLPEADO
                    a.fin = reloj + 350
                    a.imgTopo.setImageResource(GestorAjustes.tema.topoBurlon)
                    perderVida()
                }
                else
                {
                    vaciar(a)
                }
            }
        }
    }

    private fun contarFuera(): Int
    {
        var total = 0
        for(a in agujeros)
        {
            if(a.estado != Agujero.VACIO)
            {
                total++
            }
        }
        return total
    }

    // Elige un agujero libre al azar y saca un topo (o una bomba)
    private fun sacarTopo()
    {
        val libres = mutableListOf<Agujero>()
        for(a in agujeros)
        {
            if(a.estado == Agujero.VACIO)
            {
                libres.add(a)
            }
        }

        if(libres.size > 0)
        {
            val a = libres[random.nextInt(libres.size)]
            val r = random.nextInt(100)
            if(r < probDorado)
            {
                a.tipo = Agujero.DORADO
            }
            else if(r < probDorado + probBomba)
            {
                a.tipo = Agujero.BOMBA
            }
            else if(r < probDorado + probBomba + probEnfadado)
            {
                a.tipo = Agujero.ENFADADO
            }
            else
            {
                a.tipo = Agujero.NORMAL
            }

            val tema = GestorAjustes.tema
            var visible = visibleActual
            when(a.tipo)
            {
                Agujero.DORADO ->
                {
                    a.golpesQuedan = 1
                    a.imgTopo.setImageResource(tema.topoDorado)
                    visible = visibleActual * 8 / 10          // el dorado se escapa antes
                }
                Agujero.BOMBA ->
                {
                    a.golpesQuedan = 1
                    a.imgTopo.setImageResource(tema.bomba)
                }
                Agujero.ENFADADO ->
                {
                    a.golpesQuedan = 2
                    a.imgTopo.setImageResource(tema.topoEnfadado)
                    visible = visibleActual * 13 / 10         // un poco más para los 2 golpes
                }
                else ->
                {
                    a.golpesQuedan = 1
                    a.imgTopo.setImageResource(tema.topoNormal)
                }
            }

            a.estado = Agujero.FUERA
            a.golpeado = false
            a.salio = reloj
            a.fin = reloj + visible
            mostrarTopo(a)
        }
    }

    // El topo sube desde el agujero
    // Esta parte es de información externa que no sale en los vídeos
    // (view.animate(): una animación de 0,12 s para que el topo «salte»)
    private fun mostrarTopo(a: Agujero)
    {
        a.imgTopo.animate().cancel()
        a.imgTopo.translationY = a.imgTopo.height * 0.5f
        a.imgTopo.visibility = View.VISIBLE
        a.imgTopo.animate().translationY(0f).setDuration(120).start()
        a.imgEfecto.visibility = View.INVISIBLE
        a.lblPuntos.visibility = View.INVISIBLE
    }

    private fun vaciar(a: Agujero)
    {
        a.imgTopo.animate().cancel()
        a.imgTopo.translationY = 0f
        a.estado = Agujero.VACIO
        a.imgTopo.visibility = View.INVISIBLE
        a.imgEfecto.visibility = View.INVISIBLE
        a.lblPuntos.visibility = View.INVISIBLE
    }

    // =====================================================================
    // TOQUES
    // =====================================================================

    private fun tocar(a: Agujero)
    {
        if(estado == JUGANDO)
        {
            if(a.estado == Agujero.VACIO)
            {
                // Agujero vacío: fallo. No resta puntos, pero rompe la racha.
                fallos++
                racha = 0
                Sonidos.efecto(Sonidos.FALLO)
            }
            else if(a.estado == Agujero.FUERA && a.tipo == Agujero.BOMBA)
            {
                tocarBomba(a)
            }
            else if(a.estado == Agujero.FUERA)
            {
                golpearTopo(a)
            }
            // Si ya está GOLPEADO (se ve el golpe) no pasa nada: un doble toque rápido no es un fallo
            actualizarHud()
        }
    }

    // Bomba: cuenta como error y quita una vida
    private fun tocarBomba(a: Agujero)
    {
        fallos++
        racha = 0
        Sonidos.efecto(Sonidos.BOMBA)
        a.estado = Agujero.GOLPEADO
        a.fin = reloj + 600
        a.imgTopo.setImageResource(GestorAjustes.tema.explosion)
        mostrarPuntos(a, getString(R.string.partida_menos_vida))
        perderVida()
    }

    private fun golpearTopo(a: Agujero)
    {
        aciertos++
        racha++
        if(racha > mejorRacha)
        {
            mejorRacha = racha
        }
        if(racha == ReglasJuego.ACIERTOS_PARA_COMBO)
        {
            mostrarMensaje(getString(R.string.partida_combo_mensaje, ReglasJuego.MULTIPLICADOR_COMBO))
            Sonidos.efecto(Sonidos.COMBO)
        }

        // Tiempo de reacción: desde que sale hasta el primer golpe
        if(!a.golpeado)
        {
            a.golpeado = true
            sumaReaccion = sumaReaccion + (reloj - a.salio)
            numReacciones++
        }
        a.golpesQuedan--

        if(a.golpesQuedan > 0)
        {
            // Enfadado después del primer golpe: se pone la tirita y aguanta un poquito más
            a.imgTopo.setImageResource(GestorAjustes.tema.topoEnfadadoTirita)
            Sonidos.efecto(Sonidos.GOLPE)
            a.fin = a.fin + 400
        }
        else
        {
            // Topo derrotado: puntos (dobles si hay combo)
            var base = ReglasJuego.PUNTOS_NORMAL
            if(a.tipo == Agujero.ENFADADO)
            {
                base = ReglasJuego.PUNTOS_ENFADADO
                Sonidos.efecto(Sonidos.GOLPE)
            }
            else if(a.tipo == Agujero.DORADO)
            {
                base = ReglasJuego.PUNTOS_DORADO
                doradosGolpeados++
                Sonidos.efecto(Sonidos.DORADO)
                // El dorado da una vida, sin pasar de las que había al empezar
                if(vidas < dificultad.vidas)
                {
                    vidas++
                }
            }
            else
            {
                Sonidos.efecto(Sonidos.GOLPE)
            }
            val ganados = base * multiplicador()
            puntos = puntos + ganados

            a.estado = Agujero.GOLPEADO
            a.fin = reloj + 450
            a.imgTopo.setImageResource(caraGolpeado(a.tipo))
            a.imgEfecto.visibility = View.VISIBLE
            mostrarPuntos(a, getString(R.string.partida_mas_puntos, ganados))
        }
    }

    // Cada tipo de topo tiene su cara de golpeado (el dorado sigue dorado y el enfadado sigue enfadado)
    private fun caraGolpeado(tipo: Int): Int
    {
        val tema = GestorAjustes.tema
        var cara = tema.topoGolpeado
        when(tipo)
        {
            Agujero.DORADO   -> cara = tema.topoDoradoGolpeado
            Agujero.ENFADADO -> cara = tema.topoEnfadadoGolpeado
        }
        return cara
    }

    private fun multiplicador(): Int
    {
        var veces = 1
        if(racha >= ReglasJuego.ACIERTOS_PARA_COMBO)
        {
            veces = ReglasJuego.MULTIPLICADOR_COMBO
        }
        return veces
    }

    private fun perderVida()
    {
        Sonidos.efecto(Sonidos.VIDA)
        vidas--
        if(vidas <= 0)
        {
            vidas = 0
            terminar()
        }
    }

    private fun mostrarPuntos(a: Agujero, texto: String)
    {
        a.lblPuntos.text = texto
        a.lblPuntos.visibility = View.VISIBLE
    }

    private fun mostrarMensaje(texto: String)
    {
        lblPartidaMensaje.text = texto
        lblPartidaMensaje.visibility = View.VISIBLE
        mensajeHasta = reloj + 900
    }

    // =====================================================================
    // BARRA DE ARRIBA
    // =====================================================================

    private fun actualizarHud()
    {
        lblPartidaPuntos.text = puntos.toString()
        lblPartidaVidas.text = getString(R.string.partida_vidas, vidas)
        if(vidas > 0)
        {
            imgPartidaCorazon.setImageResource(R.drawable.corazon_lleno)
        }
        else
        {
            imgPartidaCorazon.setImageResource(R.drawable.corazon_vacio)
        }

        if(racha >= ReglasJuego.ACIERTOS_PARA_COMBO)
        {
            lblPartidaCombo.text = getString(R.string.partida_combo, ReglasJuego.MULTIPLICADOR_COMBO)
        }
        else
        {
            lblPartidaCombo.text = getString(R.string.partida_racha, racha)
        }
    }

    // =====================================================================
    // PAUSA Y AYUDA
    // =====================================================================

    private fun pausar(conAyuda: Boolean)
    {
        if(estado != TERMINADA)
        {
            estado = PAUSA
            handler.removeCallbacks(bucle)      // en pausa el bucle se para (no gasta batería)
            lblPartidaMensaje.visibility = View.GONE
            if(conAyuda)
            {
                ayudas++
                layPartidaAyuda.visibility = View.VISIBLE
                layPartidaPausa.visibility = View.GONE
            }
            else
            {
                layPartidaPausa.visibility = View.VISIBLE
                layPartidaAyuda.visibility = View.GONE
            }
        }
    }

    private fun reanudar()
    {
        layPartidaPausa.visibility = View.GONE
        layPartidaAyuda.visibility = View.GONE
        ultimoTick = SystemClock.elapsedRealtime()
        // Si se pausó durante la cuenta atrás, se repite entera
        if(reloj == 0L)
        {
            lblPartidaMensaje.text = ""          // así el «3» vuelve a sonar
            cuentaAtras = ReglasJuego.CUENTA_ATRAS_MS
            estado = CUENTA_ATRAS
        }
        else
        {
            estado = JUGANDO
        }
        // Se vuelve a lanzar el bucle (removeCallbacks antes, por si ya estaba programado)
        handler.removeCallbacks(bucle)
        handler.post(bucle)
    }

    // =====================================================================
    // FIN DE LA PARTIDA
    // =====================================================================

    // Los datos de la partida tal como los pide POST api/sesiones
    private fun crearSesion(abandonada: Boolean): SesionNueva
    {
        var reaccion: Int? = null
        if(numReacciones > 0)
        {
            reaccion = (sumaReaccion / numReacciones).toInt()
        }
        val segundos = Math.max(1, (reloj / 1000).toInt())
        return SesionNueva(SharedData.MODO_INFINITO, segundos, fallos, puntos, dificultad.nombre,
                           aciertos + fallos, escapados, aciertos, mejorRacha, doradosGolpeados,
                           abandonada, reaccion, ayudas)
    }

    // Se han acabado las vidas: el resultado se enseña y se guarda en ResultadoActivity
    private fun terminar()
    {
        if(estado != TERMINADA)
        {
            estado = TERMINADA
            handler.removeCallbacks(bucle)
            Sonidos.efecto(Sonidos.FIN)

            val intent = Intent(this, ResultadoActivity::class.java)
            intent.putExtra(ResultadoActivity.resultadoConstants.SESION, crearSesion(false))
            intent.putExtra(ResultadoActivity.resultadoConstants.DIFICULTAD, dificultad)
            startActivity(intent)
            finish()
        }
    }

    // Salir a medias desde la pausa: se guarda como abandonada (sirve para el informe de abandono).
    // Se espera a que la API conteste antes de cerrar la pantalla: así, al volver a las dificultades,
    // la partida ya está guardada. Si ni siquiera ha empezado a jugar, no se guarda nada.
    private fun abandonar(reiniciar: Boolean)
    {
        val habiaEmpezado = reloj >= 1000
        estado = TERMINADA
        handler.removeCallbacks(bucle)
        findViewById<TextView>(R.id.BtnPartidaReiniciar).isEnabled = false
        findViewById<TextView>(R.id.BtnPartidaSalir).isEnabled = false
        findViewById<TextView>(R.id.BtnPartidaContinuar).isEnabled = false
        // Mientras la API contesta, la pausa dice «Guardando…» (así no parece que la tablet se ha colgado)
        findViewById<TextView>(R.id.LblPartidaPausa).text = getString(R.string.guardando)

        if(habiaEmpezado)
        {
            val sesion = crearSesion(true)
            val token = GestorSesion.cabecera(this)
            val sesionApi = RetrofitAddSesionClient.getInstance().create(ApiAddSesion::class.java)

            // GlobalScope.launch: lo de dentro se ejecuta en segundo plano. Las llamadas a la API tardan y,
            // si se hicieran en el hilo de la pantalla, la app se congelaría (Android no lo deja).
            // Dentro, call.execute() espera la respuesta y response.body() es el JSON ya pasado a objetos (null si hay error).
            GlobalScope.launch()
            {
                try
                {
                    val call = sesionApi.addSesion(token, sesion)
                    val response = call.execute()
                    if(response.body() != null)
                    {
                        // runOnUiThread: las vistas solo se pueden cambiar desde el hilo de la pantalla, no desde el launch.
                        // pantallaActiva() comprueba que el niño no ha salido de la pantalla mientras llegaba la respuesta.
                        runOnUiThread()
                        {
                            if(pantallaActiva())
                            {
                                cerrarPartida(reiniciar)
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
                                Avisos.errorApi(this@PartidaActivity, codigo)
                                cerrarPartida(reiniciar)
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
                            Avisos.errorConexion(this@PartidaActivity)
                            cerrarPartida(reiniciar)
                        }
                    }
                }
            }
        }
        else
        {
            cerrarPartida(reiniciar)
        }
    }

    // Después de abandonar: otra partida igual (Reiniciar) o volver a las dificultades (Salir)
    private fun cerrarPartida(reiniciar: Boolean)
    {
        if(reiniciar && !isFinishing)
        {
            val otra = Intent(this, PartidaActivity::class.java)
            otra.putExtra(partidaConstants.DIFICULTAD, dificultad)
            startActivity(otra)
        }
        finish()
    }
}
