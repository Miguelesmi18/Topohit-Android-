package com.groundstudio.topohit

// Los 12 avatares. La posición en la lista + 1 es el id de la tabla AVATAR de la base de datos
// (AVATARES[0] es el avatar 1 y AVATARES[11] el avatar 12): si se cambia el orden aquí, hay que
// cambiarlo también en la base de datos. A la API solo se envía el número, nunca R.drawable
// (ese número cambia cada vez que se compila).
// El avatar no depende de la temática: cualquier jugador puede elegir cualquiera.
object Avatares
{
    val AVATARES = listOf(R.drawable.topo_normal_03_feliz, R.drawable.topo_normal_05_enfadado,
                          R.drawable.topo_normal_08_dorado, R.drawable.topo_normal_07_burlon,
                          R.drawable.topo_halloween_01_feliz_bruja, R.drawable.topo_halloween_03_enfadado_vampiro,
                          R.drawable.topo_halloween_06_dorado_bruja, R.drawable.topo_halloween_05_burlon_fantasma,
                          R.drawable.topo_navidad_01_feliz_papanoel, R.drawable.topo_navidad_03_enfadado_reno,
                          R.drawable.topo_navidad_06_dorado_navidad, R.drawable.topo_navidad_05_burlon_bufanda)

    // El dibujo de un avatar a partir de su id (1 a 12). Si el id no es válido, el avatar 1
    fun dibujo(avatarId: Int): Int
    {
        var dibujo = AVATARES[0]
        if(avatarId >= 1 && avatarId <= AVATARES.size)
        {
            dibujo = AVATARES[avatarId - 1]
        }
        return dibujo
    }
}
