package com.groundstudio.topohit.util

import android.content.Context
<<<<<<< HEAD
=======
import com.groundstudio.topohit.api.RetrofitClient
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
import com.groundstudio.topohit.data_model.Tematicas
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

// Guarda en la tablet lo que hay que recordar al cerrar la app:
<<<<<<< HEAD
// la sesión (token), los ajustes (temática y sonido) y el avatar de cada jugador.
=======
// la sesión (token), los ajustes (temática y servidor) y el avatar de cada jugador.
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
// [No visto en Android] Usamos archivos de texto línea a línea (FileWriter / BufferedReader),
// como en el vídeo "Arxius de text", dentro de la carpeta privada de la app (context.filesDir).
object Almacen {

    private const val ARCHIVO_SESION = "sesion.txt"
    private const val ARCHIVO_AJUSTES = "ajustes.txt"

    // ---------- Escribir y leer líneas ----------

    private fun escribirLineas(context: Context, nombre: String, lineas: List<String>) {
        try {
            val fw = FileWriter(File(context.filesDir, nombre))
            for (l in lineas) {
                fw.write(l + "\n")
            }
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun leerLineas(context: Context, nombre: String): List<String> {
        val lineas = mutableListOf<String>()
        val archivo = File(context.filesDir, nombre)
        if (!archivo.exists()) {
            return lineas
        }
        try {
            val br = BufferedReader(FileReader(archivo))
            var linea = br.readLine()
            while (linea != null) {
                lineas.add(linea)
                linea = br.readLine()
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return lineas
    }

    // ---------- Sesión ----------

    fun guardarSesion(context: Context) {
        escribirLineas(context, ARCHIVO_SESION,
            listOf(Juego.token, Juego.idUsuario.toString(), Juego.username, Juego.rol))
    }

    // Devuelve true si había una sesión guardada (así no hace falta volver a escribir la contraseña)
    fun cargarSesion(context: Context): Boolean {
        val lineas = leerLineas(context, ARCHIVO_SESION)
        if (lineas.size < 4 || lineas[0].isEmpty()) {
            return false
        }
        Juego.token = lineas[0]
        Juego.idUsuario = lineas[1].toInt()
        Juego.username = lineas[2]
        Juego.rol = lineas[3]
<<<<<<< HEAD
=======
        Juego.invitado = false
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        Juego.avatar = cargarAvatar(context, Juego.username)
        return true
    }

    fun borrarSesion(context: Context) {
        File(context.filesDir, ARCHIVO_SESION).delete()
        Juego.cerrarSesion()
    }

    // ---------- Ajustes ----------

<<<<<<< HEAD
    // ajustes.txt: línea 1 la temática, línea 2 la música (1 o 0), línea 3 los efectos (1 o 0)
    fun guardarAjustes(context: Context) {
        var musica = "0"
        if (Sonidos.musicaActivada) {
            musica = "1"
        }
        var efectos = "0"
        if (Sonidos.efectosActivados) {
            efectos = "1"
        }
        escribirLineas(context, ARCHIVO_AJUSTES, listOf(Juego.tema.nombre, musica, efectos))
=======
    fun guardarAjustes(context: Context) {
        escribirLineas(context, ARCHIVO_AJUSTES, listOf(Juego.tema.nombre, RetrofitClient.url))
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
    }

    fun cargarAjustes(context: Context) {
        val lineas = leerLineas(context, ARCHIVO_AJUSTES)
        if (lineas.isNotEmpty()) {
            Juego.tema = Tematicas.porNombre(lineas[0])
        }
<<<<<<< HEAD
        if (lineas.size > 2) {
            Sonidos.musicaActivada = lineas[1] != "0"
            Sonidos.efectosActivados = lineas[2] != "0"
=======
        if (lineas.size > 1 && lineas[1].isNotEmpty()) {
            RetrofitClient.url = lineas[1]
>>>>>>> 6f38390c9fce1eba1c53bb712e1a3853243a1880
        }
    }

    // ---------- Avatar (la API no lo guarda, así que va en la tablet) ----------

    fun guardarAvatar(context: Context, username: String, avatar: Int) {
        escribirLineas(context, "avatar_" + username.lowercase() + ".txt", listOf(avatar.toString()))
    }

    fun cargarAvatar(context: Context, username: String): Int {
        val lineas = leerLineas(context, "avatar_" + username.lowercase() + ".txt")
        if (lineas.isEmpty()) {
            return 0
        }
        try {
            return lineas[0].toInt()
        } catch (e: NumberFormatException) {
            return 0
        }
    }
}
