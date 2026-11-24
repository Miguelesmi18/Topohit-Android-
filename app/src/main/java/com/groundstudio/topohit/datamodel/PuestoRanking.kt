package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName

// Un puesto del ranking (GET api/sesiones/ranking): el mejor récord de cada jugador.
// En el ranking total (GET api/sesiones/ranking/total) mejorPuntuacion es la suma de sus récords y
// recordFacil, recordNormal y recordDificil el de cada dificultad (null si no la ha jugado).
// En el ranking de una dificultad esos tres llegan a null.
data class PuestoRanking (@SerializedName("idUsuario") var idUsuario: Int,
                          @SerializedName("username") var username: String,
                          @SerializedName("avatarId") var avatarId: Int,
                          @SerializedName("mejorPuntuacion") var mejorPuntuacion: Int,
                          @SerializedName("partidas") var partidas: Int,
                          @SerializedName("recordFacil") var recordFacil: Int?,
                          @SerializedName("recordNormal") var recordNormal: Int?,
                          @SerializedName("recordDificil") var recordDificil: Int?)
