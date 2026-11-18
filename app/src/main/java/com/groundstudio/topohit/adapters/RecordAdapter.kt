package com.groundstudio.topohit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.R
import com.groundstudio.topohit.data_model.FilaRecord

// Adaptador de "Mis récords" (RecyclerView + ViewHolder, vídeo 19)
class RecordAdapter(private val filas: List<FilaRecord>) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgIcono: ImageView = view.findViewById(R.id.imgIcono)
        val lblNombre: TextView = view.findViewById(R.id.lblNombre)
        val lblDetalle: TextView = view.findViewById(R.id.lblDetalle)
        val lblPuntos: TextView = view.findViewById(R.id.lblPuntos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val f = filas[position]
        holder.imgIcono.setImageResource(f.icono)
        holder.lblNombre.text = f.nombre
        holder.lblDetalle.text = f.detalle
        if (f.puntos > 0) {
            holder.lblPuntos.text = "" + f.puntos + " pts"
        } else {
            holder.lblPuntos.text = "—"
        }
    }

    override fun getItemCount(): Int {
        return filas.size
    }
}
