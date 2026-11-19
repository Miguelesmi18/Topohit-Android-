package com.groundstudio.topohit.data_model

import com.google.gson.annotations.SerializedName

// Clases de datos que viajan en JSON entre la tablet y la API (vídeos 35 y 36).
// La API de ASP.NET escribe las claves en camelCase ("finalScore", "idUsuario"...).

// Lo que se envía a api/auth/login y a api/auth/registro.
// La contraseña NO viaja tal cual: se envía su hash bcrypt (ver util/Cifrado.kt).
data class LoginPeticion(
    @SerializedName("username") val username: String,
    @SerializedName("passwordHash") val passwordHash: String
)

// CAMBIO 19/11: lo que se envía a api/auth/registro. Además del usuario y el hash
// va el avatar elegido (id de la tabla AVATAR, de 1 a 12).
data class RegistroPeticion(
    @SerializedName("username") val username: String,
    @SerializedName("passwordHash") val passwordHash: String,
    @SerializedName("avatarId") val avatarId: Int
)

// Lo que devuelve la API si el login o el registro van bien
data class LoginRespuesta(
    @SerializedName("token") val token: String,
    @SerializedName("expira") val expira: String,
    @SerializedName("idUsuario") val idUsuario: Int,
    @SerializedName("username") val username: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("avatarId") val avatarId: Int        // CAMBIO 19/11: el avatar guardado en la BD
)

// CAMBIO 19/11: PUT api/auth/avatar (cambiar el avatar desde el Perfil)
data class AvatarPeticion(
    @SerializedName("avatarId") val avatarId: Int
)

data class AvatarRespuesta(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("avatarId") val avatarId: Int
)

// Cuando algo va mal la API responde { "mensaje": "..." }
data class MensajeError(
    @SerializedName("mensaje") val mensaje: String?
)

// GET api/modos/historia
data class NivelHistoria(
    @SerializedName("numero") val numero: Int,
    @SerializedName("zona") val zona: String,
    @SerializedName("agujeros") val agujeros: Int,
    @SerializedName("objetivo") val objetivo: Int,     // 0 = nivel jefe
    @SerializedName("tiempo") val tiempo: Int,
    @SerializedName("novedad") val novedad: String?
)

// GET api/modos/contrarreloj
data class DificultadContrarreloj(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("agujeros") val agujeros: Int,
    @SerializedName("tiempo") val tiempo: Int,
    @SerializedName("objetivo") val objetivo: Int,
    @SerializedName("tiempoVisibleMs") val tiempoVisibleMs: Int,
    @SerializedName("saleCadaMs") val saleCadaMs: Int,
    @SerializedName("toposALaVez") val toposALaVez: Int
)

// GET api/modos/infinito
data class DificultadInfinito(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("agujeros") val agujeros: Int,
    @SerializedName("vidas") val vidas: Int,
    @SerializedName("incremento") val incremento: Int    // % que acelera cada segundo
)

// POST api/sesiones: lo que enviamos al acabar una partida.
// Reglas de la API: nAttempts = hits + errorsMade; en Historia difficulty = null;
// en Contrarreloj e Infinito levelReached = null.
data class SesionNueva(
    @SerializedName("modeId") val modeId: Int,             // 1 Historia, 2 Contrarreloj, 3 Infinito
    @SerializedName("sessionLength") val sessionLength: Int,
    @SerializedName("errorsMade") val errorsMade: Int,
    @SerializedName("finalScore") val finalScore: Int,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("levelReached") val levelReached: Int?,
    @SerializedName("nAttempts") val nAttempts: Int,
    @SerializedName("molesEscaped") val molesEscaped: Int,
    @SerializedName("hits") val hits: Int,
    @SerializedName("maxCombo") val maxCombo: Int,
    @SerializedName("goldenHits") val goldenHits: Int,
    @SerializedName("abandoned") val abandoned: Boolean,
    @SerializedName("avgReactionMs") val avgReactionMs: Int?
)

// Respuesta de POST api/sesiones
data class SesionCreada(
    @SerializedName("sessionId") val sessionId: Int
)

// Una partida guardada (GET api/sesiones/mias)
data class Sesion(
    @SerializedName("sessionId") val sessionId: Int,
    @SerializedName("modo") val modo: String?,
    @SerializedName("dateTime") val dateTime: String?,
    @SerializedName("sessionLength") val sessionLength: Int,
    @SerializedName("errorsMade") val errorsMade: Int,
    @SerializedName("finalScore") val finalScore: Int,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("levelReached") val levelReached: Int?,
    @SerializedName("hits") val hits: Int,
    @SerializedName("nAttempts") val nAttempts: Int,
    @SerializedName("accuracy") val accuracy: Double?,
    @SerializedName("abandoned") val abandoned: Boolean
)

// GET api/sesiones/progreso: por dónde va el jugador.
// La API devuelve una fila por cada nivel activo de Historia y por cada dificultad
// de Contrarreloj e Infinito, con sus estrellas y su récord.
class Progreso(
    @SerializedName("niveles") var niveles: MutableList<ProgresoNivel>?,
    @SerializedName("estrellasTotales") var estrellasTotales: Int,
    @SerializedName("nivelActual") var nivelActual: Int?      // null = Historia completada
)

class ProgresoNivel(
    @SerializedName("modeId") val modeId: Int,                // 1 Historia, 2 Contrarreloj, 3 Infinito
    @SerializedName("difficulty") val difficulty: String?,    // solo Contrarreloj e Infinito
    @SerializedName("levelReached") val levelReached: Int?,   // solo Historia: el número de nivel
    @SerializedName("abierto") var abierto: Boolean,
    @SerializedName("estrellas") var estrellas: Int,          // solo cuentan en Historia
    @SerializedName("record") var record: Int?,               // null = aún no ha jugado
    @SerializedName("partidas") var partidas: Int
)
