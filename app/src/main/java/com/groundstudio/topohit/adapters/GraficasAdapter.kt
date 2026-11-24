package com.groundstudio.topohit.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.R
import com.groundstudio.topohit.datamodel.Grafica

// Adapter de «Mis estadísticas»: el título y la imagen de cada gráfica que hace Python
class GraficasAdapter (private val context: Context,
                       private var graficas: List<Grafica>):
    RecyclerView.Adapter<GraficasAdapter.GraficaViewHolder>()
{
    private val layout = R.layout.item_grafica

    class GraficaViewHolder(val view: View):
        RecyclerView.ViewHolder(view)
    {
        val lblTitulo: TextView
        val imgGrafica: ImageView

        init
        {
            lblTitulo  = view.findViewById(R.id.LblListGraficaTitulo)
            imgGrafica = view.findViewById(R.id.ImgListGraficaImagen)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GraficaViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return GraficaViewHolder(view)
    }

    override fun onBindViewHolder(holder: GraficaViewHolder, position: Int)
    {
        val grafica = graficas[position]
        bindGrafica(holder, grafica)
    }

    override fun getItemCount() = graficas.size

    fun bindGrafica(holder: GraficaViewHolder, grafica: Grafica)
    {
        holder.lblTitulo.text = grafica.titulo
        // Esta parte es de información externa que no sale en los vídeos
        // (Base64 y BitmapFactory: pasar la imagen en base64 que manda Python a una imagen que se pueda pintar)
        val bytes = Base64.decode(grafica.imagen, Base64.DEFAULT)
        holder.imgGrafica.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        holder.imgGrafica.contentDescription = grafica.titulo
    }

    fun setGraficas(nuevas: List<Grafica>)
    {
        graficas = nuevas
        notifyDataSetChanged()
    }
}
