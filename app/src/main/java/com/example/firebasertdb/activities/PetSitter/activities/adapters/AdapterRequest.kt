package com.example.firebasertdb.activities.PetSitter.activities.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.model.UserAttributes
import com.example.firebasertdb.models.ServiceRequestClass

class AdapterRequest(private var dataList:MutableList<ServiceRequestClass>):RecyclerView.Adapter<AdapterRequest.ViewHolderClass>() {
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    fun setData(newDataList: MutableList<ServiceRequestClass>) {
        dataList = newDataList
    }

    fun getClickedItem(position: Int): ServiceRequestClass?{
        if(position in 0 until dataList.size){
            return dataList[position]
        }
        else
            return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.serviciu_request_layout,parent,false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]


        holder.numePetOwnerSiRequest.text="${currentItem.requestingUserName} doreste sa rezerve serviciul: ${currentItem.serviceRequested}"
        holder.dataSiOra.text ="Pe data de: ${currentItem.date} la ora ${currentItem.hour}"
        holder.numePetRequest.text="Nume Pet: ${currentItem.petName}"
        holder.rasaPetRequest.text="Rasa Pet: ${currentItem.petRace}"
        holder.statusRequest.text= currentItem.status

        if (holder.statusRequest.text.toString()=="Accepted")
            holder.statusRequest.setTextColor(Color.GREEN)

        else if (holder.statusRequest.text.toString()=="Declined"||
            holder.statusRequest.text.toString()=="Canceled by PetSitter"||
            holder.statusRequest.text.toString()=="Expired")
            holder.statusRequest.setTextColor(Color.RED)

        holder.itemView.setOnClickListener{
            itemClickListener?.invoke(holder.adapterPosition)
        }
    }
    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {
        val statusRequest:TextView=itemView.findViewById(R.id.status_rezervare)
        val numePetOwnerSiRequest: TextView = itemView.findViewById(R.id.nume_petowner_nume_request)
        val dataSiOra: TextView = itemView.findViewById(R.id.data_ora_request)
        val numePetRequest: TextView = itemView.findViewById(R.id.nume_pet_rezervare)
        val rasaPetRequest: TextView = itemView.findViewById(R.id.rasa_pet_cerere)
    }
}