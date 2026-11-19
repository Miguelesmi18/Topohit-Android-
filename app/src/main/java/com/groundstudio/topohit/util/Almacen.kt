package com.groundstudio.topohit.util

import android.content.Context
import com.groundstudio.topohit.data_model.Tematicas
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

// Guarda en la tablet lo que hay que recordar al cerrar la app:
// la sesión (token y avatar) y los ajustes (temática y sonido).
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

    // sesion.txt: token, id, usuario, rol y (CAMBIO 19/11) el id del avatar.
    // El avatar de verdad está en la API; aquí solo se apunta para pintarlo al abrir la app sin pedirlo.
    fun guardarSesion(context: Context) {
        escribirLineas(context, ARCHIVO_SESION,
            listOf(Juego.token, Juego.idUsuario.toString(), Juego.username, Juego.rol, Juego.avatar.toString()))
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
        Juego.avatar = 1
        if (lineas.size > 4) {
            try {
                Juego.avatar = lineas[4].toInt()
            } catch (e: NumberFormatException) {
                Juego.avatar = 1
            }
        }
        return true
    }

    fun borrarSesion(context: Context) {
        File(context.filesDir, ARCHIVO_SESION).delete()
        Juego.cerrarSesion()
    }

    // ---------- Ajustes ----------

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
    }

    fun cargarAjustes(context: Context) {
        val lineas = leerLineas(context, ARCHIVO_AJUSTES)
        if (lineas.isNotEmpty()) {
            Juego.tema = Tematicas.porNombre(lineas[0])
        }
        if (lineas.size > 2) {
            Sonidos.musicaActivada = lineas[1] != "0"
            Sonidos.efectosActivados = lineas[2] != "0"
        }
    }
}
