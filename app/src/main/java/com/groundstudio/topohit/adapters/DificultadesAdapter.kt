package com.groundstudio.topohit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.groundstudio.topohit.GestorAjustes
import com.groundstudio.topohit.R
import com.groundstudio.topohit.datamodel.EstadoDificultad

// Adapter de las 3 dificultades de Infinito (Fácil, Normal y Difícil).
// Una dificultad cerrada se pinta con el candado y, debajo, por qué está cerrada:
//  - le faltan puntos en la anterior: «Consigue 500 puntos en Fácil»
//  - la ha bloqueado el tutor: su motivo o «Tu tutor ha bloqueado esta dificultad»
// El clic lo gestiona el adapter y la activity decide qué hacer (REGLA-0489-055).
class DificultadesAdapter (private val context: Context,
                           private var dificultades: List<EstadoDificultad>):
    RecyclerView.Adapter<DificultadesAdapter.DificultadesViewHolder>(),
    View.OnClickListener
{
    private val layout = R.layout.item_dificultad
    private var clickListener: View.OnClickListener? = null

    class DificultadesViewHolder(val view: View):
        RecyclerView.ViewHolder(view)
    {
        val layTarjeta:  LinearLayout
        val layVista:    LinearLayout
        val lblNombre:   TextView
        val lblInfo:     TextView
        val lblVidas:    TextView
        val lblRecord:   TextView
        val layCandado:  LinearLayout
        val lblCandadoNombre: TextView
        val lblCandado:  TextView
        val lblProgreso: TextView

        init
        {
            layTarjeta  = view.findViewById(R.id.LayListDificultadTarjeta)
            layVista    = view.findViewById(R.id.LayListDificultadVista)
            lblNombre   = view.findViewById(R.id.LblListDificultadNombre)
            lblInfo     = view.findViewById(R.id.LblListDificultadInfo)
            lblVidas    = view.findViewById(R.id.LblListDificultadVidas)
            lblRecord   = view.findViewById(R.id.LblListDificultadRecord)
            layCandado  = view.findViewById(R.id.LayListDificultadCandado)
            lblCandadoNombre = view.findViewById(R.id.LblListDificultadCandadoNombre)
            lblCandado  = view.findViewById(R.id.LblListDificultadCandado)
            lblProgreso = view.findViewById(R.id.LblListDificultadProgreso)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DificultadesViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        view.setOnClickListener(this)
        return DificultadesViewHolder(view)
    }

    override fun onBindViewHolder(holder: DificultadesViewHolder, position: Int)
    {
        val dificultad = dificultades[position]
        bindDificultad(holder, dificultad)
    }

    override fun getItemCount() = dificultades.size

    fun bindDificultad(holder: DificultadesViewHolder, dificultad: EstadoDificultad)
    {
        val tema = GestorAjustes.tema
        val color = ContextCompat.getColor(context, tema.colorTexto)
        holder.layTarjeta.setBackgroundResource(tema.panel)
        holder.lblNombre.setTextColor(color)
        holder.lblInfo.setTextColor(color)
        holder.lblVidas.setTextColor(color)

        holder.lblNombre.text = dificultad.nombre.uppercase()
        holder.lblCandadoNombre.text = dificultad.nombre.uppercase()
        holder.lblInfo.text = context.getString(R.string.dificultad_agujeros, dificultad.agujeros)

        // Una ❤ por cada vida
        var corazones = ""
        for(i in 1..dificultad.vidas)
        {
            corazones = corazones + "❤"
        }
        holder.lblVidas.text = corazones

        val record = dificultad.record
        if(record != null)
        {
            holder.lblRecord.text = context.getString(R.string.dificultad_record, record)
        }
        else
        {
            holder.lblRecord.text = context.getString(R.string.dificultad_sin_jugar)
        }

        pintarAgujeros(holder.layVista, dificultad.agujeros)

        // Cerrada: se ve el candado y no el récord (no se puede jugar todavía)
        if(dificultad.abierta)
        {
            holder.layCandado.visibility = View.GONE
            holder.lblRecord.visibility = View.VISIBLE
        }
        else
        {
            holder.layCandado.visibility = View.VISIBLE
            holder.lblRecord.visibility = View.INVISIBLE
            pintarCandado(holder, dificultad)
        }
    }

    // El texto que va debajo del candado
    private fun pintarCandado(holder: DificultadesViewHolder, dificultad: EstadoDificultad)
    {
        val motivo = dificultad.motivo
        if(dificultad.bloqueadaTutor)
        {
            if(motivo != null && motivo != "")
            {
                holder.lblCandado.text = context.getString(R.string.candado_tutor_motivo, motivo)
            }
            else
            {
                holder.lblCandado.text = context.getString(R.string.candado_tutor)
            }
            holder.lblProgreso.visibility = View.GONE
        }
        else
        {
            var anterior = dificultad.dificultadAnterior
            if(anterior == null)
            {
                anterior = ""
            }
            holder.lblCandado.text = context.getString(R.string.candado_puntos,
                                                       dificultad.puntosDesbloqueo, anterior)
            holder.lblProgreso.visibility = View.VISIBLE
            holder.lblProgreso.text = context.getString(R.string.candado_llevas, dificultad.recordAnterior)
        }
    }

    // Vista previa: filas de 3 agujeros pequeños (inflando layouts, como en las listas personalizadas)
    // Esta parte es de información externa que no sale en los vídeos
    // (addView: las vistas infladas se añaden a un LinearLayout desde el código)
    private fun pintarAgujeros(contenedor: LinearLayout, agujeros: Int)
    {
        contenedor.removeAllViews()
        val inflater = LayoutInflater.from(context)
        val filas = agujeros / 3
        for(f in 1..filas)
        {
            val fila = inflater.inflate(R.layout.fila_mini, contenedor, false) as LinearLayout
            for(c in 1..3)
            {
                fila.addView(inflater.inflate(R.layout.item_agujero_mini, fila, false))
            }
            contenedor.addView(fila)
        }
    }

    fun setDificultades(nuevas: List<EstadoDificultad>)
    {
        dificultades = nuevas
        // notifyDataSetChanged: avisa al RecyclerView de que la lista ha cambiado para que la vuelva a pintar
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: View.OnClickListener)
    {
        clickListener = listener
    }

    override fun onClick(view: View?)
    {
        clickListener?.onClick(view)
    }
}
