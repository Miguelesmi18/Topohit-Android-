package com.groundstudio.topohit.util

import android.widget.ImageView
import com.groundstudio.topohit.R
import com.groundstudio.topohit.data_model.Tematicas
import com.groundstudio.topohit.databinding.SelectorAvataresBinding

// Ayuda para la rejilla de 12 avatares (layout selector_avatares.xml)
object Avatares {

    // Las 12 ImageView de la rejilla, en orden
    fun imagenes(b: SelectorAvataresBinding): List<ImageView> {
        return listOf(b.imgAvatar0, b.imgAvatar1, b.imgAvatar2, b.imgAvatar3,
            b.imgAvatar4, b.imgAvatar5, b.imgAvatar6, b.imgAvatar7,
            b.imgAvatar8, b.imgAvatar9, b.imgAvatar10, b.imgAvatar11)
    }

    // Pone los dibujos y marca con un borde amarillo el elegido.
    // CAMBIO 19/11: "elegido" es el id del avatar (1 a 12), no la posición: la imagen i es el avatar i + 1
    fun pintar(b: SelectorAvataresBinding, elegido: Int) {
        val lista = imagenes(b)
        for (i in 0..lista.size - 1) {
            lista[i].setImageResource(Tematicas.AVATARES[i])
            if (i + 1 == elegido) {
                lista[i].setBackgroundResource(R.drawable.seleccion)
            } else {
                lista[i].background = null
            }
        }
    }

    // El dibujo de un avatar a partir de su id (1 a 12). Si el id no es válido, el avatar 1
    fun dibujo(avatarId: Int): Int {
        if (avatarId < 1 || avatarId > Tematicas.AVATARES.size) {
            return Tematicas.AVATARES[0]
        }
        return Tematicas.AVATARES[avatarId - 1]
    }
}
