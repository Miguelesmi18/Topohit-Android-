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

// Adapter del ranking: puesto, avatar, nombre y mejor puntuación.
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
        val lblPuntos: TextView

        init
        {
            layFila   = view.findViewById(R.id.LayListRankingFila)
            lblPuesto = view.findViewById(R.id.LblListRankingPuesto)
            imgAvatar = view.findViewById(R.id.ImgListRankingAvatar)
            lblNombre = view.findViewById(R.id.LblListRankingNombre)
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

        if(puesto.idUsuario == idJugador)
        {
            holder.layFila.setBackgroundResource(R.drawable.fila_resaltada)
        }
        else
        {
            holder.layFila.setBackgroundResource(R.drawable.fila)
        }
    }

    fun setPuestos(nuevos: List<PuestoRanking>)
    {
        puestos = nuevos
        notifyDataSetChanged()
    }
}
