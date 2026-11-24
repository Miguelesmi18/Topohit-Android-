package com.groundstudio.topohit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.Avatares
import com.groundstudio.topohit.R

// Adapter de la rejilla de 12 avatares (Registro y Perfil).
// El elegido lleva el borde amarillo. El clic lo gestiona el adapter (REGLA-0489-055)
// y la activity guarda cuál es: la posición + 1 es el id del avatar.
class AvataresAdapter (private val context: Context,
                       private var elegido: Int):
    RecyclerView.Adapter<AvataresAdapter.AvataresViewHolder>(),
    View.OnClickListener
{
    private val layout = R.layout.item_avatar
    private var clickListener: View.OnClickListener? = null

    class AvataresViewHolder(val view: View):
        RecyclerView.ViewHolder(view)
    {
        val imgAvatar: ImageView

        init
        {
            imgAvatar = view.findViewById(R.id.ImgListAvatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvataresViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        view.setOnClickListener(this)
        return AvataresViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvataresViewHolder, position: Int)
    {
        bindAvatar(holder, position)
    }

    override fun getItemCount() = Avatares.AVATARES.size

    fun bindAvatar(holder: AvataresViewHolder, position: Int)
    {
        holder.imgAvatar.setImageResource(Avatares.AVATARES[position])
        if(position + 1 == elegido)
        {
            holder.imgAvatar.setBackgroundResource(R.drawable.seleccion)
            holder.imgAvatar.contentDescription = context.getString(R.string.avatar_elegido, position + 1)
        }
        else
        {
            holder.imgAvatar.background = null
            holder.imgAvatar.contentDescription = context.getString(R.string.avatar_numero, position + 1)
        }
    }

    // Marca otro avatar (id de 1 a 12)
    fun setElegido(avatarId: Int)
    {
        elegido = avatarId
        // notifyDataSetChanged: avisa al RecyclerView de que la lista ha cambiado para que la vuelva a pintar
        notifyDataSetChanged()
    }

    fun getElegido(): Int = elegido

    fun setOnClickListener(listener: View.OnClickListener)
    {
        clickListener = listener
    }

    override fun onClick(view: View?)
    {
        clickListener?.onClick(view)
    }
}
