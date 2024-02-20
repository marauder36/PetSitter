package com.example.firebasertdb.activities.PetSitter.activities

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasertdb.NotificationTextActivity
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.Pets.EditPetActivity
import com.example.firebasertdb.activities.PetSitter.activities.ReviewsReceived.ReviewsReceivedActivity
import com.example.firebasertdb.activities.PetSitter.activities.adapters.AdapterRequest
import com.example.firebasertdb.activities.PetSitter.activities.calendar.CalendarActivity
import com.example.firebasertdb.activities.PetSitter.activities.galerie.GalerieActivity
import com.example.firebasertdb.activities.PetSitter.activities.requests.RequestDetailsActivity
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.activities.authPart.SplashActivity
import com.example.firebasertdb.chatpart.MainChatActivity
import com.example.firebasertdb.models.ChatMessage
import com.example.firebasertdb.models.ServiceRequestClass
import com.example.firebasertdb.utils.Constants
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mikhaellopez.circularimageview.CircularImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardPetSitterActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    private var overlayAdded = false

    private lateinit var displayName:TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var customDrawerImage:ImageView
    private lateinit var requestsListRV:RecyclerView
    private lateinit var toolbar:Toolbar

    private lateinit var invis_reviews_menu_item:View
    private lateinit var invis_calendar_menu_item:View
    private lateinit var invis_gallery_menu_item:View
    private lateinit var invis_profile_menu_item:View

    private lateinit var requestIDList:MutableList<String>

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser:DatabaseReference

    private lateinit var dataList:MutableList<ServiceRequestClass>
    private lateinit var adapter: AdapterRequest

//    private lateinit var sendPushNotification:Button

    val REQUEST_DECISION_CODE=4098

    override fun onStart() {
        val firstAuth=FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
            super.onStart()
        }
        else{
            startActivity(Intent(this@DashboardPetSitterActivity, SelectorActivity::class.java))
            finish()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_petsitter_dashboard)

        dataList= mutableListOf()
        requestIDList= mutableListOf()

        initui()
        initToolbar()
        initRTDB()
        initRecycler(dataList)


//        sendPushNotification=findViewById(R.id.send_push_notification)
//        sendPushNotification.setOnClickListener {
//            startActivity(Intent(this@DashboardPetSitterActivity,NotificationTextActivity::class.java))
//        }

        adapter.setOnItemClickListener { position ->
            val item=adapter.getClickedItem(position)
            if (item!=null){
                val toRequestDetailsFromPetsitterDashboard = Intent(this, RequestDetailsActivity::class.java)
                toRequestDetailsFromPetsitterDashboard.putExtra(Constants.petSitterRequestPet,item.petID)
                toRequestDetailsFromPetsitterDashboard.putExtra(Constants.petSitterRequestSender,item.requestingUserID)
                toRequestDetailsFromPetsitterDashboard.putExtra(Constants.petSitterRequestService,item.serviceRequested)
                toRequestDetailsFromPetsitterDashboard.putExtra(Constants.petSitterRequestID,requestIDList[position])
                startActivity(toRequestDetailsFromPetsitterDashboard)
            }

        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(!it.isSuccessful){
                Log.i("ErrorRetrievingToken","$it")
                return@addOnCompleteListener
            }
            val token = it.result
            Log.d("FCM Token", token ?: "Token is null")
            databaseReference.child(mAuth.currentUser?.uid.toString()).child("fcmToken").setValue(token)
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this@DashboardPetSitterActivity)

        val toggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {
                databaseReference.child(mAuth.currentUser?.uid.toString()).child("imagine").get()
                    .addOnSuccessListener {
                        val uriString = it.value.toString()
//                Picasso.get().load(uriString).into(profilePicture)
                        val profilePictureNav = findViewById<CircularImageView>(R.id.profileImageNavHeader)
                        if(uriString!="null")
                            Glide.with(applicationContext).load(uriString).into(profilePictureNav)
                        profilePictureNav.setOnClickListener {
                            startActivity(Intent(this@DashboardPetSitterActivity,EditProfilePetSitterActivity::class.java))
                        }
                        Log.i("firebase", "Got value ${it.value}")
                    }
                    .addOnFailureListener {
                       // Toast.makeText(this@DashboardPetSitterActivity,"Could not retrieve image from RTDB",Toast.LENGTH_LONG).show()
                    }
            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {

            }

        })
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        monitorMessages()
        updateRating()
        isProfileComplete()

        doesUserNeedTutorial()
    }

    private fun doesUserNeedTutorial(){
        currentUser.child("tutorial").get().addOnSuccessListener {
            if(it.value.toString()!="Skip" && it.value.toString()!="Completed"){
                startTutorial()
            }
        }
    }

    private fun startTutorial(){
        val firstTargetDashboard = TapTarget.forView(customDrawerImage,"Calendar activitate",
            "Da click aici pentru a vedea ce rezervari ai acceptat si care este statusul lor")
            .targetRadius(50)
            .targetCircleColor(R.color.blue_darker)

        val secondTargetDashboard =TapTarget.forToolbarNavigationIcon(toolbar,"Da click aici pentru profilul tau",
            "Poti sa descoperi si alte functionalitati ale acestei aplicatii !")
            .targetRadius(50)
            .targetCircleColor(R.color.blue_darker)

        val thirdTargetDashboard =TapTarget.forView(invis_profile_menu_item,"Da click aici daca doresti sa iti editezi profilul",
            "Aici iti poti schimba numeroase detalii ale contului tau, precum sa si adaugi servicii pe care PetOwnerii le pot descoperii !")
            .targetRadius(30)
            .targetCircleColor(R.color.less_transparent_dark_blue)
            .transparentTarget(true)
            .outerCircleColor(R.color.less_transparent_dark_purple)

        val fourthTargetDashboard =TapTarget.forView(invis_calendar_menu_item,"Da click aici cand vrei sa vezi ce activitati urmeaza !",
            "Aici iti vor aparea toate request-urile carora le-ai dat accept, carora le poti si adauga detalii in plus sau notite, sau poti chiar contacta PetOwnerii !")
            .targetRadius(30)
            .targetCircleColor(R.color.less_transparent_dark_blue)
            .transparentTarget(true)
            .outerCircleColor(R.color.less_transparent_dark_purple)

        val fifthTargetDashboard =TapTarget.forView(invis_gallery_menu_item,"Da click aici pentru a intra in propria ta galerie foto !",
            "Daca doresti, poti sa incarci imagini cu tine, cu animalutele tale de companie, sau chiar cu locul in care PetOwnerii isi vor lasa in grija ta companionii!")
            .targetRadius(30)
            .targetCircleColor(R.color.less_transparent_dark_blue)
            .transparentTarget(true)
            .outerCircleColor(R.color.less_transparent_dark_purple)

        val sixthDashboard =TapTarget.forView(invis_reviews_menu_item,"Aici iti vor fi salvate toate review-urile tale !",
            "Vei putea sa vezi ce le-a placut PetOwner-ilor si ce nu le-a placut, pentru a putea sa te imbunatatesti ca PetSitter NonStop!")
            .targetRadius(30)
            .targetCircleColor(R.color.less_transparent_dark_blue)
            .transparentTarget(true)
            .outerCircleColor(R.color.less_transparent_dark_purple)

        TapTargetSequence(this)
            .targets(firstTargetDashboard,secondTargetDashboard,thirdTargetDashboard,fourthTargetDashboard,fifthTargetDashboard,sixthDashboard)
            .listener(object : TapTargetSequence.Listener{
                override fun onSequenceFinish() {
                    currentUser.child("tutorial").setValue("Completed")
                    //Toast.makeText(this@DashboardPetSitterActivity,"Tutorial finished",Toast.LENGTH_SHORT).show()
                }

                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    if (lastTarget==secondTargetDashboard){
                        drawerLayout.openDrawer(GravityCompat.START)
                    }
                    //Toast.makeText(this@DashboardPetSitterActivity,"Tutorial next",Toast.LENGTH_SHORT).show()
                }

                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    currentUser.child("tutorial").setValue("Skip")
                    //Toast.makeText(this@DashboardPetSitterActivity,"Tutorial canceled",Toast.LENGTH_SHORT).show()
                }

            }).start()


    }

    override fun onResume() {
        super.onResume()
        //isProfileComplete()
    }

    private fun isProfileComplete(){
        currentUser.get().addOnSuccessListener {snapshot->

            val isGoogleAccount = snapshot.child("google").value.toString()
            val hasCompletedRegistration=snapshot.child("hasCompletedRegi").value.toString()
            if(isGoogleAccount=="yes" && hasCompletedRegistration!="yes"){
                showDialogForAction()
            }

        }
    }

    private fun showDialogForAction() {
        val dialog = Dialog(this@DashboardPetSitterActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.complete_user_registration_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnEdit: Button = dialog.findViewById(R.id.customDialogEditButton)

        btnEdit.setOnClickListener {
            val requestCode = 0
            var toEditIntent = Intent(this, CompleteRegistrationActivity::class.java)
            startActivityForResult(toEditIntent,requestCode)
            //Toast.makeText(this@PetsActivity, "Pressed EDIT", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        dialog.setCancelable(false)

        dialog.show()
    }

    private fun updateRating(){
        var pondMed = mutableListOf<Double>()
        var sumaFinala:Double = 0.0
        database.getReference("PetSitter").child(mAuth.currentUser?.uid.toString()).child("Reviews").get()
            .addOnSuccessListener {
                for(review in it.children){
                    val ratingPetSitter = review.child("ratingPetSitter").value.toString()
                    val ratingServiciu  = review.child("ratingServiciu").value.toString()

                    val pondSum = ((ratingPetSitter.toDouble())+(ratingServiciu.toDouble()))/2

                    pondMed.add(pondSum)
                    Log.i("PondMed","$pondMed")

                }
                for(el in pondMed){
                    if (sumaFinala==0.0)
                        sumaFinala=el
                    else
                        sumaFinala=(sumaFinala+el)/2
                }
                database.getReference("PetSitter").child(mAuth.currentUser?.uid.toString()).child("rating").setValue(sumaFinala)
                Log.i("PondMedSumaFinala","$sumaFinala")
            }
    }

    private fun monitorMessages(){
        database.getReference("Messages").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for(message in snapshot.children) {

                    val currentUserID = message.child("sender").child("id").value.toString()
                    val senderName = message.child("sender").child("name").value.toString()

                    val otherUserID = message.child("receiver").child("id").value.toString()

                    val viewed = message.child("viewed").value.toString()

                    if (otherUserID == mAuth.currentUser?.uid.toString()) {
                        if (viewed == "no") {
                            makeNotification(currentUserID, otherUserID, senderName)
                            Log.i("CalledMakeNotif","$currentUserID $otherUserID $senderName")
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ErrorGettingDataDB","$error")
            }
        })
    }

    private fun makeNotification(currentUserID:String,otherUserID:String,senderName:String){

        val channelID = "CHANNEL_ID_NOTIFICATION"
        val channelName = "CHANNEL_NAME_NOTIFICATION"

        val intent = Intent(this, MainChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(Constants.chat_with_other_user_ID,currentUserID)

        val pendingIntent = PendingIntent.getActivity(applicationContext,0,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(R.drawable.paw_print_notification)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setContentTitle("PetMe")
            .setContentText("$senderName v-a trimis un mesaj")
        //builder = builder.setContent(getRemoteView(title,message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){

            val notificationChannel = notificationManager.getNotificationChannel(channelID)
            if(notificationChannel==null){

                val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)

                notificationManager.createNotificationChannel(notificationChannel)
            }

        }
        notificationManager.notify(0,builder.build())
    }

    private fun initToolbar(){
        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        customDrawerImage = findViewById(R.id.custom_toolbar_calendar)
        customDrawerImage.setOnClickListener {
            val zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.calendar_on_click)
            customDrawerImage.startAnimation(zoomAnimation)
            startActivity(Intent(this@DashboardPetSitterActivity,CalendarActivity::class.java))
//            Toast.makeText(this, "Custom icon clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("PetSitter")
        currentUser = databaseReference.child(mAuth.currentUser?.uid.toString())
        readDataFromRTDB(currentUser)
    }

    private fun initRecycler(dataList:MutableList<ServiceRequestClass>){
        adapter = AdapterRequest(dataList)
        requestsListRV.layoutManager = LinearLayoutManager(this)
        requestsListRV.setHasFixedSize(false)
        requestsListRV.adapter = adapter
    }

    private fun readDataFromRTDB(currentUser: DatabaseReference) {
        currentUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                requestIDList.clear()

                for(request in snapshot.child("Requests").children)
                {
                    val requestingUserID = request.child("requestingUserID").value.toString()
                    val requestingUserName=request.child("requestingUserName").value.toString()
                    val serviceRequested = request.child("serviceRequested").value.toString()
                    val status = request.child("status").value.toString()
                    val date = request.child("date").value.toString()
                    val hour = request.child("hour").value.toString()
                    val petID = request.child("petID").value.toString()
                    val petName = request.child("petName").value.toString()
                    val petRace = request.child("petRace").value.toString()

                    val currRequest = ServiceRequestClass(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace)

                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

                    val dateToCheck = dateFormat.parse(date)
                    val calendar =Calendar.getInstance()
                    calendar.time=Date()
                    calendar.add(Calendar.DAY_OF_YEAR,-1)
                    var yesterdayDate = calendar.time
                    calendar.add(Calendar.DAY_OF_YEAR,-2)
                    val threeDaysAgo = calendar.time

                    val calendar2=Calendar.getInstance()
                    calendar2.time=Date()
                    calendar2.add(Calendar.HOUR_OF_DAY,8)
                    val minimumHour = calendar2.get(Calendar.HOUR_OF_DAY)
                    val oraRequest=hour.substringBefore(":").toInt()
                    Log.i("ORAREQUESTprimita","$oraRequest")
                    Log.i("ORAREQUESTcalculata","$minimumHour")
                    if(status=="Pending"&& dateToCheck.after(Date()))
                    {
                        dataList.add(currRequest)
                        requestIDList.add(request.key.toString())
                    }
//                    else if( dateToCheck.before(threeDaysAgo))
//                    {
//                        currentUser.child("Requests").child(request.key.toString()).removeValue()
//                    }
                    else if(status=="Pending"&& dateToCheck.before(Date()))
                    {
                        currentUser.child("Requests").child(request.key.toString()).child("status").setValue("Expired")
                    }


                }

                adapter.setData(dataList)
                adapter.notifyDataSetChanged()
                val currentName = snapshot.child("nume").value.toString()
                val currentPrenume =snapshot.child("prenume").value.toString()
                toolbar.title="Buna $currentPrenume $currentName !"
                if(dataList.isEmpty())
                    displayName.text="Bine ai venit la PetMe $currentName $currentPrenume ! " +
                            "Nu ai nicio cerere noua pentru rezervare"
                else
                    displayName.text="Bine ai venit la PetMe $currentName $currentPrenume ! Acestea sunt ultimele cereri de rezervare primite:"

            }
            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@DashboardPetSitterActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }

        })

    }

    private fun initui(){

        displayName =findViewById(R.id.display_google_name)
        drawerLayout = findViewById(R.id.drawer_layout_dashboard)
        requestsListRV=findViewById(R.id.recycler_view_servicii_requests)

        invis_gallery_menu_item=findViewById(R.id.invis_gallery_menu_item)
        invis_profile_menu_item=findViewById(R.id.invis_profile_menu_item)
        invis_reviews_menu_item=findViewById(R.id.invis_reviews_menu_item)
        invis_calendar_menu_item=findViewById(R.id.invis_calendar_menu_item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_logout->{

                FirebaseAuth.getInstance().signOut()
                Identity.getSignInClient(this).signOut()
                startActivity(Intent(this@DashboardPetSitterActivity, SelectorActivity::class.java))
                finish()
            }
            R.id.nav_profile->{

                startActivity(Intent(this@DashboardPetSitterActivity, EditProfilePetSitterActivity::class.java))

            }
            R.id.nav_calendar->{

                startActivity(Intent(this@DashboardPetSitterActivity,CalendarActivity::class.java))

            }
            R.id.nav_gallery->{

                startActivity(Intent(this@DashboardPetSitterActivity,GalerieActivity::class.java))

            }
            R.id.nav_your_reviews->{

                startActivity(Intent(this@DashboardPetSitterActivity,ReviewsReceivedActivity::class.java))

            }


        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==REQUEST_DECISION_CODE){
            if(resultCode== RESULT_OK){
                readDataFromRTDB(currentUser)
                //Toast.makeText(this,"Result OK",Toast.LENGTH_SHORT).show()
            }
        }
        else
            if(requestCode==0){
                if (resultCode!= RESULT_OK){
                    isProfileComplete()
                }
            }

    }
}