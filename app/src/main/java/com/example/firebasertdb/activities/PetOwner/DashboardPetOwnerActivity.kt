package com.example.firebasertdb.activities.PetOwner

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.Filter.FilterActivity
import com.example.firebasertdb.activities.PetOwner.Pets.PetsActivity
import com.example.firebasertdb.activities.PetOwner.ReviewsAndRatings.PetownerGivenReviewsActivity
import com.example.firebasertdb.activities.PetOwner.clickedOnPetSitter.PetSitterDetailsActivity
import com.example.firebasertdb.activities.PetOwner.requestsMade.MadeRequestsActivity
import com.example.firebasertdb.activities.PetSitter.activities.CompleteRegistrationActivity
import com.example.firebasertdb.activities.PetSitterOwner.Filter.adapter.FilterAdapter
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.chatpart.MainChatActivity
import com.example.firebasertdb.model.UserAttributes
import com.example.firebasertdb.utils.Constants
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DashboardPetOwnerActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener ,
    OnMapReadyCallback,GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter {

    private lateinit var quickPetSitterRV:RecyclerView
    private lateinit var adapter: FilterAdapter
    private lateinit var dataClass: UserAttributes
    private lateinit var allUsersDatalist: MutableList<UserAttributes>
    private lateinit var userAttributesList: MutableList<UserAttributes>
    private lateinit var currCoords:LatLng

    private lateinit var displayName:TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var customDrawerImage:ImageView

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var PetSitterReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var prefType:String
    private lateinit var currentUser: DatabaseReference
    private lateinit var toolbar: Toolbar
//    private lateinit var toMapsButton:Button

    private lateinit var uriList: MutableList<String>
    private lateinit var petSitterNames:MutableList<String>
    private lateinit var userEmailList:MutableList<String>

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
//    private lateinit var mainMapView: MapView
    private lateinit var mMap: GoogleMap
    private val userLatLng = LatLng( 37.4216548, -122.0856375)
    private val ACCESS_LOCATION_REQUEST_CODE=41
    private val REQUEST_CHECK_LOCATION_IS_ON = 1234567

    private lateinit var addressList:MutableList<LatLng>

    override fun onStart() {
        val firstAuth=FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
           if(isLocationEnabled()==true){
                //Toast.makeText(this@DashboardPetOwnerActivity,"Location enabled",Toast.LENGTH_SHORT).show()
                super.onStart()
//                mainMapView.onStart()
            }
            else
           {
                super.onStart()
               // Toast.makeText(this@DashboardPetOwnerActivity,"Location NOT turned on",Toast.LENGTH_SHORT).show()
                checkLocationSettings()
           }
        }
        else{
            startActivity(Intent(this@DashboardPetOwnerActivity, SelectorActivity::class.java))
            finish()
        }

    }
    override fun onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_pet_owner)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        allUsersDatalist= mutableListOf()
        userAttributesList= mutableListOf()

        addressList = mutableListOf(LatLng( 37.4216548, -122.0856375))
        uriList = mutableListOf()
        petSitterNames= mutableListOf()
        userEmailList= mutableListOf()
        currCoords=LatLng(44.436181,26.0198499)

        initui()
        initRTDB()
        initToolbar()
        readDataFromRTDB(currentUser)
        initRecycler()
        getLatLongLocation()

        adapter.setOnItemClickListener { position->
            val clickedItem = adapter.getClickedItem(position)
            clickedItem?.let {
                Log.d("Clicked Item", "Name: ${it.nume}, Email: ${it.email}, Distance: ${it.distance}")
                AlertDialog.Builder(this)
                    .setMessage("Doriti sa vizualizati detaliile PetSitter-ului ?")
                    .setPositiveButton("Detalii"){dialog,_->
                        val toPetSitterDetailsActivity = Intent(this,PetSitterDetailsActivity::class.java)
                        toPetSitterDetailsActivity.putExtra(Constants.email_selected_marker,it.email)
                        startActivity(toPetSitterDetailsActivity)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Inapoi"){dialog,_->
                        dialog.dismiss()
                    }
                    .create().show()
            }

        }
//        mainMapView=findViewById(R.id.main_maps_view)
//        mainMapView.onCreate(savedInstanceState)

//        readLocationsFromRTDB { currentCoords ->
//            Log.i("uriList", "$uriList")
//            mainMapView.getMapAsync {googleMap->
//                addMarkers(googleMap, currentCoords, uriList,petSitterNames,userEmailList)
//                Log.i("ValoriRedateCurrentCoords", "$currentCoords")
//            }
//        }
//
//        mainMapView.getMapAsync(this)


//        toMapsButton.setOnClickListener {
//
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
//                requestPermissionLocation()
//            }
//            else{
//                Toast.makeText(this@DashboardPetOwnerActivity,"Permission Already Granted",Toast.LENGTH_SHORT).show()
//                getLocation()
//            }
//
//                //startActivity(Intent(this,MapsActivity::class.java))
//        }


//        mFusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
//                // Got last known location
//                location?.let {
//                    // Save this location to Firebase Realtime Database
//                    Toast.makeText(this@DashboardPetOwnerActivity,"${it.latitude} ${it.longitude}",Toast.LENGTH_SHORT).show()
//                }
//            }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this@DashboardPetOwnerActivity)

        val toggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {

                databaseReference.child(mAuth.currentUser?.uid.toString()).child("imagine").get()
                    .addOnSuccessListener {
                        val profilePictureNav =
                            findViewById<CircularImageView>(R.id.profileImageNavHeader)
                        profilePictureNav.setOnClickListener {
                            startActivity(Intent(this@DashboardPetOwnerActivity,EditProfilePetOwnerActivity::class.java))
                        }
                        val uriString = it.value.toString()
                        if (it.value!=null){
                            Glide.with(this@DashboardPetOwnerActivity).load(uriString)
                                .into(profilePictureNav)
                            Log.i("firebase", "Got value ${it.value}")
                        }
                    }
                    .addOnFailureListener {
                        //Toast.makeText(this@DashboardPetOwnerActivity,"Could not retrieve image from RTDB",Toast.LENGTH_LONG).show()
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
        isProfileComplete()
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
        val dialog = Dialog(this@DashboardPetOwnerActivity)
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

    private fun getUserDetails(){
        PetSitterReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var index = 0
                allUsersDatalist.clear()
                userAttributesList.clear()
                Log.i("TimesCalledOnDataChanged","Again")
                for (user in snapshot.children) {

                    Log.i("UserSorted","${user}")

                    val uID=user.key.toString()
                    Log.i("UserSorted","${uID}")

                    val nume = user.child("nume").value.toString()
                    val prenume=user.child("prenume").value.toString()
                    val email=user.child("email").value.toString()
                    val rating=user.child("rating").value.toString()
                    val mediePretServiciu=user.child("mediePreturiServicii").value.toString()
                    val nrEvenimente=user.child("nrEvenimente").value.toString()

                    val dataEvenimenteList = mutableListOf<String>()
                    val statusEvenimenteList= mutableListOf<String>()
                    for(event in user.child("Requests").children){
                        val evenimentData = event.child("date").value.toString()
                        val evenimentStatus = event.child("status").value.toString()
                        dataEvenimenteList.add(evenimentData)
                        statusEvenimenteList.add(evenimentStatus)
                    }

                    val URI = user.child("imagine").value.toString()

                    val lat  = user.child("Location").child("latitude").value
                    val long = user.child("Location").child("longitude").value
                    var distance :Double

                    if(lat!=null&&long!=null){
                        distance = calculateDistance(currCoords.latitude,currCoords.longitude,lat.toString().toDouble(),long.toString().toDouble())
                        val currentUserDetails = UserAttributes(uID,distance.toString(),nume,prenume,email,rating,mediePretServiciu,
                            nrEvenimente,URI,dataEvenimenteList,statusEvenimenteList)
                        userAttributesList.add(currentUserDetails)
                        allUsersDatalist.add(currentUserDetails)
                        allUsersDatalist.sortBy { it.distance.toDouble() }
                        adapter.setData(allUsersDatalist)
                        adapter.notifyDataSetChanged()
//                        adapter.notifyItemInserted(index)
//                        index++
                    }
                    else{
                        distance = calculateDistance(currCoords.latitude,currCoords.longitude,44.426767399999996,26.1025384)
                        val currentUserDetails = UserAttributes(uID,distance.toString(),nume,prenume,email,rating,mediePretServiciu,
                            nrEvenimente,URI,dataEvenimenteList,statusEvenimenteList)
                        userAttributesList.add(currentUserDetails)
                        allUsersDatalist.add(currentUserDetails)
                        allUsersDatalist.sortBy { it.distance.toDouble() }
                        adapter.setData(allUsersDatalist)
                        adapter.notifyDataSetChanged()
//                        adapter.notifyItemInserted(index)
//                        index++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ErrorSorting","$error")
            }
        })
    }

    private fun getLatLongLocation() {
        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if(location!=null)
                    currCoords= LatLng(location.latitude,location.longitude)
                getUserDetails()
            }
        }
        else {
            checkLocationSettings()
            //Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
//         val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//         startActivity(intent)
        }
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000 // Earth radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    private fun initRecycler(){
        adapter = FilterAdapter(allUsersDatalist)
        adapter.setOnItemClickListener { position ->
//            showDialogForAction(position)
            //Toast.makeText(this,"Clicked on PetSitter $position",Toast.LENGTH_SHORT).show()
        }
        quickPetSitterRV.layoutManager = LinearLayoutManager(this)
        quickPetSitterRV.setHasFixedSize(false)
        quickPetSitterRV.adapter = adapter
    }

    private fun initui(){
        quickPetSitterRV=findViewById(R.id.quick_petsitter_RV)
        displayName =findViewById(R.id.display_google_name)
//        toMapsButton=findViewById(R.id.maps_button)
        drawerLayout = findViewById(R.id.drawer_layout_dashboard)
    }
    private fun initToolbar(){
        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        customDrawerImage = findViewById(R.id.custom_toolbar_calendar)
        customDrawerImage.setOnClickListener {
            val zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.calendar_on_click)
            customDrawerImage.startAnimation(zoomAnimation)
            startActivity(Intent(this@DashboardPetOwnerActivity, FilterActivity::class.java))
//            Toast.makeText(this, "Custom icon clicked", Toast.LENGTH_SHORT).show()
        }

        toolbarTitle()

    }
    private fun toolbarTitle(){
        currentUser.get().addOnSuccessListener {
            if(it.child("prenume").value!=null&& it.child("nume").value!=null)
                toolbar.title="Gaseste PetSitter-ul perfect->"
                //toolbar.title="Welcome to PetMe ${it.child("prenume").value.toString()} ${it.child("nume").value.toString()}"
            else
                toolbar.title="Gaseste PetSitter-ul perfect->"
        }
    }
    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetOwner")!!
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType)
        PetSitterReference = database.getReference("PetSitter")
        currentUser = databaseReference.child(mAuth.currentUser?.uid.toString())
    }

    private fun readDataFromRTDB(currentUser: DatabaseReference) {

        currentUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for(data in snapshot.children){
                    val currentName = snapshot.child("nume").value.toString()
                    val currentPrenume =snapshot.child("prenume").value.toString()
                    val currentType = snapshot.child("type").value.toString()
                    val currentAdress=snapshot.child("address").value.toString()

                    displayName.text="Bine ai venit la PetMe $currentName $currentPrenume ! Ai nevoie de un PetSitter rapid ?"
            }
            }
            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@DashboardPetOwnerActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })

    }

    private fun readLocationsFromRTDB(callback:(MutableList<LatLng>) -> Unit) {
        var addressListRTDB:MutableList<LatLng> = mutableListOf()

        PetSitterReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                uriList= mutableListOf()
                petSitterNames= mutableListOf()
                userEmailList= mutableListOf()
                var cnt = 0
                for(user in snapshot.children){
                    if(user.child("Location").value==null){
                        val currentAdress=user.child("address").value.toString()
                        val geocoder = Geocoder(this@DashboardPetOwnerActivity, Locale.getDefault())
                        try{
                        val geoList: MutableList<Address>? = geocoder.getFromLocationName(currentAdress, 1)
                        if(geoList!!.isNotEmpty()){
                            val locationData = mapOf(
                                "latitude" to geoList[0].latitude,
                                "longitude" to geoList[0].longitude
                            )
                            PetSitterReference.child(user.key.toString()).child("Location").setValue(locationData)
                            val element = LatLng(geoList[0].latitude,geoList[0].longitude)
                            addressListRTDB.add(element)
                            Log.i("ValoriAdrese","Geocoder address: ${geoList[0].latitude} ${geoList[0].longitude}|" +
                                    " addressList: $addressList | addresslistRTDB: $addressListRTDB")

                            val currentUri = user.child("imagine").value.toString()
                            uriList.add(currentUri)

                            val currentName = user.child("username").value.toString()
                            petSitterNames.add(currentName)

                            val currentEmail=user.child("email").value.toString()
                            userEmailList.add(currentEmail)
                        }
                        }
                        catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    else{
                        val currLat = user.child("Location").child("latitude").value.toString().toDouble()
                        val currLong = user.child("Location").child("longitude").value.toString().toDouble()
                        val geocoder = Geocoder(this@DashboardPetOwnerActivity, Locale.getDefault())
                        val geoList: MutableList<Address>? = geocoder.getFromLocation(currLat,currLong,1)
                        if(geoList!!.isNotEmpty()){
                            val element = LatLng(geoList!![0].latitude,geoList[0].longitude)
                            addressListRTDB.add(element)
                            Log.i("ValoriAdrese","Geocoder address: ${geoList[0].latitude} ${geoList[0].longitude}|" +
                                    " addressList: $addressList | addresslistRTDB: $addressListRTDB")

                            val currentUri = user.child("imagine").value.toString()
                            uriList.add(currentUri)

                            val currentName = user.child("username").value.toString()
                            petSitterNames.add(currentName)

                            val currentEmail=user.child("email").value.toString()
                            userEmailList.add(currentEmail)
                        }

                    }
                }
                callback(addressListRTDB)
            }
            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@DashboardPetOwnerActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_logout->{
                FirebaseAuth.getInstance().signOut()
                Identity.getSignInClient(this).signOut()
                startActivity(Intent(this@DashboardPetOwnerActivity, SelectorActivity::class.java))
                finish()
            }
            R.id.nav_profile->{

                startActivity(Intent(this@DashboardPetOwnerActivity, EditProfilePetOwnerActivity::class.java))

            }
            R.id.nav_pets->{

                startActivity(Intent(this@DashboardPetOwnerActivity, PetsActivity::class.java))

            }
            R.id.nav_made_requests->{

                startActivity(Intent(this@DashboardPetOwnerActivity,MadeRequestsActivity::class.java))

            }
            R.id.nav_reviews->{

                startActivity(Intent(this@DashboardPetOwnerActivity, PetownerGivenReviewsActivity::class.java))

            }

        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermissionLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_LOCATION_REQUEST_CODE)
                }.setNegativeButton(R.string.dialog_button_no) { dialog, _ ->
                    dialog.cancel()
                }.setTitle("Permission needed")
                .setMessage("This permission is needed for accessing your location")
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_LOCATION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE && grantResults.size > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED) {
           // Toast.makeText(this@DashboardPetOwnerActivity,"Permission Granted",Toast.LENGTH_SHORT).show()
            getLocation()
        } else {

            AlertDialog.Builder(this)
                .setPositiveButton("Settings") {_, _ ->
                    goToSettingsForLocation(context = applicationContext)
                }.setNegativeButton("No thanks") {dialog, _ ->
                    dialog.cancel()
                }.setTitle("Go to settings ?")
                .setMessage("This permission is needed for accessing your current location." +
                        " Please allow it in the settings of this app.")
                .show()

            //Toast.makeText(this,"Permission not granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun addMarkers(googleMap: GoogleMap,addressList: MutableList<LatLng>,uriList: MutableList<String>,nameList: MutableList<String>,emailList: MutableList<String>){
        Log.i("ValoriRedateFunctie","$addressList")
        Log.i("uriListFunctie", "$uriList")
        val inflater =LayoutInflater.from(this)
        val markerView=inflater.inflate(R.layout.custom_marker_layout,null)
        val markerImage=markerView.findViewById<CircularImageView>(R.id.marker_image)
        //markerView.findViewById<>()
        Log.i("uriListGetMap", "$uriList")

        for ((index,location) in addressList.withIndex()) {

            Log.i("uriListFor", "$uriList")
            Log.i("cnt", "${uriList[index]}")
            if(uriList[index]!="null"){
                Glide.with(this@DashboardPetOwnerActivity)
                    .asBitmap()
                    .load(uriList[index])
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            // Set the loaded bitmap onto the markerImage
                            markerImage.setImageBitmap(resource)
                            val markerOptions = MarkerOptions()
                            if(nameList[index]!="null"){
                            // Add marker to the map after the image is loaded
                                markerOptions.position(location)
                                .title("PetSitter ${nameList[index]}")
                                .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerView)))
                                .anchor(0.5f, 1.0f)}
                            else{
                                markerOptions.position(location)
                                    .title("PetSitter")
                                    .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerView)))
                                    .anchor(0.5f, 1.0f)
                            }
                            googleMap.addMarker(markerOptions)

                        }
                    })}
                else
            {
                Log.i("uriListFor", "$uriList")
                Log.i("cnt", "${uriList[index]}")
                val markerOptions = MarkerOptions()
                if(nameList[index]!="null"){
                    markerOptions.position(location).title("PetSitter ${nameList[index]}")}
                else{
                    markerOptions.position(location).title("PetSitter")
                }

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerView)))
                markerOptions.anchor(0.5f,1.0f)
                googleMap.addMarker(markerOptions)


            }

        }

    }
    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)


        return bitmap
    }

    private fun getLocation() {

        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                //Toast.makeText(this@DashboardPetOwnerActivity,"$location",Toast.LENGTH_SHORT).show()
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    try {
                    val list: MutableList<Address>? =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        val locationData = mapOf(
                            "latitude" to list!![0].latitude,
                            "longitude" to list[0].longitude
                        )
                        currentUser.child("Location").setValue(locationData)
                            .addOnSuccessListener {
                               // Toast.makeText(this@DashboardPetOwnerActivity,"Saved location",Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                               // Toast.makeText(this@DashboardPetOwnerActivity,"Failed to save location",Toast.LENGTH_SHORT).show()
                                Log.i("ErrorSavingLocation","${it}")
                            }
                        }
                    catch (e:IOException){
                        e.printStackTrace()
                    }
                    }
                }
            }

        else {
            checkLocationSettings()
            //Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
//         val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//         startActivity(intent)
        }
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@DashboardPetOwnerActivity, REQUEST_CHECK_LOCATION_IS_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_LOCATION_IS_ON) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Toast.makeText(this, "Location turned on", Toast.LENGTH_LONG).show()
                    // Location settings enabled by the user.
                    // Proceed with location-related actions.
                    super.onStart()
                }
                Activity.RESULT_CANCELED -> {
                    //Toast.makeText(this, "Location NOT turned on, closing app", Toast.LENGTH_LONG).show()
                    // User chose not to enable location services.
                    // Handle this scenario as needed.
                    finish()
                }
            }
        }
        if(requestCode==0){
            if(resultCode!= RESULT_OK)
            {
                isProfileComplete()
            }
        }
    }

    private fun goToSettingsForLocation(context: Context){

        val newintent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        with(newintent) {
            data = Uri.fromParts("package", context.packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        startActivity(newintent)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.isMyLocationEnabled=true
        mFusedLocationClient.lastLocation.addOnSuccessListener {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude), 18f)) }
        mMap.setMapStyle(null)
        mMap.setOnMarkerClickListener(this)
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        val username=marker.title.toString().substringAfter(" ")
        val email=username.substringAfter("Contact: ")
        //Toast.makeText(this@DashboardPetOwnerActivity,"Clicked on marker $username",Toast.LENGTH_SHORT).show()
        showDialogForAction(username,email)
        return false
    }
    override fun onResume() {
        super.onResume()
//        mainMapView.onResume()
    }
    override fun onPause() {
        super.onPause()
//        mainMapView.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
//        mainMapView.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
//        mainMapView.onLowMemory()
    }
    override fun onStop() {
        super.onStop()
//        mainMapView.onStop()
    }

    private fun showDialogForAction(name:String,email:String) {
        val dialog = Dialog(this@DashboardPetOwnerActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.marker_selected_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnView: Button = dialog.findViewById(R.id.customDialogEditButton)
        val btnBack: Button = dialog.findViewById(R.id.customDialogDeleteButton)

        cdTitle.text="Petsitter $name"
        cdMessage.text="Doriti sa vizualizati detaliile Petsitter-ului $name ?"
        btnView.setOnClickListener {
            val toPetSitterDetails=Intent(this, PetSitterDetailsActivity::class.java)
            toPetSitterDetails.putExtra(Constants.email_selected_marker,email)
            Log.i("PassedEmail","$email")
            startActivity(toPetSitterDetails)
           // Toast.makeText(this@DashboardPetOwnerActivity, "Pressed View", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnBack.setOnClickListener {
           // Toast.makeText(this@DashboardPetOwnerActivity, "Pressed Back", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getDataFromRTDB(name: String){

    }

    override fun getInfoContents(marker: Marker): View? {
        val infoWindow = layoutInflater.inflate(R.layout.custom_info_window, null)

        // Set the user's name in the custom info window
        val userNameTextView = infoWindow.findViewById<TextView>(R.id.userNameTextView)
        userNameTextView.text = marker.title

        return infoWindow
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }


}