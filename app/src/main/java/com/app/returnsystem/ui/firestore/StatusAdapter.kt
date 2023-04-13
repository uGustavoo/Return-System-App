package com.app.returnsystem.ui.firestore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.returnsystem.R


class StatusAdapter(private val datalist: ArrayList<StatusData>) :RecyclerView.Adapter<StatusAdapter.MyViewHolder>(){
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val tvModelo:TextView = itemView.findViewById(R.id.item_modelo)
        val tvSerial_Number:TextView = itemView.findViewById(R.id.item_serial_number)
        val tvSn:TextView = itemView.findViewById(R.id.item_sn)
        val tvKit:TextView = itemView.findViewById(R.id.item_kit)
        val tvOperacao:TextView = itemView.findViewById(R.id.item_operacao)
        val tvRecebedor:TextView = itemView.findViewById(R.id.item_recebedor)
        val tvUsuario:TextView = itemView.findViewById(R.id.item_usuario)
        val tvObs:TextView = itemView.findViewById(R.id.item_obs)
        val tvData_Hora:TextView = itemView.findViewById(R.id.item_data_hora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_status, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvModelo.text = datalist[position].modelo
        holder.tvSerial_Number.text = datalist[position].serial_number
        holder.tvSn.text = datalist[position].sn
        holder.tvKit.text = datalist[position].kit
        holder.tvOperacao.text = datalist[position].operacao
        holder.tvRecebedor.text = datalist[position].recebedor
        holder.tvUsuario.text = datalist[position].usuario
        holder.tvObs.text = datalist[position].observacoes
        holder.tvData_Hora.text = datalist[position].data
    }
}
