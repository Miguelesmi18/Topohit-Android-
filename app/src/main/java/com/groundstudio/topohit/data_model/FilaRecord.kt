package com.groundstudio.topohit.data_model

// Una fila de la pantalla "Mis récords"
data class FilaRecord(
    val nombre: String,         // "Fácil", "Nivel 3"...
    val detalle: String,        // "3 agujeros", "★★☆"...
    val puntos: Int,
    val icono: Int              // dibujo de la izquierda
)
