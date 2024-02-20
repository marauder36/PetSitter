package com.example.firebasertdb.activities.PetSitterOwner.Filter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.model.UserAttributes
import com.squareup.picasso.Picasso
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.floor

class FilterAdapter(private var dataList:MutableList<UserAttributes>):RecyclerView.Adapter<FilterAdapter.ViewHolderClass>() {
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setData(newDataList: MutableList<UserAttributes>) {
        dataList = newDataList
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    fun getClickedItem(position: Int):UserAttributes?{
        if(position in 0 until dataList.size){
            return dataList[position]
        }
        else
            return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.petsitter_details_rv_layout,parent,false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        if(currentItem.uri!="Placeholder"&&currentItem.uri!="null")
            Picasso.get().load(currentItem.uri).into(holder.imaginePetSitterLayout)
        else
            holder.imaginePetSitterLayout.setImageResource(R.drawable.petsittericon)

        holder.numePetSitterLayout.text=currentItem.prenume+" "+currentItem.nume
        holder.emailPetSitterLayout.text=currentItem.email



        if(currentItem.rating!="null"){
            holder.ratingBarPetSitter.rating = currentItem.rating.toFloat()
            holder.ratingPetSitterLayout.text =currentItem.rating
        }
        else{
            holder.ratingBarPetSitter.rating=0.0f
            holder.ratingPetSitterLayout.text ="0.0"
        }

        if (currentItem.nrEvenimente=="0"||currentItem.nrEvenimente=="null")
            holder.disponibilitatePetSitterLayout.text="No upcoming event"
        else if (currentItem.nrEvenimente=="1")
            holder.disponibilitatePetSitterLayout.text="One upcoming event"
        else
            holder.disponibilitatePetSitterLayout.text=currentItem.nrEvenimente+" upcoming events"

        val decimalFormat = DecimalFormat("#.#")
        decimalFormat.roundingMode = RoundingMode.CEILING
        var formattedValue = decimalFormat.format(currentItem.distance.toDouble()).toDouble()
        if(formattedValue<1000)
            holder.distantaPetSitterLayout.text=formattedValue.toString()+" m"
        else if(formattedValue>=1000) {
            formattedValue = ceil(formattedValue / 1000)
            holder.distantaPetSitterLayout.text=formattedValue.toString()+" km"
        }

        holder.itemView.setOnClickListener{
            itemClickListener?.invoke(holder.adapterPosition)
        }
    }
    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {
        val numePetSitterLayout: TextView = itemView.findViewById(R.id.titlu_petsitter)
        val emailPetSitterLayout: TextView = itemView.findViewById(R.id.email_petsitter)

        val distantaPetSitterLayout:TextView =itemView.findViewById(R.id.distance_petsitter)
        val ratingPetSitterLayout: TextView = itemView.findViewById(R.id.rating_petsitter)
        val ratingBarPetSitter: RatingBar = itemView.findViewById(R.id.rating_bar_petsitter)
        val disponibilitatePetSitterLayout: TextView = itemView.findViewById(R.id.disponibility_petsitter)

        val imaginePetSitterLayout:ImageView=itemView.findViewById(R.id.imagine_petsitter)
    }
}