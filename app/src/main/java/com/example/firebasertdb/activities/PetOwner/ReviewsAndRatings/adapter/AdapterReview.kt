package com.example.firebasertdb.activities.PetOwner.ReviewsAndRatings.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasertdb.R
import com.example.firebasertdb.models.ReviewClass
import com.google.android.material.textfield.TextInputEditText
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso

class AdapterReview(private var dataList:MutableList<ReviewClass>, private val context: Context):RecyclerView.Adapter<AdapterReview.ViewHolderClass>() {
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    fun setData(newDataList: MutableList<ReviewClass>) {
        dataList = newDataList
    }

    fun getClickedItem(position: Int): ReviewClass?{
        if(position in 0 until dataList.size){
            return dataList[position]
        }
        else
            return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.given_review_layout,parent,false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.timeStamp.text="Review lasat de ${currentItem.userThatPostedPrenume} ${currentItem.userThatPostedNume} pe: ${currentItem.timestamp}"
        holder.titluRatingPetSitter.text="Cat de bine si-a facut treaba ${currentItem.userForReviewingPrenume} ${currentItem.userForReviewingNume}"

        holder.ratingGivenToPetSitter.rating=currentItem.ratingPetSitter.toFloat()
        holder.ratingGivenToPetServiciu.rating=currentItem.ratingServiciu.toFloat()
        holder.displayCurrentRatingPetSitter.text= "${ currentItem.ratingPetSitter }/5.0"
        holder.displayCurrentRatingServiciu.text= "${ currentItem.ratingServiciu }/5.0"
        holder.detaliiReview.setText(currentItem.comment)

        if(currentItem.userThatPostedImageURI!="Placeholder"&& currentItem.userThatPostedImageURI!="null")
            Picasso.get().load(currentItem.userThatPostedImageURI).into(holder.petOwnerImagine)
        else
            holder.petOwnerImagine.setImageResource(R.drawable.ic_profile)

        if(currentItem.userForReviewingImageURI!="Placeholder"&&currentItem.userForReviewingImageURI!="null")
            Picasso.get().load(currentItem.userForReviewingImageURI).into(holder.imaginePetsitter)
        else
            holder.imaginePetsitter.setImageResource(R.drawable.petsittericon)

        holder.detaliiReview.isFocusable=false
        holder.detaliiReview.isClickable=false
        holder.detaliiReview.isCursorVisible=false
        holder.detaliiReview.keyListener=null
    }
    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {
        val imaginePetsitter:CircularImageView = itemView.findViewById(R.id.petsitter_icon)
        val titluRatingPetSitter:TextView=itemView.findViewById(R.id.petsitter_review_name)
        val ratingGivenToPetSitter:RatingBar = itemView.findViewById(R.id.rating_given_to_petsitter)
        val ratingGivenToPetServiciu:RatingBar = itemView.findViewById(R.id.rating_given_to_service)
        val displayCurrentRatingPetSitter: TextView=itemView.findViewById(R.id.display_current_rating_given_to_petsitter)
        val displayCurrentRatingServiciu: TextView=itemView.findViewById(R.id.display_current_rating_given_to_service)
        val detaliiReview:TextInputEditText=itemView.findViewById(R.id.ti_edit_text_detalii_review)
        val timeStamp: TextView=itemView.findViewById(R.id.time_stamp_TV)
        val petOwnerImagine: CircularImageView = itemView.findViewById(R.id.petowner_image)
    }
}