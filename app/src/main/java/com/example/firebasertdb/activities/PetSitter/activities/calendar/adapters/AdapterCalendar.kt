package com.example.firebasertdb.activities.PetSitter.activities.calendar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.models.ServiceRequestClassEvent

class AdapterCalendar(private var dataList:MutableList<ServiceRequestClassEvent>):RecyclerView.Adapter<AdapterCalendar.ViewHolderClass>() {
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    fun setData(newDataList: MutableList<ServiceRequestClassEvent>) {
        dataList = newDataList
    }

    fun getClickedItem(position: Int): ServiceRequestClassEvent?{
        if(position in 0 until dataList.size){
            return dataList[position]
        }
        else
            return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.event_calendar_layout,parent,false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]


        holder.numePetOwnerSiRequest.text="${currentItem.requestingUserName} a rezervat serviciul: ${currentItem.serviceRequested}"
        holder.dataSiOra.text ="Pe data de: ${currentItem.date} la ora ${currentItem.hour}"
        holder.numePetRequest.text="Nume Pet: ${currentItem.petName}"
        holder.rasaPetRequest.text="Rasa Pet: ${currentItem.petRace}"

        if(currentItem.descriereExtra!="null"){
            holder.descriereExtra.text="Notes: "+currentItem.descriereExtra
        }
        holder.itemView.setOnClickListener{
            itemClickListener?.invoke(holder.adapterPosition)
        }
    }
    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {
        val descriereExtra:TextView=itemView.findViewById(R.id.descriere_extra)
        val numePetOwnerSiRequest: TextView = itemView.findViewById(R.id.nume_petowner_nume_request)
        val dataSiOra: TextView = itemView.findViewById(R.id.data_ora_request)
        val numePetRequest: TextView = itemView.findViewById(R.id.nume_pet_rezervare)
        val rasaPetRequest: TextView = itemView.findViewById(R.id.rasa_pet_cerere)
    }
}