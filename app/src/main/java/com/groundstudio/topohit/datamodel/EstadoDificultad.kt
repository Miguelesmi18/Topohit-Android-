package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Una dificultad de Infinito tal como está para el jugador (GET api/sesiones/dificultades).
// La API decide si está abierta: la primera siempre; las otras, cuando el récord en la
// anterior llega a puntosDesbloqueo. Si el tutor la ha bloqueado, bloqueadaTutor = true
// y motivo trae lo que ha escrito (o null).
// Es Serializable porque viaja en el Intent de la partida y del resultado (REGLA-0489-043).
data class EstadoDificultad (@SerializedName("nombre") var nombre: String,
                             @SerializedName("orden") var orden: Int,
                             @SerializedName("agujeros") var agujeros: Int,
                             @SerializedName("vidas") var vidas: Int,
                             @SerializedName("incremento") var incremento: Int,
                             @SerializedName("puntosDesbloqueo") var puntosDesbloqueo: Int,
                             @SerializedName("dificultadAnterior") var dificultadAnterior: String?,
                             @SerializedName("recordAnterior") var recordAnterior: Int,
                             @SerializedName("abierta") var abierta: Boolean,
                             @SerializedName("bloqueadaTutor") var bloqueadaTutor: Boolean,
                             @SerializedName("motivo") var motivo: String?,
                             @SerializedName("record") var record: Int?,
                             @SerializedName("partidas") var partidas: Int) : Serializable
