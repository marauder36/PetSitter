package com.example.firebasertdb.activities.PetSitter.activities.galerie.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.squareup.picasso.Picasso

class AdapterGaleriePozePetSitter(private val dataList:MutableList<String>):RecyclerView.Adapter<AdapterGaleriePozePetSitter.ViewHolderClass>() {
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.poza_din_galerie_layout,parent,false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        Picasso.get().load(currentItem).into(holder.imagineLayout)
        holder.itemView.setOnClickListener{
            itemClickListener?.invoke(holder.adapterPosition)
        }
    }
    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {
        val imagineLayout:ImageView=itemView.findViewById(R.id.imagine_galerie)
    }
}
