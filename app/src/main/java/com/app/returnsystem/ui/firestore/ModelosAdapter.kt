package com.app.returnsystem.ui.firestore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.returnsystem.R


class ModelosAdapter(private val datalist: ArrayList<ModelosData>) :RecyclerView.Adapter<ModelosAdapter.MyViewHolder>(){
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val tvModelo:TextView = itemView.findViewById(R.id.item_modelo)
        val tvPart_Number:TextView = itemView.findViewById(R.id.item_part_number)
        val tvData_Hora:TextView = itemView.findViewById(R.id.item_data_hora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_modelos, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvModelo.text = datalist[position].modelo
        holder.tvPart_Number.text = datalist[position].part_number
        holder.tvData_Hora.text = datalist[position].cadastragem
    }
}
