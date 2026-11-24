package com.groundstudio.topohit

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

// Música de fondo y efectos de sonido del juego.
//
// Esta parte es de información externa que no sale en los vídeos
// (SoundPool reproduce los sonidos cortos al momento, aunque suenen varios a la vez;
// MediaPlayer reproduce la canción de fondo en bucle; Handler espera medio segundo
// para saber si la app se ha cerrado y hay que parar la música).
// Los archivos están en res/raw y se usan con R.raw.nombre, igual que las imágenes con R.drawable.
object Sonidos
{
    // Nombres de los efectos (mismo orden que la lista de archivos de iniciar)
    const val GOLPE = 0
    const val DORADO = 1
    const val BOMBA = 2
    const val FALLO = 3
    const val COMBO = 4
    const val CUENTA = 5
    const val YA = 6
    const val VICTORIA = 7
    const val FIN = 8
    const val VIDA = 9

    private var soundPool: SoundPool? = null
    private val ids = mutableListOf<Int>()        // id de cada efecto dentro del SoundPool
    private var musica: MediaPlayer? = null

    // Cuántas pantallas del juego se ven ahora mismo (0 = la app está en segundo plano)
    private var pantallasVisibles = 0
    private val handler = Handler(Looper.getMainLooper())

    // Carga los efectos y la canción. Solo se hace la primera vez.
    fun iniciar(context: Context)
    {
        if(soundPool == null)
        {
            val atributos = AudioAttributes.Builder().
                    setUsage(AudioAttributes.USAGE_GAME).
                    setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).
                    build()
            val pool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(atributos).build()
            val archivos = listOf(R.raw.sfx_golpe, R.raw.sfx_dorado, R.raw.sfx_bomba, R.raw.sfx_fallo,
                                  R.raw.sfx_combo, R.raw.sfx_cuenta, R.raw.sfx_ya, R.raw.sfx_victoria,
                                  R.raw.sfx_fin, R.raw.sfx_vida)
            for(archivo in archivos)
            {
                ids.add(pool.load(context, archivo, 1))
            }
            soundPool = pool

            val mp = MediaPlayer.create(context, R.raw.musica_fondo)
            if(mp != null)
            {
                mp.isLooping = true
                mp.setVolume(0.4f, 0.4f)      // más baja que los efectos
                musica = mp
            }
        }
    }

    // Reproduce un efecto (GOLPE, BOMBA...)
    fun efecto(cual: Int)
    {
        val pool = soundPool
        if(GestorAjustes.efectos && pool != null && cual >= 0 && cual < ids.size)
        {
            pool.play(ids[cual], 1f, 1f, 1, 0, 1f)
        }
    }

    // ---------- Música: la llaman PantallaBase.onResume y onPause ----------

    fun pantallaVisible()
    {
        pantallasVisibles++
        sonarMusica()
    }

    // Al cambiar de pantalla, la nueva aparece justo después de que se oculte la anterior.
    // Se espera medio segundo: si no aparece ninguna, la app se ha cerrado y se para la música.
    fun pantallaOculta()
    {
        pantallasVisibles--
        if(pantallasVisibles < 0)
        {
            pantallasVisibles = 0
        }
        handler.postDelayed(
        {
            if(pantallasVisibles == 0)
            {
                pausarMusica()
            }
        }, 500)
    }

    fun sonarMusica()
    {
        val mp = musica
        if(mp != null && GestorAjustes.musica && !mp.isPlaying)
        {
            mp.start()
        }
    }

    fun pausarMusica()
    {
        val mp = musica
        if(mp != null && mp.isPlaying)
        {
            mp.pause()
        }
    }
}
