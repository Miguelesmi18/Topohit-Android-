package com.groundstudio.topohit

// Los números del juego (página «Ajustes del juego» del Figma).
// Lo que cambia por dificultad (agujeros, vidas y cuánto acelera) viene de la API,
// así el escritorio lo puede cambiar. Aquí solo está lo que es igual siempre.
object ReglasJuego
{
    // Puntos
    const val PUNTOS_NORMAL = 10
    const val PUNTOS_ENFADADO = 25
    const val PUNTOS_DORADO = 50

    // Combo: con 5 aciertos seguidos los puntos valen el doble hasta el siguiente fallo o escape
    const val ACIERTOS_PARA_COMBO = 5
    const val MULTIPLICADOR_COMBO = 2

    // Velocidad de salida: empieza como Fácil y va acelerando (incremento de la API, % por segundo)
    const val VISIBLE_BASE_MS = 1500L       // cuánto se ve cada topo al empezar
    const val SALE_CADA_BASE_MS = 1200L     // cada cuánto sale uno al empezar
    const val VISIBLE_MINIMO_MS = 450L      // nunca más rápido que esto
    const val SALE_CADA_MINIMO_MS = 350L

    // Cuenta atrás antes de empezar (3, 2, 1, ¡YA!)
    const val CUENTA_ATRAS_MS = 3500L

    // Topos que pueden estar fuera a la vez según la dificultad (orden 1, 2 o 3)
    fun toposALaVez(orden: Int): Int
    {
        var topos = 1
        when(orden)
        {
            2 -> topos = 2
            3 -> topos = 3
        }
        return topos
    }

    // De cada 100 apariciones, cuántas son topos enfadados
    fun probEnfadado(orden: Int): Int
    {
        var prob = 15
        when(orden)
        {
            2 -> prob = 20
            3 -> prob = 25
        }
        return prob
    }

    // De cada 100 apariciones, cuántas son bombas (en Fácil no hay)
    fun probBomba(orden: Int): Int
    {
        var prob = 0
        when(orden)
        {
            2 -> prob = 12
            3 -> prob = 22
        }
        return prob
    }

    // De cada 100 apariciones, cuántas son topos dorados (igual en todas)
    fun probDorado(): Int = 4
}
