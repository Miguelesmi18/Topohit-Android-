package com.groundstudio.topohit.data_model

import com.groundstudio.topohit.databinding.ItemAgujeroBinding

// Un agujero del tablero y lo que hay dentro en cada momento.
// Los tiempos son milisegundos del reloj de la partida (que se para en la pausa).
class Agujero(val vista: ItemAgujeroBinding) {

    var estado = VACIO
    var tipo = NORMAL
    var golpesQuedan = 0        // el enfadado necesita 2 golpes y el jefe 10
    var golpeado = false        // si ya ha recibido algún golpe (para el tiempo de reacción)
    var salio = 0L              // cuándo salió el topo
    var fin = 0L                // cuándo se esconde (o cuándo acaba el efecto del golpe)

    companion object {
        // Estados
        const val VACIO = 0         // no hay nada
        const val FUERA = 1         // hay un topo o una bomba que se puede tocar
        const val GOLPEADO = 2      // se está viendo el golpe o la explosión

        // Tipos
        const val NORMAL = 0
        const val ENFADADO = 1
        const val DORADO = 2
        const val BOMBA = 3
        const val JEFE = 4
    }
}
