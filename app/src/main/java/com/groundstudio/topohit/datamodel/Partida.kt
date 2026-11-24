package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Una partida guardada del jugador (GET api/sesiones/mias), para el historial del Perfil
data class Partida (@SerializedName("sessionId") var sessionId: Int,
                    @SerializedName("modo") var modo: String?,
                    @SerializedName("dateTime") var dateTime: String?,
                    @SerializedName("sessionLength") var sessionLength: Int,
                    @SerializedName("errorsMade") var errorsMade: Int,
                    @SerializedName("finalScore") var finalScore: Int,
                    @SerializedName("difficulty") var difficulty: String?,
                    @SerializedName("hits") var hits: Int,
                    @SerializedName("nAttempts") var nAttempts: Int,
                    @SerializedName("abandoned") var abandoned: Boolean)
