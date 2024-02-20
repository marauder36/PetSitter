package com.example.firebasertdb.activities.PetSitter.activities.servicii.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.model.UserAttributes
import com.example.firebasertdb.models.ServiceRequestClassPrice
import com.example.firebasertdb.models.serviciuTest

class AdapterServiciu(private var dataList:MutableList<serviciuTest>):RecyclerView.Adapter<AdapterServiciu.ViewHolderClass>() {
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    fun setData(newDataList: MutableList<serviciuTest>) {
        dataList = newDataList
    }

    fun getClickedItem(position: Int): serviciuTest?{
        if(position in 0 until dataList.size){
            return dataList[position]
        }
        else
            return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.serviciu_layout,parent,false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.numeServiciu.text=currentItem.nume
        holder.pretServiciu.text ="Pret: ${currentItem.pret} RON"
        holder.descriereServiciu.text=currentItem.descriere
        holder.itemView.setOnClickListener{
            itemClickListener?.invoke(holder.adapterPosition)
        }
    }
    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {

        val numeServiciu: TextView = itemView.findViewById(R.id.titlu_serviciu)
        val pretServiciu: TextView = itemView.findViewById(R.id.pret_serviciu)
        val descriereServiciu: TextView = itemView.findViewById(R.id.descriere_serviciu)
    }
}