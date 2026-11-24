package com.groundstudio.topohit

// Datos compartidos por toda la app (REGLA-0489-071: la URL de la API en un solo sitio)
class SharedData
{
    companion object
    {
        // Desde el emulador, 10.0.2.2 es el ordenador donde corre la API (perfil http, puerto 5110).
        // En una tablet de verdad se pone la IP del ordenador (misma wifi), por ejemplo
        // "http://192.168.1.20:5110/api/", y se vuelve a instalar la app.
        val PROJECT_ROOT = "http://10.0.2.2:5110/api/"

        // El juego solo tiene el modo Infinito (tabla MODO de la base de datos: 3)
        val MODO_INFINITO = 3

        // Las dificultades, con tilde, tal como están en la base de datos.
        // Se usan para las pestañas del ranking; su estado (abierta o con candado) viene siempre de la API.
        val DIFICULTADES = listOf("Fácil", "Normal", "Difícil")

        // Cuántos puestos enseña el ranking
        val TOP_RANKING = 10
    }
}
