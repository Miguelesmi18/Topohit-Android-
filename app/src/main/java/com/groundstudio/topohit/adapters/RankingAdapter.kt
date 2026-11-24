package com.groundstudio.topohit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.Avatares
import com.groundstudio.topohit.R
import com.groundstudio.topohit.datamodel.PuestoRanking

// Adapter del ranking: puesto, avatar, nombre y mejor puntuación (en el total, la suma de récords
// y debajo del nombre el récord de cada dificultad).
// La fila del jugador que ha entrado se resalta (GDD: «la fila del jugador se resalta»).
class RankingAdapter (private val context: Context,
                      private var puestos: List<PuestoRanking>,
                      private val idJugador: Int):
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>()
{
    private val layout = R.layout.item_ranking

    class RankingViewHolder(val view: View):
        RecyclerView.ViewHolder(view)
    {
        val layFila:   LinearLayout
        val lblPuesto: TextView
        val imgAvatar: ImageView
        val lblNombre: TextView
        val lblDetalle: TextView
        val lblPuntos: TextView

        init
        {
            layFila   = view.findViewById(R.id.LayListRankingFila)
            lblPuesto = view.findViewById(R.id.LblListRankingPuesto)
            imgAvatar = view.findViewById(R.id.ImgListRankingAvatar)
            lblNombre = view.findViewById(R.id.LblListRankingNombre)
            lblDetalle = view.findViewById(R.id.LblListRankingDetalle)
            lblPuntos = view.findViewById(R.id.LblListRankingPuntos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int)
    {
        val puesto = puestos[position]
        bindPuesto(holder, puesto, position + 1)
    }

    override fun getItemCount() = puestos.size

    fun bindPuesto(holder: RankingViewHolder, puesto: PuestoRanking, numero: Int)
    {
        holder.lblPuesto.text = numero.toString()
        holder.imgAvatar.setImageResource(Avatares.dibujo(puesto.avatarId))
        holder.lblNombre.text = puesto.username
        holder.lblPuntos.text = context.getString(R.string.puntos_cantidad, puesto.mejorPuntuacion)

        // En el ranking total se enseña de dónde salen los puntos (el récord de cada dificultad)
        if(puesto.recordFacil != null || puesto.recordNormal != null || puesto.recordDificil != null)
        {
            holder.lblDetalle.text = context.getString(R.string.ranking_detalle, textoRecord(puesto.recordFacil),
                                                       textoRecord(puesto.recordNormal),
                                                       textoRecord(puesto.recordDificil))
            holder.lblDetalle.visibility = View.VISIBLE
        }
        else
        {
            holder.lblDetalle.visibility = View.GONE
        }

        if(puesto.idUsuario == idJugador)
        {
            holder.layFila.setBackgroundResource(R.drawable.fila_resaltada)
        }
        else
        {
            holder.layFila.setBackgroundResource(R.drawable.fila)
        }
    }

    // El récord de una dificultad, o una raya si todavía no la ha jugado
    private fun textoRecord(record: Int?): String
    {
        var texto = context.getString(R.string.sin_record)
        if(record != null)
        {
            texto = record.toString()
        }
        return texto
    }

    fun setPuestos(nuevos: List<PuestoRanking>)
    {
        puestos = nuevos
        // notifyDataSetChanged: avisa al RecyclerView de que la lista ha cambiado para que la vuelva a pintar
        notifyDataSetChanged()
    }
}
