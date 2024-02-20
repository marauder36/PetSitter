package com.example.firebasertdb.activities.PetOwner.Pets.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasertdb.R
import com.example.firebasertdb.models.PetClass
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso

class AdapterPet(private val dataList:MutableList<PetClass>):RecyclerView.Adapter<AdapterPet.ViewHolderClass>() {
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.pet_layout,parent,false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        if(currentItem.imaginePet!="Placeholder")
            Picasso.get().load(currentItem.imaginePet).into(holder.imaginePetLayout)
        else
            holder.imaginePetLayout.setImageResource(R.drawable.animal_profile_simple)
        holder.numePetLayout.text=currentItem.numePet
        holder.rasaPetLayout.text =currentItem.rasaPet
        holder.istoricPetLayout.text=currentItem.istoricMedicalScrisPet
        holder.necesitatiPetLayout.text=currentItem.necesitatiPet
        holder.itemView.setOnClickListener{
            itemClickListener?.invoke(holder.adapterPosition)
        }
    }
    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {
        val necesitatiPetLayout:TextView =itemView.findViewById(R.id.necesitati_pet)
        val numePetLayout: TextView = itemView.findViewById(R.id.titlu_pet)
        val rasaPetLayout: TextView = itemView.findViewById(R.id.rasa_pet)
        val istoricPetLayout: TextView = itemView.findViewById(R.id.istoric_medical_pet)
        val imaginePetLayout:ImageView=itemView.findViewById(R.id.imagine_pet)
    }
}