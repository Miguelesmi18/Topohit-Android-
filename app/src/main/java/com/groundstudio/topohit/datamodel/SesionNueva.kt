package com.groundstudio.topohit.datamodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Lo que se envía a POST api/sesiones al acabar una partida.
// Reglas de la API: modeId = 3 (Infinito), difficulty con tilde, levelReached = null,
// nAttempts = hits + errorsMade y avgReactionMs = null si no se ha golpeado ningún topo.
// Es Serializable porque PartidaActivity se la pasa a ResultadoActivity, que es quien la envía.
data class SesionNueva (@SerializedName("modeId") var modeId: Int,
                        @SerializedName("sessionLength") var sessionLength: Int,
                        @SerializedName("errorsMade") var errorsMade: Int,
                        @SerializedName("finalScore") var finalScore: Int,
                        @SerializedName("difficulty") var difficulty: String,
                        @SerializedName("levelReached") var levelReached: Int?,
                        @SerializedName("nAttempts") var nAttempts: Int,
                        @SerializedName("molesEscaped") var molesEscaped: Int,
                        @SerializedName("hits") var hits: Int,
                        @SerializedName("maxCombo") var maxCombo: Int,
                        @SerializedName("goldenHits") var goldenHits: Int,
                        @SerializedName("abandoned") var abandoned: Boolean,
                        @SerializedName("avgReactionMs") var avgReactionMs: Int?) : Serializable
