package com.groundstudio.topohit.datamodel

// Una temática visual del juego (Normal, Halloween o Navidad).
// Cambia los dibujos y los colores, pero la forma de jugar es la misma.
// Todos los Int son recursos: R.drawable... o R.color...
// colorTextoBoton y colorTextoBotonSecundario: el texto de los botones (marrón sobre el naranja y el verde,
// que con blanco no se leen bien)
class Tematica (val nombre: String,
                val fondo: Int,
                val logo: Int,
                val topoNormal: Int,
                val topoGolpeado: Int,
                val topoDoradoGolpeado: Int,
                val topoEnfadadoGolpeado: Int,
                val topoEnfadado: Int,
                val topoEnfadadoTirita: Int,
                val topoBurlon: Int,
                val topoDorado: Int,
                val bomba: Int,
                val explosion: Int,
                val botonPrincipal: Int,
                val botonSecundario: Int,
                val panel: Int,
                val circulo: Int,
                val colorTexto: Int,
                val colorTextoBoton: Int,
                val colorTextoBotonSecundario: Int)
