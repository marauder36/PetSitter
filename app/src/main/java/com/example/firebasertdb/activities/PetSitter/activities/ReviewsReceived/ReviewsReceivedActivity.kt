package com.example.firebasertdb.activities.PetSitter.activities.ReviewsReceived

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.ReviewsAndRatings.adapter.AdapterReview
import com.example.firebasertdb.activities.PetOwner.requestsMade.adapter.AdapterRequestWithPrice
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.ReviewClass
import com.example.firebasertdb.models.ServiceRequestClassPrice
import com.example.firebasertdb.models.ServiciiClass
import com.example.firebasertdb.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ReviewsReceivedActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUser:DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var displayMadeReviewsRV:RecyclerView
    private lateinit var adapterReview: AdapterReview
    private lateinit var dataListReviews: MutableList<ReviewClass>


    override fun onStart() {
        val firstAuth= FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
            super.onStart()
        }
        else{
            startActivity(Intent(this, SelectorActivity::class.java))
            finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Handle back button click
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews_received)

        dataListReviews= mutableListOf()

        initui()
        initToolbar()
        initRTDB()
        initRecycler(dataListReviews)
        getReviews()

        adapterReview.setOnItemClickListener { position->
            val clickedItem = adapterReview.getClickedItem(position)

        }
    }

    private fun getReviews(){
        currentUser.child("Reviews").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                dataListReviews.clear()

                    for(review in snapshot.children){

                        val comment = review.child("comment").value.toString()
                        val ratingPetSitter=review.child("ratingPetSitter").value.toString()
                        val ratingServiciu=review.child("ratingServiciu").value.toString()
                        val timestamp=review.child("timestamp").value.toString()
                        val userForReviewingID=review.child("userForReviewingID").value.toString()
                        val userForReviewingImageURI=review.child("userForReviewingImageURI").value.toString()
                        val userForReviewingNume=review.child("userForReviewingNume").value.toString()
                        val userForReviewingPrenume=review.child("userForReviewingPrenume").value.toString()
                        val userThatPostedID=review.child("userThatPostedID").value.toString()
                        val userThatPostedImageURI=review.child("userThatPostedImageURI").value.toString()
                        val userThatPostedNume=review.child("userThatPostedNume").value.toString()
                        val userThatPostedPrenume=review.child("userThatPostedPrenume").value.toString()

                        dataListReviews.add(
                            ReviewClass(userThatPostedID, userThatPostedPrenume, userThatPostedNume, userThatPostedImageURI,
                                ratingServiciu, ratingPetSitter, comment, timestamp,
                                userForReviewingID, userForReviewingPrenume, userForReviewingNume, userForReviewingImageURI)
                        )
                        adapterReview.setData(dataListReviews)
                        adapterReview.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ErrorGettingData","$error")
            }
        })
    }

    private fun initui(){
        toolbar=findViewById(R.id.given_reviews_toolbar)
        displayMadeReviewsRV=findViewById(R.id.given_reviews_rv)
    }

    private fun initToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Review-uri acordate tie"
        }
    }
    private fun initRTDB(){

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("PetSitter")
        currentUser=databaseReference.child(mAuth.currentUser?.uid.toString())

    }

    private fun initRecycler(dataListReviews:MutableList<ReviewClass>){
        adapterReview = AdapterReview(dataListReviews,this)
        displayMadeReviewsRV.layoutManager = LinearLayoutManager(this)
        displayMadeReviewsRV.setHasFixedSize(false)
        displayMadeReviewsRV.adapter = adapterReview
    }
}