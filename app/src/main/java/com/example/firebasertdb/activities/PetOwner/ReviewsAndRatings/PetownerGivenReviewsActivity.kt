package com.example.firebasertdb.activities.PetOwner.ReviewsAndRatings

import android.app.AlertDialog
import android.content.Intent
import android.media.Rating
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.ReviewsAndRatings.adapter.AdapterReview
import com.example.firebasertdb.activities.PetOwner.requestsMade.adapter.AdapterRequestWithPrice
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.ReviewClass
import com.example.firebasertdb.models.ServiceRequestClassPrice
import com.example.firebasertdb.models.ServiciiClass
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikhaellopez.circularimageview.CircularImageView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class PetownerGivenReviewsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var database: FirebaseDatabase
    private lateinit var petSitterReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private var alertDialogUniquePetSitter: AlertDialog? = null
    
    private lateinit var displayMadeReviewsRV:RecyclerView
    private lateinit var adapterReview: AdapterReview
    private lateinit var dataList: MutableList<ReviewClass>
    private lateinit var requestsID:MutableList<String>
    private lateinit var uniqueUsersID:MutableList<String>
    private lateinit var requestsDataList: MutableList<ServiceRequestClassPrice>

    private lateinit var currentUser:DatabaseReference
    private lateinit var currentUserImage  :String
    private lateinit var currentUserPrenume:String
    private lateinit var currentUserNume   :String
    private lateinit var currentUserID     :String

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
        setContentView(R.layout.activity_petowner_given_reviews)

        dataList= mutableListOf()
        requestsDataList= mutableListOf()
        requestsID= mutableListOf()
        uniqueUsersID= mutableListOf()

        initui()
        initToolbar()
        initRTDB()
        initRecycler(dataList)
        getCompletedRequests()
        getMadeReviews()

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
            title = "Reviews you gave"
        }
    }

    private fun initRTDB(){
        
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        petSitterReference = database.getReference("PetSitter")
        currentUser=database.getReference("Petowner").child(mAuth.currentUser?.uid.toString())
        getCurrentUserDetails()
    }

    private fun getCurrentUserDetails(){
        currentUser.get().addOnSuccessListener {userDetails->
            currentUserImage = userDetails.child("imagine").value.toString()
            currentUserPrenume=userDetails.child("prenume").value.toString()
            currentUserNume   =userDetails.child("nume").value.toString()
            currentUserID     =mAuth.currentUser?.uid.toString()
        }
    }

    private fun initRecycler(dataList:MutableList<ReviewClass>){
        adapterReview = AdapterReview(dataList,this)
        displayMadeReviewsRV.layoutManager = LinearLayoutManager(this)
        displayMadeReviewsRV.setHasFixedSize(false)
        displayMadeReviewsRV.adapter = adapterReview
    }

    private fun getMadeReviews(){
        petSitterReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                dataList.clear()

                for(user in snapshot.children){
                    for(review in user.child("Reviews").children){

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

                        if (userThatPostedID==mAuth.currentUser?.uid.toString())
                        {
                            dataList.add(ReviewClass(userThatPostedID, userThatPostedPrenume, userThatPostedNume, userThatPostedImageURI,
                                ratingServiciu, ratingPetSitter, comment, timestamp,
                                userForReviewingID, userForReviewingPrenume, userForReviewingNume, userForReviewingImageURI))
                            adapterReview.setData(dataList)
                            adapterReview.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("Error","$error")
            }

        })
    }

    private fun getCompletedRequests(){
        
        petSitterReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                requestsDataList.clear()
                requestsID.clear()
                uniqueUsersID.clear()
                for (user in snapshot.children){
                    for (request in user.child("Requests").children){
                        val petSitterID = user.key.toString()
                        val requestingUserID = request.child("requestingUserID").value.toString()
                        val requestingUserName=request.child("requestingUserName").value.toString()
                        val serviceRequested = request.child("serviceRequested").value.toString()
                        val status = request.child("status").value.toString()
                        val date = request.child("date").value.toString()
                        val hour = request.child("hour").value.toString()
                        val petID = request.child("petID").value.toString()
                        val petName = request.child("petName").value.toString()
                        val petRace = request.child("petRace").value.toString()
                        val pretServiciuCerut=request.child("pretServiciuCerut").value.toString()
                        val reviewFlag=request.child("reviewed").value.toString()

                        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val dateToCheck = dateFormat.parse(date)
                        val calendar = Calendar.getInstance()
                        calendar.time=Date()

                        val currDay = calendar.get(Calendar.DAY_OF_MONTH)
                        val currMonth = calendar.get(Calendar.MONTH)+1
                        val currYear = calendar.get(Calendar.YEAR)

                        val todayDate = dateFormat.parse("$currDay-$currMonth-$currYear")

                        val currHour = calendar.get(Calendar.HOUR_OF_DAY)
                        val hourToCheck=hour.substringBefore(":").toInt()

                        if (status=="Payed" && dateToCheck.equals(todayDate) && hourToCheck<currHour)
                            petSitterReference.child(user.key.toString()).child("Requests")
                                .child(request.key.toString()).child("status").setValue("Completed")

                        if(status=="Completed"&&reviewFlag=="null"){
                            requestsDataList.add(ServiceRequestClassPrice(requestingUserID,requestingUserName,serviceRequested,
                                status,date,hour,petID,petName,petRace,pretServiciuCerut))

                            if (!uniqueUsersID.contains(petSitterID))
                                uniqueUsersID.add(petSitterID)

                            requestsID.add(request.key.toString())

                            Log.i("GoodDates","$dateToCheck vs $todayDate")}
                    }
                }
                
                Log.i("UniqueUsersIDs","Got: $uniqueUsersID")
                displayReviewDialog(uniqueUsersID)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ErrorRetrievingData","$error")
            }

        })
        
    }
    
    private fun displayReviewDialog(uniqueUserIDs:MutableList<String>){
        for(user in uniqueUserIDs){
            petSitterReference.child(user).get().addOnSuccessListener {currUser->
                val currUserID = currUser.key.toString()
                val currUserName = currUser.child("nume").value.toString()
                val currUserPrenume = currUser.child("prenume").value.toString()
                val currUserImagineURI=currUser.child("imagine").value.toString()

                if (uniqueUserIDs.isNotEmpty() && alertDialogUniquePetSitter==null){

                    val dialogView: View = LayoutInflater.from(this@PetownerGivenReviewsActivity)
                        .inflate(R.layout.review_layout,null)


                    val petsitter_icon = dialogView.findViewById<CircularImageView>(R.id.petsitter_icon)
                    Glide.with(this).load(currUserImagineURI).into(petsitter_icon)

                    val petsitter_review_name = dialogView.findViewById<TextView>(R.id.petsitter_review_name)
                    petsitter_review_name.text="Cat de bine si-a facut treaba $currUserPrenume $currUserName ?"

                    val rating_given_to_petsitter = dialogView.findViewById<RatingBar>(R.id.rating_given_to_petsitter)
                    val display_current_rating_given_to_petsitter = dialogView.findViewById<TextView>(R.id.display_current_rating_given_to_petsitter)
                    rating_given_to_petsitter.setOnRatingBarChangeListener { _, rating, _ ->
                        display_current_rating_given_to_petsitter.text="${rating}/5"}


                    val rating_given_to_service = dialogView.findViewById<RatingBar>(R.id.rating_given_to_service)
                    val display_current_rating_given_to_service = dialogView.findViewById<TextView>(R.id.display_current_rating_given_to_service)
                    rating_given_to_service.setOnRatingBarChangeListener { _, rating, _ ->
                        display_current_rating_given_to_service.text="${rating}/5"
                    }


                    val ti_edit_text_detalii_review = dialogView.findViewById<TextInputEditText>(R.id.ti_edit_text_detalii_review)

                    val cancel_press_on = dialogView.findViewById<ImageView>(R.id.cancel_press_on)
                    val save_review_button = dialogView.findViewById<AppCompatButton>(R.id.save_review)

                    val calendar = Calendar.getInstance()
                    calendar.time= Date()

                    val currZI = calendar.get(Calendar.DAY_OF_MONTH)
                    val currLuna=calendar.get(Calendar.MONTH)+1
                    val currAn  =calendar.get(Calendar.YEAR)
                    val currOra =calendar.get(Calendar.HOUR_OF_DAY)
                    val currMinut=calendar.get(Calendar.MINUTE)

                    val timeStamp = "$currZI-$currLuna-$currAn la ora $currOra:$currMinut"

                    cancel_press_on.setOnClickListener{
                        alertDialogUniquePetSitter?.dismiss()
                    }
                    save_review_button.setOnClickListener {
                        val currRatingServiciu=rating_given_to_service.rating.toString()
                        val currRatingPetSitter=rating_given_to_petsitter.rating.toString()
                        val currentComment=ti_edit_text_detalii_review.text.toString()
                        

                        val currentReview = ReviewClass(currentUserID,currentUserPrenume,currentUserNume,currentUserImage,currRatingServiciu,currRatingPetSitter,
                            currentComment,timeStamp,currUserID,currUserPrenume,currUserName,currUserImagineURI)
                        
                        saveReview(currentReview,currUserID)

                    }

                    val builder = AlertDialog.Builder(this)
                    builder.setView(dialogView)
                    alertDialogUniquePetSitter = builder.create()
                    alertDialogUniquePetSitter?.setCanceledOnTouchOutside(false)
                    alertDialogUniquePetSitter?.show()
                }
                
            }
        }
    }
    private fun saveReview(currentReview:ReviewClass,currPetSitterID:String){

        petSitterReference.child(currPetSitterID).child("Reviews").child("Review de la ${currentReview.userThatPostedPrenume}" +
                " ${currentReview.userThatPostedNume} pentru ${currentReview.userForReviewingPrenume} ${currentReview.userForReviewingNume} pe data de " +
                "${currentReview.timestamp}").setValue(currentReview)
            .addOnSuccessListener {
                setReviewedFlagDa(currPetSitterID)
                alertDialogUniquePetSitter?.dismiss()
                uniqueUsersID.remove(currPetSitterID)
                displayReviewDialog(uniqueUsersID)
            }

    }
    
    private fun setReviewedFlagDa(petSitterID:String){
        
        petSitterReference.child(petSitterID).child("Requests").get()
            .addOnSuccessListener { snapshot->

                for(request in snapshot.children){
                    if (request.child("requestingUserID").value.toString()==mAuth.currentUser?.uid.toString() &&
                        request.child("status").value.toString()=="Completed")
                    {
                        petSitterReference.child(petSitterID).child("Requests").child(request.key.toString()).child("reviewed").setValue("Yes")
                    }
                }

            }
        
    }
    

}