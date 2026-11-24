package com.groundstudio.topohit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.R
import com.groundstudio.topohit.datamodel.Partida

// Adapter del historial de partidas del Perfil (RecyclerView + ViewHolder, REGLA-0489-052)
class HistorialAdapter (private val context: Context,
                        private var partidas: List<Partida>):
    RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>()
{
    private val layout = R.layout.item_partida

    class HistorialViewHolder(val view: View):
        RecyclerView.ViewHolder(view)
    {
        val lblFecha:      TextView
        val lblDificultad: TextView
        val lblPuntos:     TextView
        val lblPrecision:  TextView
        val lblEstado:     TextView

        init
        {
            lblFecha      = view.findViewById(R.id.LblListPartidaFecha)
            lblDificultad = view.findViewById(R.id.LblListPartidaDificultad)
            lblPuntos     = view.findViewById(R.id.LblListPartidaPuntos)
            lblPrecision  = view.findViewById(R.id.LblListPartidaPrecision)
            lblEstado     = view.findViewById(R.id.LblListPartidaEstado)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int)
    {
        val partida = partidas[position]
        bindPartida(holder, partida)
    }

    override fun getItemCount() = partidas.size

    fun bindPartida(holder: HistorialViewHolder, partida: Partida)
    {
        holder.lblFecha.text = formatearFecha(partida.dateTime)

        var dificultad = partida.difficulty
        if(dificultad == null)
        {
            dificultad = ""
        }
        holder.lblDificultad.text = dificultad
        holder.lblPuntos.text = context.getString(R.string.puntos_cantidad, partida.finalScore)

        if(partida.nAttempts > 0)
        {
            holder.lblPrecision.text = context.getString(R.string.porcentaje, partida.hits * 100 / partida.nAttempts)
        }
        else
        {
            holder.lblPrecision.text = context.getString(R.string.sin_dato)
        }

        if(partida.abandoned)
        {
            holder.lblEstado.text = context.getString(R.string.partida_abandonada)
        }
        else
        {
            holder.lblEstado.text = context.getString(R.string.partida_terminada)
        }
    }

    // "2026-10-29T17:40:12" -> "29/10 17:40" (el texto que manda la API, cortado con substring)
    private fun formatearFecha(fecha: String?): String
    {
        var texto = ""
        if(fecha != null && fecha.length >= 16)
        {
            texto = fecha.substring(8, 10) + "/" + fecha.substring(5, 7) + " " + fecha.substring(11, 16)
        }
        return texto
    }

    fun setPartidas(nuevas: List<Partida>)
    {
        partidas = nuevas
        notifyDataSetChanged()
    }
}
