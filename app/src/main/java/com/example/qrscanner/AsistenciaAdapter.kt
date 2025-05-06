package com.example.qrscanner

import Asistencia
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AsistenciaAdapter(private val lista: List<Asistencia>) :
    RecyclerView.Adapter<AsistenciaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtApellido: TextView = view.findViewById(R.id.txtApellido)
        val txtClave: TextView = view.findViewById(R.id.txtClave)
        val txtEntrada: TextView = view.findViewById(R.id.txtEntrada)
        val txtSalida: TextView = view.findViewById(R.id.txtSalida)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asistencia, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNombre.text = item.nombre
        holder.txtApellido.text = item.apellido
        holder.txtClave.text = item.clave
        holder.txtEntrada.text = item.horaEntrada ?: "Sin entrada"
        holder.txtSalida.text = item.horaSalida ?: "Sin salida"
    }

    override fun getItemCount(): Int = lista.size
}
