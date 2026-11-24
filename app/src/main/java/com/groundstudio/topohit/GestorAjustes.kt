package com.groundstudio.topohit

import android.content.Context
import com.groundstudio.topohit.datamodel.Tematica
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

// Los ajustes de la tablet: la temática, la música y los efectos.
// Se guardan en ajustes.txt (carpeta files), una línea por dato, igual que la sesión.
object GestorAjustes
{
    private val ARCHIVO = "ajustes.txt"

    var tema: Tematica = Tematicas.NORMAL
    var musica = true
    var efectos = true

    private var cargado = false

    // Lee el archivo la primera vez (lo llama PantallaBase al abrir cualquier pantalla)
    fun cargar(context: Context)
    {
        if(!cargado)
        {
            cargado = true
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
                // Primera vez que se abre el juego: se quedan los valores por defecto
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

            if(lineas.size >= 3)
            {
                tema = Tematicas.porNombre(lineas[0])
                musica = lineas[1] == "1"
                efectos = lineas[2] == "1"
            }
        }
    }

    // Línea 1 la temática, línea 2 la música (1 o 0) y línea 3 los efectos (1 o 0)
    fun guardar(context: Context)
    {
        var textoMusica = "0"
        if(musica)
        {
            textoMusica = "1"
        }
        var textoEfectos = "0"
        if(efectos)
        {
            textoEfectos = "1"
        }
        var writer: FileWriter? = null
        try
        {
            writer = FileWriter(File(context.filesDir, ARCHIVO))
            writer.write(tema.nombre + "\n")
            writer.write(textoMusica + "\n")
            writer.write(textoEfectos + "\n")
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
}
