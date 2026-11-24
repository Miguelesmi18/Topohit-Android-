package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Un puesto del ranking (GET api/sesiones/ranking): el mejor récord de cada jugador
data class PuestoRanking (@SerializedName("idUsuario") var idUsuario: Int,
                          @SerializedName("username") var username: String,
                          @SerializedName("avatarId") var avatarId: Int,
                          @SerializedName("mejorPuntuacion") var mejorPuntuacion: Int,
                          @SerializedName("partidas") var partidas: Int)
