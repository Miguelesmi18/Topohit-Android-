package com.groundstudio.topohit.data_model

import com.groundstudio.topohit.R

// Una temática visual del juego (Normal, Halloween o Navidad).
// Cambia los dibujos y los colores, pero la forma de jugar es la misma.
class Tematica(
    val nombre: String,
    val fondo: Int,                 // fondo de pantalla
    val logo: Int,
    val topoNormal: Int,            // topo que suma 10
    val topoGolpeado: Int,          // cara al recibir el golpe
    val topoEnfadado: Int,          // necesita 2 golpes
    val topoEnfadadoTirita: Int,    // el enfadado después del primer golpe
    val topoBurlon: Int,            // sale en "Fin de partida"
    val topoDorado: Int,
    val bomba: Int,
    val explosion: Int,
    val botonPrincipal: Int,        // fondo de los botones importantes (JUGAR, ENTRAR...)
    val botonSecundario: Int,       // fondo de los otros botones
    val panel: Int,                 // fondo de los paneles
    val circulo: Int,               // botones redondos (← ? ≡)
    val colorTexto: Int,            // color del texto encima del panel
    val colorPanel: Int
)

// Las tres temáticas con los colores del GDD
object Tematicas {

    val NORMAL = Tematica(
        "Normal", R.drawable.fondo_partida_normal, R.drawable.logo_normal,
        R.drawable.topo_normal_03_feliz, R.drawable.topo_normal_04_golpeado,
        R.drawable.topo_normal_05_enfadado, R.drawable.topo_normal_06_enfadado_tirita,
        R.drawable.topo_normal_07_burlon, R.drawable.topo_normal_08_dorado,
        R.drawable.bomba, R.drawable.explosion,
        R.drawable.btn_naranja, R.drawable.btn_verde, R.drawable.panel_crema, R.drawable.circulo_crema,
        R.color.marron, R.color.crema
    )

    val HALLOWEEN = Tematica(
        "Halloween", R.drawable.fondo_partida_halloween, R.drawable.logo_halloween,
        R.drawable.topo_halloween_01_feliz_bruja, R.drawable.topo_halloween_02_golpeado_bruja,
        R.drawable.topo_halloween_03_enfadado_vampiro, R.drawable.topo_halloween_04_vampiro_tirita,
        R.drawable.topo_halloween_05_burlon_fantasma, R.drawable.topo_halloween_06_dorado_bruja,
        R.drawable.topo_halloween_07_bomba_calabaza, R.drawable.topo_halloween_08_explosion_calabaza,
        R.drawable.btn_naranja_halloween, R.drawable.btn_morado, R.drawable.panel_morado_oscuro, R.drawable.circulo_crema,
        R.color.crema_halloween, R.color.morado_oscuro
    )

    val NAVIDAD = Tematica(
        "Navidad", R.drawable.fondo_partida_navidad, R.drawable.logo_navidad,
        R.drawable.topo_navidad_01_feliz_papanoel, R.drawable.topo_navidad_02_golpeado_papanoel,
        R.drawable.topo_navidad_03_enfadado_reno, R.drawable.topo_navidad_04_reno_tirita,
        R.drawable.topo_navidad_05_burlon_bufanda, R.drawable.topo_navidad_06_dorado_navidad,
        R.drawable.topo_navidad_07_bomba_regalo, R.drawable.topo_navidad_08_explosion_regalo,
        R.drawable.btn_rojo, R.drawable.btn_verde_navidad, R.drawable.panel_blanco_nieve, R.drawable.circulo_blanco_nieve,
        R.color.marron_navidad, R.color.blanco_nieve
    )

    // Devuelve la temática a partir de su nombre (si no la encuentra, la Normal)
    fun porNombre(nombre: String): Tematica {
        return when (nombre) {
            "Halloween" -> HALLOWEEN
            "Navidad" -> NAVIDAD
            else -> NORMAL
        }
    }

    // Los 12 avatares. CAMBIO 19/11: la posición en la lista + 1 es el id de la tabla AVATAR
    // (AVATARES[0] es el avatar 1, AVATARES[11] el avatar 12). Si se cambia el orden aquí,
    // hay que cambiarlo también en la base de datos.
    // El avatar NO depende de la temática: los dibujos tienen estilo normal, Halloween o Navidad,
    // pero cualquier jugador puede elegir cualquiera.
    // A la API solo se envía el número (avatarId), nunca R.drawable: ese número cambia al compilar.
    val AVATARES = listOf(
        R.drawable.topo_normal_03_feliz, R.drawable.topo_normal_05_enfadado,
        R.drawable.topo_normal_08_dorado, R.drawable.topo_normal_07_burlon,
        R.drawable.topo_halloween_01_feliz_bruja, R.drawable.topo_halloween_03_enfadado_vampiro,
        R.drawable.topo_halloween_06_dorado_bruja, R.drawable.topo_halloween_05_burlon_fantasma,
        R.drawable.topo_navidad_01_feliz_papanoel, R.drawable.topo_navidad_03_enfadado_reno,
        R.drawable.topo_navidad_06_dorado_navidad, R.drawable.topo_navidad_05_burlon_bufanda
    )
}
