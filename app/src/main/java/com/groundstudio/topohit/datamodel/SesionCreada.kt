package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Respuesta de POST api/sesiones: el número de la partida guardada
data class SesionCreada (@SerializedName("sessionId") var sessionId: Int)
