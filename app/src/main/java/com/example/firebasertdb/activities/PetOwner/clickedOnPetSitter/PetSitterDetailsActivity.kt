package com.example.firebasertdb.activities.PetOwner.clickedOnPetSitter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.firebasertdb.R
import com.mikhaellopez.circularimageview.CircularImageView
import android.widget.Button
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.Glide
import com.example.firebasertdb.activities.PetOwner.ReviewsAndRatings.adapter.AdapterReview
import com.example.firebasertdb.activities.PetSitter.activities.calendar.SelectedDayActivitiesActivity
import com.example.firebasertdb.activities.PetSitter.activities.galerie.PozaFullScreenActivity
import com.example.firebasertdb.activities.PetSitter.activities.galerie.adapters.AdapterGaleriePozePetSitter
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.chatpart.MainChatActivity
import com.example.firebasertdb.models.ReviewClass
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView


class PetSitterDetailsActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var editTextTitle:TextView
    private lateinit var profilePicture: CircularImageView
    private lateinit var currNume:TextInputEditText
    private lateinit var currPrenume:TextInputEditText
    private lateinit var currUsername:TextInputEditText
    private lateinit var currEmail:TextInputEditText
    private lateinit var currAdresa:TextInputEditText
    private lateinit var currTelefon:TextInputEditText
    private lateinit var currDescriere:TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var servicii_btn:Button
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private lateinit var dialButton:TextView
    private lateinit var chatButton:TextView

    private lateinit var calendarDisponibilitate:MaterialCalendarView
    private lateinit var requestDates:MutableList<String>

    private lateinit var recyclerViewReviews: RecyclerView
    private lateinit var adapterReview:AdapterReview
    private lateinit var dataListReviews:MutableList<ReviewClass>

    private lateinit var passedEmail:String

    private lateinit var datalist: MutableList<String>
    private lateinit var dataClass: String
    private lateinit var numeImagineDeTrimis:String
    private lateinit var galerieID:MutableList<String>
    private lateinit var adapter: AdapterGaleriePozePetSitter
    private lateinit var currentUserGalerie:DatabaseReference

    override fun onStart() {
        val firstAuth=FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
            super.onStart()
        }
        else{
            startActivity(Intent(this@PetSitterDetailsActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_details_petsitter)

        passedEmail = intent.getStringExtra(Constants.email_selected_marker)!!

        galerieID= mutableListOf()
        datalist = mutableListOf()
        dataListReviews= mutableListOf()
        requestDates= mutableListOf()

        initUI()
        initToolbar()
        initRTDB()
        initRecycler()
        initRecyclerReviews(dataListReviews)
        getReviews()
        servicii_btn.setOnClickListener {
            val toServiciiDetailsActivity = Intent(this@PetSitterDetailsActivity, ServiciiDetailsActivity::class.java)
            toServiciiDetailsActivity.putExtra(Constants.email_selected_marker,passedEmail)
            startActivity(toServiciiDetailsActivity)
        }

        dialButton.setOnClickListener {
            database.getReference("PetSitter").get()
                .addOnSuccessListener {snapshot->
                    for(user in snapshot.children){
                        val phoneNumber=user.child("phoneNumber").value.toString()
                        if (user.child("email").value.toString()==passedEmail) {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:${phoneNumber}")
                            startActivity(intent)
                        }
                    }
                }
        }

        chatButton.setOnClickListener {
            database.getReference("PetSitter").get()
                .addOnSuccessListener {snapshot->
                    for(user in snapshot.children){
                        val otherUserID=user.key.toString()
                        if (user.child("email").value.toString()==passedEmail) {
                            val intent=Intent(this, MainChatActivity::class.java)
                            intent.putExtra(Constants.chat_with_other_user_ID,otherUserID)
                            startActivity(intent)
                        }
                    }
                }

        }
        calendarDisponibilitate.setOnDateChangedListener { widget, date, selected ->


//            val dayOfMonth=  date.day
//            val month =      date.month
//            val year  =      date.year
//
//            selectedDate="$dayOfMonth-${month}-$year"
//            val selectedDayIntent=Intent(this@CalendarActivity, SelectedDayActivitiesActivity::class.java)
//            selectedDayIntent.putExtra(Constants.ZI_ALEASA,selectedDate)
//            startActivity(selectedDayIntent)
            calendarDisponibilitate.clearSelection()
        }

    }

    private fun getEvents(){
        databaseReference.child(mAuth.currentUser?.uid.toString()).child("Requests").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for(request in snapshot.children)
                {
                    val eventDate = request.child("date").value.toString()
                    val eventStatus=request.child("status").value.toString()
                    if(eventStatus=="Accepted")
                        requestDates.add(eventDate)

                }
                Log.i("DatesList","$requestDates")
                displayEventsOnCalendar(requestDates)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ErrorGettingDataFromDB","$error")
            }

        })
    }

    private fun displayEventsOnCalendar(requestDates:MutableList<String>){
        calendarDisponibilitate.clearSelection()

        val datesWithEvents = mutableListOf<CalendarDay>()

        for(eventDate in requestDates){
            val parts = eventDate.split("-")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            Log.i("DatesParts","$day-$month-$year")

            val calendarDay = CalendarDay.from(year,month, day)

            Log.i("DatesCalendarDays","$calendarDay")

            datesWithEvents.add(calendarDay)
        }

        calendarDisponibilitate.addDecorator(EventDecorator(this,datesWithEvents))
    }

    class EventDecorator(private val context: Context, private val datesWithEvents:MutableList<CalendarDay>):
        DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return datesWithEvents.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setSelectionDrawable(context.resources.getDrawable(R.drawable.event_indicator_calendar))
        }

    }

    private fun initRecyclerReviews(dataList:MutableList<ReviewClass>){
        adapterReview = AdapterReview(dataList,this)
        recyclerViewReviews.layoutManager = LinearLayoutManager(this)
        recyclerViewReviews.setHasFixedSize(false)
        recyclerViewReviews.adapter = adapterReview
    }

    private fun getReviews(){
        databaseReference.addValueEventListener(object :ValueEventListener{


            override fun onDataChange(snapshot: DataSnapshot) {
                dataListReviews.clear()
                for(user in snapshot.children){
                    if(user.child("email").value.toString()==passedEmail){
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

                            dataListReviews.add(
                                ReviewClass(userThatPostedID, userThatPostedPrenume, userThatPostedNume, userThatPostedImageURI,
                                    ratingServiciu, ratingPetSitter, comment, timestamp,
                                    userForReviewingID, userForReviewingPrenume, userForReviewingNume, userForReviewingImageURI)
                            )
                            adapterReview.setData(dataListReviews)
                            adapterReview.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ErrorGettingData","$error")
            }
        })
    }

    private fun initUI() {
        calendarDisponibilitate=findViewById(R.id.calendar_disponibilitate)

        recyclerViewReviews=findViewById(R.id.recycler_view_reviews)
        recyclerView=findViewById(R.id.recycler_view_details_galerie)
        servicii_btn=findViewById(R.id.servicii_button)
        editTextTitle = findViewById(R.id.edit_text_title)

        dialButton=findViewById(R.id.dial_button)
        chatButton=findViewById(R.id.send_message_TV)

        profilePicture = findViewById(R.id.editProfileImage)
        profilePicture.isFocusable=false
        profilePicture.isClickable=false

        currNume = findViewById(R.id.ti_edit_text_nume)
        currNume.isFocusable=false
        currNume.isClickable=false
        currNume.isCursorVisible=false
        currNume.keyListener=null

        currPrenume = findViewById(R.id.ti_edit_text_prenume)
        currPrenume.isFocusable=false
        currPrenume.isClickable=false
        currPrenume.isCursorVisible=false
        currPrenume.keyListener=null

        currUsername=findViewById(R.id.ti_edit_text_username)
        currUsername.isFocusable=false
        currUsername.isClickable=false
        currUsername.isCursorVisible=false
        currUsername.keyListener=null

        currEmail = findViewById(R.id.ti_edit_text_email)
        currEmail.isFocusable=false
        currEmail.isClickable=false
        currEmail.isCursorVisible=false
        currEmail.keyListener=null

        currAdresa=findViewById(R.id.ti_edit_text_address)
        currAdresa.isFocusable=false
        currAdresa.isClickable=false
        currAdresa.isCursorVisible=false
        currAdresa.keyListener=null

        currTelefon=findViewById(R.id.ti_edit_text_phone)
        currTelefon.isFocusable=false
        currTelefon.isClickable=false
        currTelefon.isCursorVisible=false
        currTelefon.keyListener=null

        currDescriere=findViewById(R.id.ti_edit_text_descriere)
        currDescriere.isFocusable=false
        currDescriere.isClickable=false
        currDescriere.isCursorVisible=false
        currDescriere.keyListener=null

    }

    private fun initToolbar(){
        toolbar = findViewById(R.id.petsitter_profile_details_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("PetSitter")
        currentUserGalerie=databaseReference.child("User(unique email)").child("Galerie")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for(user in snapshot.children){
                    val currentEmail = user.child("email").value.toString()
                    val currentUserUID=user.key.toString()
                    if(currentEmail==passedEmail){
                        updateUserProfile(user)
                        Log.i("CurrentUserUID","$currentUserUID")
                        currentUserGalerie=databaseReference.child("$currentUserUID").child("Galerie")
                        Log.i("GalerieDupaFetch","$currentUserGalerie")
                        datalist.clear()
                        galerieID.clear()
                        var index = 0
                        currentUserGalerie.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.i("GalerieImediat","$snapshot")
                                Log.i("fetchFromFirebase", "Called")
                                Log.i("fetchFromFirebase", "$snapshot")
                                for(poza in snapshot.children) {
                                    Log.i("fetchFromFirebase", "Called")
                                    numeImagineDeTrimis=poza.key.toString()
                                    galerieID.add(numeImagineDeTrimis)
                                    Log.i("TitluID", "Got: $numeImagineDeTrimis")

                                    val uriImagineCurr = poza.child("uriImagine").value.toString()

                                    Log.i(
                                        "ValoriRedate",
                                        "Got: ${poza.child("uriImagine").value.toString()}| $uriImagineCurr")


                                    dataClass = uriImagineCurr
                                    datalist.add(dataClass)
                                    Log.i(
                                        "DataList",
                                        "Got: ${datalist}")
                                    adapter.notifyItemInserted(index)
                                    index++
                                }
                                adapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.i("GalerieDupaFetch","$error")
                            }

                        })

                        for(request in user.child("Requests").children)
                        {
                            val eventDate = request.child("date").value.toString()
                            val eventStatus=request.child("status").value.toString()

                            if(eventStatus=="Accepted")
                                requestDates.add(eventDate)

                        }
                        Log.i("DatesList","$requestDates")
                        displayEventsOnCalendar(requestDates)
                        //break
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@PetSitterDetailsActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
    }

    private fun initRecycler() {
        adapter = AdapterGaleriePozePetSitter(datalist)
        adapter.setOnItemClickListener { position ->
            var toEditIntent = Intent(this, PozaFullScreenActivity::class.java)
            toEditIntent.putExtra(Constants.poza_aleasa,datalist[position])
            startActivity(toEditIntent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter

    }

    private fun updateUserProfile(snapshot: DataSnapshot) {

        val numeRTDB = snapshot.child("nume").value.toString()
        val prenumeRTDB = snapshot.child("prenume").value.toString()
        val usernameRTDB = snapshot.child("username").value.toString()
        val emailRTDB = snapshot.child("email").value.toString()
        val adresaRTDB = snapshot.child("address").value.toString()
        val telefonRTDB = snapshot.child("phoneNumber").value.toString()
        val descriereRTDB=snapshot.child("descriere").value.toString()
        val imagineRTDB=snapshot.child("imagine").value.toString()
        if(imagineRTDB!="null")
            Glide.with(applicationContext).load(imagineRTDB).into(profilePicture)
        Log.i("Data Accessed","Again")

        if(snapshot.child("nume").value!=null){
            editTextTitle.text=numeRTDB
            currNume.setText(numeRTDB)
            supportActionBar?.apply {
                title = "Detalii $prenumeRTDB $numeRTDB"
            }
        }


        if (snapshot.child("prenume").value!=null)
            currPrenume.setText(prenumeRTDB)

        if (snapshot.child("username").value!=null)
            currUsername.setText(usernameRTDB)

        if (snapshot.child("email").value!=null)
            currEmail.setText(emailRTDB)

        if (snapshot.child("address").value!=null)
            currAdresa.setText(adresaRTDB)

        if (snapshot.child("phoneNumber").value!=null)
            currTelefon.setText(telefonRTDB)

        if (snapshot.child("descriere").value!=null)
            currDescriere.setText(descriereRTDB)

        Log.i("Data Accessed","Again")

    }
}