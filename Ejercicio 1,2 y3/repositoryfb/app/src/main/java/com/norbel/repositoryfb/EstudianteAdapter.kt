package com.norbel.repositoryfb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Callback
class EstudianteAdapter(
    private val lista: MutableList<Estudiante>,
    private val onItemClick: (Estudiante) -> Unit    // lambda de selección
) : RecyclerView.Adapter<EstudianteAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre  : TextView = view.findViewById(R.id.tvItemNombre)
        val tvCarrera : TextView = view.findViewById(R.id.tvItemCarrera)
        val tvCurso   : TextView = view.findViewById(R.id.tvItemCurso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val e = lista[position]
        holder.tvNombre.text  = e.nombre
        holder.tvCarrera.text = "Carrera: ${e.carrera}"
        holder.tvCurso.text   = "Curso: ${e.curso}"
        // Al tocar un ítem se carga en el formulario para editar/eliminar
        holder.itemView.setOnClickListener { onItemClick(e) }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nuevaLista: List<Estudiante>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
