package com.groundstudio.topohit

import com.groundstudio.topohit.datamodel.Tematica

// Las tres temáticas con los colores del GDD
object Tematicas
{
    val NORMAL = Tematica("Normal", R.drawable.fondo_partida_normal, R.drawable.logo_normal,
                          R.drawable.topo_normal_03_feliz, R.drawable.topo_normal_04_golpeado,
                          R.drawable.topo_normal_09_dorado_golpeado, R.drawable.topo_normal_10_enfadado_golpeado,
                          R.drawable.topo_normal_05_enfadado, R.drawable.topo_normal_06_enfadado_tirita,
                          R.drawable.topo_normal_07_burlon, R.drawable.topo_normal_08_dorado,
                          R.drawable.bomba, R.drawable.explosion,
                          R.drawable.btn_naranja, R.drawable.btn_verde, R.drawable.panel_crema,
                          R.drawable.circulo_crema, R.color.marron)

    val HALLOWEEN = Tematica("Halloween", R.drawable.fondo_partida_halloween, R.drawable.logo_halloween,
                             R.drawable.topo_halloween_01_feliz_bruja, R.drawable.topo_halloween_02_golpeado_bruja,
                             R.drawable.topo_halloween_09_dorado_golpeado, R.drawable.topo_halloween_10_vampiro_golpeado,
                             R.drawable.topo_halloween_03_enfadado_vampiro, R.drawable.topo_halloween_04_vampiro_tirita,
                             R.drawable.topo_halloween_05_burlon_fantasma, R.drawable.topo_halloween_06_dorado_bruja,
                             R.drawable.topo_halloween_07_bomba_calabaza, R.drawable.topo_halloween_08_explosion_calabaza,
                             R.drawable.btn_naranja_halloween, R.drawable.btn_morado, R.drawable.panel_morado_oscuro,
                             R.drawable.circulo_crema, R.color.crema_halloween)

    val NAVIDAD = Tematica("Navidad", R.drawable.fondo_partida_navidad, R.drawable.logo_navidad,
                           R.drawable.topo_navidad_01_feliz_papanoel, R.drawable.topo_navidad_02_golpeado_papanoel,
                           R.drawable.topo_navidad_09_dorado_golpeado, R.drawable.topo_navidad_10_reno_golpeado,
                           R.drawable.topo_navidad_03_enfadado_reno, R.drawable.topo_navidad_04_reno_tirita,
                           R.drawable.topo_navidad_05_burlon_bufanda, R.drawable.topo_navidad_06_dorado_navidad,
                           R.drawable.topo_navidad_07_bomba_regalo, R.drawable.topo_navidad_08_explosion_regalo,
                           R.drawable.btn_rojo, R.drawable.btn_verde_navidad, R.drawable.panel_blanco_nieve,
                           R.drawable.circulo_blanco_nieve, R.color.marron_navidad)

    // Devuelve la temática a partir de su nombre (si no la encuentra, la Normal)
    fun porNombre(nombre: String): Tematica
    {
        var tema = NORMAL
        when(nombre)
        {
            "Halloween" -> tema = HALLOWEEN
            "Navidad"   -> tema = NAVIDAD
        }
        return tema
    }
}
