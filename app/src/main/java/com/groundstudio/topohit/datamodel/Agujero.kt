package com.groundstudio.topohit.datamodel

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.groundstudio.topohit.R

// Un agujero del tablero y lo que hay dentro en cada momento.
// Las vistas se buscan una sola vez al crearlo, como en un ViewHolder (REGLA-0489-052).
// Los tiempos son milisegundos del reloj de la partida (que se para en la pausa).
class Agujero(val view: View)
{
    val imgTopo: ImageView
    val imgEfecto: ImageView
    val lblPuntos: TextView

    var estado = VACIO
    var tipo = NORMAL
    var golpesQuedan = 0        // el enfadado necesita 2 golpes
    var golpeado = false        // si ya ha recibido algún golpe (para el tiempo de reacción)
    var salio = 0L              // cuándo salió el topo
    var fin = 0L                // cuándo se esconde (o cuándo acaba el efecto del golpe)

    init
    {
        imgTopo   = view.findViewById(R.id.ImgListAgujeroTopo)
        imgEfecto = view.findViewById(R.id.ImgListAgujeroEfecto)
        lblPuntos = view.findViewById(R.id.LblListAgujeroPuntos)
    }

    companion object
    {
        // Estados
        const val VACIO = 0         // no hay nada
        const val FUERA = 1         // hay un topo o una bomba que se puede tocar
        const val GOLPEADO = 2      // se está viendo el golpe o la explosión

        // Tipos
        const val NORMAL = 0
        const val ENFADADO = 1
        const val DORADO = 2
        const val BOMBA = 3
    }
}
