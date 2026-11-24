package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Lo que devuelve el script de Python estadisticas_jugador.py (en JSON): los números del niño
// y sus gráficas. Si todavía no ha jugado, partidas = 0, los números llegan a null y no hay gráficas.
data class MetricasJugador (@SerializedName("partidas") var partidas: Int,
                            @SerializedName("abandonadas") var abandonadas: Int?,
                            @SerializedName("mejor_puntuacion") var mejorPuntuacion: Int?,
                            @SerializedName("puntuacion_media") var puntuacionMedia: Int?,
                            @SerializedName("duracion_media") var duracionMedia: Int?,
                            @SerializedName("minutos_jugados") var minutosJugados: Int?,
                            @SerializedName("precision") var precision: Int?,
                            @SerializedName("ayudas_por_partida") var ayudasPorPartida: Double?,
                            @SerializedName("dias_jugados") var diasJugados: Int?,
                            @SerializedName("graficas") var graficas: List<Grafica>)
