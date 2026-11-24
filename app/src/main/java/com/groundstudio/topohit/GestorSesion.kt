package com.groundstudio.topohit

import android.content.Context
import com.groundstudio.topohit.datamodel.SesionUsuario
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

// Guarda quién ha entrado (token, id, usuario, rol y avatar) en la carpeta files de la app,
// para no tener que escribir la contraseña cada vez que se abre el juego.
// Un dato por línea con FileWriter, y se lee con BufferedReader + readLine() hasta null
// (REGLA-0490-008 y 011, vídeo «Arxius de text»).
object GestorSesion
{
    private val ARCHIVO = "sesion.txt"

    // Sesión en memoria mientras la app está abierta
    private var sesion: SesionUsuario? = null

    fun guardar(context: Context, nueva: SesionUsuario)
    {
        sesion = nueva
        var writer: FileWriter? = null
        try
        {
            writer = FileWriter(File(context.filesDir, ARCHIVO))
            writer.write(nueva.token + "\n")
            writer.write(nueva.idUsuario.toString() + "\n")
            writer.write(nueva.username + "\n")
            writer.write(nueva.rol + "\n")
            writer.write(nueva.avatarId.toString() + "\n")
        }
        catch(e: IOException)
        {
            println(e.message)
        }
        finally
        {
            // Se cierra pase lo que pase (también si ha fallado la escritura)
            if(writer != null)
            {
                try
                {
                    writer.close()
                }
                catch(e: IOException)
                {
                    println(e.message)
                }
            }
        }
    }

    // Devuelve la sesión de memoria o, si la app se ha cerrado, la lee del archivo (null = no hay)
    fun getSesion(context: Context): SesionUsuario?
    {
        if(sesion == null)
        {
            sesion = leer(context)
        }
        return sesion
    }

    // Cerrar sesión: se borra el archivo
    fun borrar(context: Context)
    {
        sesion = null
        val archivo = File(context.filesDir, ARCHIVO)
        if(archivo.exists())
        {
            archivo.delete()
        }
    }

    // Valor de la cabecera Authorization que pide la API: "Bearer <token>"
    fun cabecera(context: Context): String
    {
        var token = ""
        val actual = getSesion(context)
        if(actual != null)
        {
            token = actual.token
        }
        return "Bearer " + token
    }

    fun getUsername(context: Context): String
    {
        var nombre = ""
        val actual = getSesion(context)
        if(actual != null)
        {
            nombre = actual.username
        }
        return nombre
    }

    fun getIdUsuario(context: Context): Int
    {
        var id = 0
        val actual = getSesion(context)
        if(actual != null)
        {
            id = actual.idUsuario
        }
        return id
    }

    fun getAvatar(context: Context): Int
    {
        var avatar = 1
        val actual = getSesion(context)
        // Solo vale un avatar de 1 a 12; cualquier otro (por ejemplo un 0 de una API antigua) es el 1
        if(actual != null && actual.avatarId >= 1 && actual.avatarId <= Avatares.AVATARES.size)
        {
            avatar = actual.avatarId
        }
        return avatar
    }

    // Al cambiar el avatar en el Perfil se vuelve a guardar el archivo
    fun cambiarAvatar(context: Context, avatarId: Int)
    {
        val actual = getSesion(context)
        if(actual != null)
        {
            actual.avatarId = avatarId
            guardar(context, actual)
        }
    }

    private fun leer(context: Context): SesionUsuario?
    {
        var leida: SesionUsuario? = null
        val lineas = mutableListOf<String>()
        var reader: BufferedReader? = null
        try
        {
            val lector = BufferedReader(FileReader(File(context.filesDir, ARCHIVO)))
            reader = lector
            var linea = lector.readLine()
            while(linea != null)
            {
                lineas.add(linea)
                linea = lector.readLine()
            }
        }
        catch(e: FileNotFoundException)
        {
            // Todavía no ha entrado nadie en esta tablet: no hay sesión
        }
        catch(e: IOException)
        {
            println(e.message)
        }
        finally
        {
            if(reader != null)
            {
                try
                {
                    reader.close()
                }
                catch(e: IOException)
                {
                    println(e.message)
                }
            }
        }

        if(lineas.size >= 5 && lineas[0] != "")
        {
            try
            {
                leida = SesionUsuario(lineas[0], lineas[1].toInt(), lineas[2], lineas[3], lineas[4].toInt())
            }
            catch(e: NumberFormatException)
            {
                // Archivo estropeado: como si no hubiera sesión (se vuelve a entrar)
                leida = null
            }
        }
        return leida
    }
}
