package com.groundstudio.topohit.util

import com.groundstudio.topohit.api.RetrofitClient
import com.groundstudio.topohit.data_model.DificultadContrarreloj
import com.groundstudio.topohit.data_model.DificultadInfinito
import com.groundstudio.topohit.data_model.NivelHistoria
import com.groundstudio.topohit.data_model.Progreso
import com.groundstudio.topohit.data_model.ProgresoNivel
<<<<<<< HEAD
=======
import com.groundstudio.topohit.data_model.RecordModo
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
import com.groundstudio.topohit.data_model.Tematica
import com.groundstudio.topohit.data_model.Tematicas
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Datos que comparten todas las pantallas mientras la app está abierta:
// quién ha entrado, la temática, la configuración de los modos y el progreso.
// (No hemos visto ViewModel: usamos un "object", que solo existe una vez en toda la app.)
object Juego {

    // ---------- Constantes de los modos (como en la tabla MODO de la BD) ----------
    const val HISTORIA = "Historia"
    const val CONTRARRELOJ = "Contrarreloj"
    const val INFINITO = "Infinito"

    fun idModo(modo: String): Int {
        return when (modo) {
            HISTORIA -> 1
            CONTRARRELOJ -> 2
            else -> 3
        }
    }

    // ---------- Puntos (página "Ajustes del juego" del Figma) ----------
    const val PUNTOS_NORMAL = 10
    const val PUNTOS_ENFADADO = 25
    const val PUNTOS_DORADO = 50
    const val PUNTOS_BOMBA = -20
    const val PUNTOS_GOLPE_JEFE = 10
    const val PUNTOS_JEFE_VENCIDO = 100
    const val ACIERTOS_PARA_COMBO = 5
    const val MULTIPLICADOR_COMBO = 2
    const val GOLPES_JEFE = 10

    // ---------- Usuario ----------
    var token = ""
    var idUsuario = 0
    var username = ""
    var rol = ""
<<<<<<< HEAD
=======
    var invitado = false         // "Probar sin conexión": se juega pero no se guarda nada
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
    var avatar = 0               // posición en Tematicas.AVATARES
    var tema: Tematica = Tematicas.NORMAL

    fun cabeceraToken(): String {
        return "Bearer " + token
    }

    fun cerrarSesion() {
        token = ""
        idUsuario = 0
        username = ""
        rol = ""
<<<<<<< HEAD
=======
        invitado = false
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        avatar = 0
        progreso = null
    }

    // ---------- Configuración de los modos ----------
    // Valores del GDD. Si la API responde, se sustituyen por los de la base de datos
    // (así las educadoras los pueden cambiar desde el escritorio).

    var niveles: List<NivelHistoria> = listOf(
        NivelHistoria(1, "Campo", 3, 150, 45, "Solo topos normales"),
        NivelHistoria(2, "Campo", 3, 200, 45, ""),
        NivelHistoria(3, "Campo", 3, 250, 45, "Aparece el topo dorado"),
        NivelHistoria(4, "Huerto", 6, 300, 45, "Entra el topo enfadado"),
        NivelHistoria(5, "Huerto", 6, 400, 45, ""),
        NivelHistoria(6, "Huerto", 6, 450, 45, "Entran las bombas"),
        NivelHistoria(7, "Huerto", 6, 500, 50, ""),
        NivelHistoria(8, "Mina", 9, 600, 50, "9 agujeros"),
        NivelHistoria(9, "Mina", 9, 700, 50, "Velocidad de Difícil"),
        NivelHistoria(10, "Mina", 9, 0, 60, "Topo gigante: 10 golpes y salta de agujero en agujero")
    )

    var contrarreloj: List<DificultadContrarreloj> = listOf(
        DificultadContrarreloj("Fácil", 3, 60, 300, 1500, 1200, 1),
        DificultadContrarreloj("Medio", 6, 60, 500, 1100, 900, 2),
        DificultadContrarreloj("Difícil", 9, 60, 800, 800, 600, 3)
    )

    var infinito: List<DificultadInfinito> = listOf(
        DificultadInfinito("Fácil", 3, 5, 1),
        DificultadInfinito("Normal", 6, 3, 2),
        DificultadInfinito("Difícil", 9, 3, 3)
    )

    // Pide la configuración a la API (no hace falta token). Si falla, se quedan los valores del GDD.
    @OptIn(DelicateCoroutinesApi::class)
    fun cargarConfiguracion() {
        GlobalScope.launch {
            try {
                val api = RetrofitClient.api()
                val r1 = api.getNiveles().execute()
                val lista1 = r1.body()
                if (r1.isSuccessful && lista1 != null && lista1.isNotEmpty()) {
                    niveles = lista1
                }
                val r2 = api.getContrarreloj().execute()
                val lista2 = r2.body()
                if (r2.isSuccessful && lista2 != null && lista2.isNotEmpty()) {
                    contrarreloj = lista2
                }
                val r3 = api.getInfinito().execute()
                val lista3 = r3.body()
                if (r3.isSuccessful && lista3 != null && lista3.isNotEmpty()) {
                    infinito = lista3
                }
            } catch (e: Exception) {
                e.printStackTrace()     // sin conexión: seguimos con los valores del GDD
            }
        }
    }

    fun buscarNivel(numero: Int): NivelHistoria {
        for (n in niveles) {
            if (n.numero == numero) {
                return n
            }
        }
        return niveles[0]
    }

    fun buscarContrarreloj(nombre: String): DificultadContrarreloj {
        for (d in contrarreloj) {
            if (d.nombre == nombre) {
                return d
            }
        }
        return contrarreloj[0]
    }

    fun buscarInfinito(nombre: String): DificultadInfinito {
        for (d in infinito) {
            if (d.nombre == nombre) {
                return d
            }
        }
        return infinito[0]
    }

    // ---------- Progreso ----------
<<<<<<< HEAD
    // Lo que devuelve GET api/sesiones/progreso: una fila (ProgresoNivel) por cada nivel de Historia
    // y por cada dificultad de Contrarreloj e Infinito. Las filas de Historia llevan el número de
    // nivel en levelReached; las otras, el nombre de la dificultad en difficulty.

    var progreso: Progreso? = null

    // Si todavía no tenemos el progreso (por ejemplo, si la API no responde), empezamos uno vacío
    fun progresoSeguro(): Progreso {
        if (progreso == null) {
            progreso = Progreso(mutableListOf(), 0, null)
            progreso!!.nivelActual = primerNivel()
=======

    var progreso: Progreso? = null

    // Si todavía no tenemos el progreso (sin conexión o invitado), empezamos uno vacío
    fun progresoSeguro(): Progreso {
        if (progreso == null) {
            progreso = Progreso(1, mutableListOf(), mutableListOf())
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        }
        if (progreso!!.niveles == null) {
            progreso!!.niveles = mutableListOf()
        }
<<<<<<< HEAD
        return progreso!!
    }

    private fun primerNivel(): Int {
        if (niveles.isEmpty()) {
            return 1
        }
        return niveles[0].numero
    }

    // Busca la fila de Historia de un nivel (null si no hay)
    private fun filaNivel(numero: Int): ProgresoNivel? {
        for (n in progresoSeguro().niveles!!) {
            if (n.modeId == 1 && n.levelReached == numero) {
                return n
            }
        }
        return null
    }

    // Busca la fila de una dificultad de Contrarreloj o Infinito (null si no hay)
    private fun filaDificultad(modo: String, dificultad: String): ProgresoNivel? {
        for (n in progresoSeguro().niveles!!) {
            if (n.modeId == idModo(modo) && n.difficulty == dificultad) {
                return n
            }
        }
        return null
    }

    // El nivel más alto que ya se puede jugar (los anteriores también están abiertos)
    fun nivelDesbloqueado(): Int {
        var mayor = primerNivel()
        for (n in progresoSeguro().niveles!!) {
            val numero = n.levelReached
            if (n.modeId == 1 && n.abierto && numero != null && numero > mayor) {
                mayor = numero
            }
        }
        return mayor
    }

    // Nivel por el que va el niño ("estás aquí"). Si ha completado la Historia, el último.
    fun nivelActual(): Int {
        val actual = progresoSeguro().nivelActual
        if (actual == null) {
            return niveles[niveles.size - 1].numero
        }
        return actual
    }

    fun estrellasDeNivel(numero: Int): Int {
        val fila = filaNivel(numero)
        if (fila == null) {
            return 0
        }
        return fila.estrellas
    }

    // Solo cuentan las estrellas de Historia (como en la API)
    fun totalEstrellas(): Int {
        var total = 0
        for (n in progresoSeguro().niveles!!) {
            if (n.modeId == 1) {
                total = total + n.estrellas
            }
=======
        if (progreso!!.records == null) {
            progreso!!.records = mutableListOf()
        }
        return progreso!!
    }

    fun nivelDesbloqueado(): Int {
        return progresoSeguro().nivelDesbloqueado
    }

    fun estrellasDeNivel(numero: Int): Int {
        for (n in progresoSeguro().niveles!!) {
            if (n.numero == numero) {
                return n.estrellas
            }
        }
        return 0
    }

    fun totalEstrellas(): Int {
        var total = 0
        for (n in progresoSeguro().niveles!!) {
            total = total + n.estrellas
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        }
        return total
    }

    // Récord de un modo: en Historia es la mejor puntuación de un nivel ("Nivel 3")
    fun record(modo: String, dificultad: String): Int {
<<<<<<< HEAD
        var fila: ProgresoNivel? = null
        if (modo == HISTORIA) {
            val numero = dificultad.replace("Nivel ", "").toIntOrNull()
            if (numero != null) {
                fila = filaNivel(numero)
            }
        } else {
            fila = filaDificultad(modo, dificultad)
        }
        if (fila == null || fila.record == null) {
            return 0
        }
        return fila.record!!
    }

    // El mejor récord de Contrarreloj e Infinito (para el menú)
    fun mejorRecord(): Int {
        var mejor = 0
        for (n in progresoSeguro().niveles!!) {
            val record = n.record
            if (n.modeId != 1 && record != null && record > mejor) {
                mejor = record
=======
        if (modo == HISTORIA) {
            for (n in progresoSeguro().niveles!!) {
                if ("Nivel " + n.numero == dificultad) {
                    return n.mejorPuntuacion
                }
            }
            return 0
        }
        for (r in progresoSeguro().records!!) {
            if (r.modo.equals(modo, true) && r.dificultad == dificultad) {
                return r.mejorPuntuacion
            }
        }
        return 0
    }

    fun mejorRecord(): Int {
        var mejor = 0
        for (r in progresoSeguro().records!!) {
            if (r.mejorPuntuacion > mejor) {
                mejor = r.mejorPuntuacion
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
            }
        }
        return mejor
    }

    // Las mismas estrellas que calcula la API: 45 %, 70 % y 100 % del objetivo
    fun calcularEstrellas(objetivo: Int, puntos: Int): Int {
        if (objetivo == 0) {
            return 3            // nivel jefe
        }
        val porcentaje = puntos * 100 / objetivo
        if (porcentaje >= 100) return 3
        if (porcentaje >= 70) return 2
        if (porcentaje >= 45) return 1
        return 0
    }

<<<<<<< HEAD
    // Apunta una partida en el progreso de la tablet, para verla al momento sin esperar a la API
    // (al volver al menú se vuelve a pedir el progreso). Devuelve true si es un récord nuevo.
    fun apuntarPartida(modo: String, dificultad: String?, nivel: Int, puntos: Int, estrellas: Int): Boolean {
        val p = progresoSeguro()
        var fila: ProgresoNivel? = null
        if (modo == HISTORIA) {
            fila = filaNivel(nivel)
            if (fila == null) {
                fila = ProgresoNivel(1, null, nivel, true, 0, null, 0)
                p.niveles!!.add(fila)
            }
        } else if (dificultad != null) {
            fila = filaDificultad(modo, dificultad)
            if (fila == null) {
                fila = ProgresoNivel(idModo(modo), dificultad, null, true, 0, null, 0)
                p.niveles!!.add(fila)
            }
        }
        if (fila == null) {
            return false
        }

        fila.partidas = fila.partidas + 1
        var nuevoRecord = false
        val anterior = fila.record
        if (anterior == null || puntos > anterior) {
            nuevoRecord = anterior != null && anterior > 0
            fila.record = puntos
        }

        if (modo == HISTORIA) {
            if (estrellas > fila.estrellas) {
                fila.estrellas = estrellas
            }
            // Con 1 estrella o más se abre el nivel siguiente de la lista
            if (estrellas >= 1) {
                abrirSiguiente(nivel)
            }
            p.nivelActual = calcularNivelActual()
        }
        return nuevoRecord
    }

    // Abre el nivel que va después de "numero" en la lista de niveles
    private fun abrirSiguiente(numero: Int) {
        for (i in 0..niveles.size - 2) {
            if (niveles[i].numero == numero) {
                val siguiente = niveles[i + 1].numero
                var fila = filaNivel(siguiente)
                if (fila == null) {
                    fila = ProgresoNivel(1, null, siguiente, true, 0, null, 0)
                    progresoSeguro().niveles!!.add(fila)
                }
                fila.abierto = true
            }
        }
    }

    // Igual que la API: el primer nivel que todavía no tiene ninguna estrella (null = todos superados)
    private fun calcularNivelActual(): Int? {
        for (n in niveles) {
            if (estrellasDeNivel(n.numero) < 1) {
                return n.numero
            }
        }
        return null
    }
=======
    // Apunta una partida en el progreso de la tablet, para no tener que volver a pedirlo a la API.
    // Devuelve true si es un récord nuevo.
    fun apuntarPartida(modo: String, dificultad: String?, nivel: Int, puntos: Int, estrellas: Int): Boolean {
        val p = progresoSeguro()
        var nuevoRecord = false
        if (modo == HISTORIA) {
            var encontrado: ProgresoNivel? = null
            for (n in p.niveles!!) {
                if (n.numero == nivel) {
                    encontrado = n
                }
            }
            if (encontrado == null) {
                encontrado = ProgresoNivel(nivel, buscarNivel(nivel).objetivo, 0, 0)
                p.niveles!!.add(encontrado)
            }
            if (puntos > encontrado.mejorPuntuacion) {
                nuevoRecord = encontrado.mejorPuntuacion > 0
                encontrado.mejorPuntuacion = puntos
            }
            if (estrellas > encontrado.estrellas) {
                encontrado.estrellas = estrellas
            }
            // Con 1 estrella o más se abre el siguiente nivel
            if (estrellas >= 1 && nivel == p.nivelDesbloqueado && nivel < niveles.size) {
                p.nivelDesbloqueado = nivel + 1
            }
        } else if (dificultad != null) {
            var encontrado: RecordModo? = null
            for (r in p.records!!) {
                if (r.modo.equals(modo, true) && r.dificultad == dificultad) {
                    encontrado = r
                }
            }
            if (encontrado == null) {
                encontrado = RecordModo(modo, dificultad, 0)
                p.records!!.add(encontrado)
            }
            if (puntos > encontrado.mejorPuntuacion) {
                nuevoRecord = encontrado.mejorPuntuacion > 0
                encontrado.mejorPuntuacion = puntos
            }
        }
        return nuevoRecord
    }
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
}
