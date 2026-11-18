package com.groundstudio.topohit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.R
import com.groundstudio.topohit.data_model.Sesion
import com.groundstudio.topohit.util.Juego

// Adaptador del historial de partidas del Perfil (RecyclerView + ViewHolder, vídeo 19)
class HistorialAdapter(private val partidas: List<Sesion>) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lblFecha: TextView = view.findViewById(R.id.lblFecha)
        val lblModo: TextView = view.findViewById(R.id.lblModo)
        val lblDificultad: TextView = view.findViewById(R.id.lblDificultad)
        val lblPuntos: TextView = view.findViewById(R.id.lblPuntos)
        val lblResultado: TextView = view.findViewById(R.id.lblResultado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val p = partidas[position]
        val modo = p.modo ?: ""
        holder.lblFecha.text = formatearFecha(p.dateTime)
        holder.lblModo.text = modo
        if (p.levelReached != null) {
            holder.lblDificultad.text = "Nivel " + p.levelReached
        } else {
            holder.lblDificultad.text = p.difficulty ?: ""
        }
        holder.lblPuntos.text = "" + p.finalScore
        holder.lblResultado.text = resultado(p, modo)
    }

    override fun getItemCount(): Int {
        return partidas.size
    }

    // "2026-10-29T17:40:12" -> "29/10 17:40"
    private fun formatearFecha(fecha: String?): String {
        if (fecha == null || fecha.length < 16) {
            return fecha ?: ""
        }
        return fecha.substring(8, 10) + "/" + fecha.substring(5, 7) + " " + fecha.substring(11, 16)
    }

    private fun resultado(p: Sesion, modo: String): String {
        if (p.abandoned) {
            return "Abandonada"
        }
        if (modo.equals(Juego.HISTORIA, true) && p.levelReached != null) {
            val estrellas = Juego.calcularEstrellas(Juego.buscarNivel(p.levelReached).objetivo, p.finalScore)
            if (estrellas == 1) {
                return "1 estrella"
            }
            return "" + estrellas + " estrellas"
        }
        if (modo.equals(Juego.CONTRARRELOJ, true) && p.difficulty != null) {
            if (p.finalScore >= Juego.buscarContrarreloj(p.difficulty).objetivo) {
                return "Victoria"
            }
            return "¡Casi!"
        }
        return "Sin vidas"
    }
}
