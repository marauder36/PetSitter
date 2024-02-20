package com.example.firebasertdb.activities.PetOwner.Filter

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.clickedOnPetSitter.PetSitterDetailsActivity
import com.example.firebasertdb.activities.PetSitterOwner.Filter.adapter.FilterAdapter
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.model.UserAttributes
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FilterActivity : AppCompatActivity(),CustomDialogListener,DateSelectionListener {

    private lateinit var sortingOptions:Spinner
    private lateinit var orderButton: Button
    private lateinit var distance100mBtn:Button
    private lateinit var distance1kmBtn:Button
    private lateinit var distanceCustomBtn:Button
    private lateinit var dispMaineButton:Button
    private lateinit var dispSaptViitoareButton:Button
    private lateinit var dispCustomButton:Button

    private lateinit var listaFiltrata100m:MutableList<UserAttributes>
    private lateinit var listaFiltrata1km:MutableList<UserAttributes>
    private lateinit var listaFiltrataCustom:MutableList<UserAttributes>
    private lateinit var listaFiltrataMaine:MutableList<UserAttributes>
    private lateinit var listaFiltrataWeek:MutableList<UserAttributes>
    private lateinit var listaFiltrataCustomDisp:MutableList<UserAttributes>


    private lateinit var todayDate:Date

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FilterAdapter
    private lateinit var allUsersDatalist: MutableList<UserAttributes>
    private lateinit var dataClass: UserAttributes

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var sharedManager : SharedStorageManager

    private lateinit var userAttributesList: MutableList<UserAttributes>

    private lateinit var activeFilters: MutableSet<String>

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var currCoords:LatLng
    private val REQUEST_CHECK_LOCATION_IS_ON = 1234567

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@FilterActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_filter)

        todayDate = Date()
        listaFiltrataMaine= mutableListOf()
        listaFiltrataWeek= mutableListOf()
        listaFiltrata100m= mutableListOf()
        listaFiltrata1km= mutableListOf()
        listaFiltrataCustom= mutableListOf()

        activeFilters = mutableSetOf()
        currCoords=LatLng(44.436181,26.0198499)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        userAttributesList= mutableListOf()
        allUsersDatalist = mutableListOf()

        initUI()
        initToolbar(toolbar)
        initRTDB()
        getLocation()
        getUserDetails()
        initRecycler()

        orderButton.setOnClickListener {
            if(distance100mBtn.isSelected){
                if(dispMaineButton.isSelected){
                    val intersect = listaFiltrata100m.intersect(listaFiltrataMaine).toMutableList()
                    sort(intersect)
                }
                else if(dispSaptViitoareButton.isSelected){
                    val intersect = listaFiltrata100m.intersect(listaFiltrataWeek).toMutableList()
                    sort(intersect)
                }
                else if(dispCustomButton.isSelected){
                    val intersect = listaFiltrata100m.intersect(listaFiltrataCustomDisp).toMutableList()
                    sort(intersect)
                }else{
                    sort(listaFiltrata100m)
                }
            }
            else if(distance1kmBtn.isSelected)
                if(dispMaineButton.isSelected){
                    val intersect = listaFiltrata1km.intersect(listaFiltrataMaine).toMutableList()
                    sort(intersect)
                }
                else if(dispSaptViitoareButton.isSelected){
                    val intersect = listaFiltrata1km.intersect(listaFiltrataWeek).toMutableList()
                    sort(intersect)
                }
                else if(dispCustomButton.isSelected){
                    val intersect = listaFiltrata1km.intersect(listaFiltrataCustomDisp).toMutableList()
                    sort(intersect)
                }else{
                    sort(listaFiltrata1km)
                }
            else if(distanceCustomBtn.isSelected)
                if(dispMaineButton.isSelected){
                    val intersect = listaFiltrataCustom.intersect(listaFiltrataMaine).toMutableList()
                    sort(intersect)
                }
                else if(dispSaptViitoareButton.isSelected){
                    val intersect = listaFiltrataCustom.intersect(listaFiltrataWeek).toMutableList()
                    sort(intersect)
                }
                else if(dispCustomButton.isSelected){
                    val intersect = listaFiltrataCustom.intersect(listaFiltrataCustomDisp).toMutableList()
                    sort(intersect)
                }else{
                    sort(listaFiltrataCustom)
                }

            else if(dispMaineButton.isSelected)
                if(distance100mBtn.isSelected){
                    val intersect = listaFiltrataMaine.intersect(listaFiltrata100m).toMutableList()
                    sort(intersect)
                }
                else if(distance1kmBtn.isSelected){
                    val intersect = listaFiltrataMaine.intersect(listaFiltrata1km).toMutableList()
                    sort(intersect)
                }
                else if(distanceCustomBtn.isSelected){
                    val intersect = listaFiltrataMaine.intersect(listaFiltrataCustom).toMutableList()
                    sort(intersect)
                }else{
                    sort(listaFiltrataMaine)
                }
            else if(dispSaptViitoareButton.isSelected)
                if(distance100mBtn.isSelected){
                    val intersect = listaFiltrataWeek.intersect(listaFiltrata100m).toMutableList()
                    sort(intersect)
                }
                else if(distance1kmBtn.isSelected){
                    val intersect = listaFiltrataWeek.intersect(listaFiltrata1km).toMutableList()
                    sort(intersect)
                }
                else if(distanceCustomBtn.isSelected){
                    val intersect = listaFiltrataWeek.intersect(listaFiltrataCustom).toMutableList()
                    sort(intersect)
                }else{
                    sort(listaFiltrataWeek)
                }
            else if(dispCustomButton.isSelected){
                if(distance100mBtn.isSelected){
                    val intersect = listaFiltrataCustomDisp.intersect(listaFiltrata100m).toMutableList()
                    sort(intersect)
                }
                else if(distance1kmBtn.isSelected){
                    val intersect = listaFiltrataCustomDisp.intersect(listaFiltrata1km).toMutableList()
                    sort(intersect)
                }
                else if(distanceCustomBtn.isSelected){
                    val intersect = listaFiltrataCustomDisp.intersect(listaFiltrataCustom).toMutableList()
                    sort(intersect)
                }else{
                    sort(listaFiltrataCustomDisp)
                }
            }
            else
                sort(allUsersDatalist)
        }

        distance100mBtn.setOnClickListener {
            if (distance1kmBtn.isSelected)
                toggleFilter(distance1kmBtn,"1km")
            resetCustomDistanceButton()
            if(!distance100mBtn.isSelected)
            {
                toggleFilter(distance100mBtn,"100m")
                listaFiltrata100m= filterListDistance(100)

                if(dispMaineButton.isSelected){
                    val intersect = listaFiltrata100m.intersect(listaFiltrataMaine).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else if(dispSaptViitoareButton.isSelected){
                    val intersect = listaFiltrata100m.intersect(listaFiltrataWeek).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else if (dispCustomButton.isSelected){
                    val intersect = listaFiltrata100m.intersect(listaFiltrataCustomDisp).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else{
                    adapter.setData(listaFiltrata100m)
                    adapter.notifyDataSetChanged()}
            }
            else if(dispMaineButton.isSelected){
                toggleFilter(distance100mBtn,"100m")
                adapter.setData(listaFiltrataMaine)
                adapter.notifyDataSetChanged()
            }
            else if(dispSaptViitoareButton.isSelected){
                toggleFilter(distance100mBtn,"100m")
                adapter.setData(listaFiltrataWeek)
                adapter.notifyDataSetChanged()
            }
            else if(dispCustomButton.isSelected){
                toggleFilter(distance100mBtn,"100m")
                adapter.setData(listaFiltrataCustomDisp)
                adapter.notifyDataSetChanged()
            }
            else
            {
                toggleFilter(distance100mBtn,"100m")
                adapter.setData(userAttributesList)
                adapter.notifyDataSetChanged()
            }
        }

        distance1kmBtn.setOnClickListener {
            if (distance100mBtn.isSelected)
                toggleFilter(distance100mBtn,"100m")
            resetCustomDistanceButton()
            if(!distance1kmBtn.isSelected)
            {
                toggleFilter(distance1kmBtn,"1km")
                listaFiltrata1km=filterListDistance(1000)
                if(dispMaineButton.isSelected){
                    val intersect = listaFiltrata1km.intersect(listaFiltrataMaine).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else if (dispSaptViitoareButton.isSelected){
                    val intersect = listaFiltrata1km.intersect(listaFiltrataWeek).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else if(dispCustomButton.isSelected){
                    val intersect = listaFiltrata1km.intersect(listaFiltrataCustomDisp).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else{
                    adapter.setData(listaFiltrata1km)
                    adapter.notifyDataSetChanged()
                }
            }
            else if(dispMaineButton.isSelected){
                toggleFilter(distance1kmBtn,"1km")
                adapter.setData(listaFiltrataMaine)
                adapter.notifyDataSetChanged()
            }
            else if (dispSaptViitoareButton.isSelected){
                toggleFilter(distance1kmBtn,"1km")
                adapter.setData(listaFiltrataWeek)
                adapter.notifyDataSetChanged()
            }
            else if(dispCustomButton.isSelected){
                toggleFilter(distance1kmBtn,"1km")
                adapter.setData(listaFiltrataCustomDisp)
                adapter.notifyDataSetChanged()
            }
            else{
                toggleFilter(distance1kmBtn,"1km")
                adapter.setData(userAttributesList)
                adapter.notifyDataSetChanged()
            }
        }

        distanceCustomBtn.setOnClickListener {
            if (distanceCustomBtn.isSelected){
                AlertDialog.Builder(this)
                    .setPositiveButton("Editeaza"){_,_->
                        selectDistanceDialog(this)
                    }
                    .setNegativeButton("Inlatura"){_,_->
                        toggleFilter(distanceCustomBtn,"Custom Distance")
                        distanceCustomBtn.text="+ Selecteaza distanta"
                        if(dispMaineButton.isSelected){
                            adapter.setData(listaFiltrataMaine)
                            adapter.notifyDataSetChanged()
                        }
                        else if(dispSaptViitoareButton.isSelected){
                            adapter.setData(listaFiltrataWeek)
                            adapter.notifyDataSetChanged()
                        }
                        else if(dispCustomButton.isSelected){
                            adapter.setData(listaFiltrataCustomDisp)
                            adapter.notifyDataSetChanged()
                        }
                        else{
                            adapter.setData(userAttributesList)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    .setMessage("Doriti sa editati acest tag sau sa il inlaturati ?")
                    .create().show()

            }
            else
                selectDistanceDialog(this)
        }

        dispMaineButton.setOnClickListener {
            if (dispSaptViitoareButton.isSelected)
                toggleFilter(dispSaptViitoareButton,"Sapt")
            resetCustomDateButton()
            if(!dispMaineButton.isSelected)
            {
                toggleFilter(dispMaineButton,"Maine")
                filterListDateTommorrow()

                if(distance100mBtn.isSelected){
                    val intersect = listaFiltrataMaine.intersect(listaFiltrata100m).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()}
                else if (distance1kmBtn.isSelected){
                    val intersect = listaFiltrataMaine.intersect(listaFiltrata1km).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()}
                else if (distanceCustomBtn.isSelected){
                    val intersect = listaFiltrataMaine.intersect(listaFiltrataCustom).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()}
                else{
                    adapter.setData(listaFiltrataMaine)
                    adapter.notifyDataSetChanged()
                }
            }
            else if (distance100mBtn.isSelected){
                toggleFilter(dispMaineButton,"Maine")
                adapter.setData(listaFiltrata100m)
                adapter.notifyDataSetChanged()
            }
            else if(distance1kmBtn.isSelected){
                toggleFilter(dispMaineButton,"Maine")
                adapter.setData(listaFiltrata1km)
                adapter.notifyDataSetChanged()
            }
            else if(distanceCustomBtn.isSelected){
                toggleFilter(dispMaineButton,"Maine")
                adapter.setData(listaFiltrataCustom)
                adapter.notifyDataSetChanged()
            }
            else{
                toggleFilter(dispMaineButton,"Maine")
                adapter.setData(userAttributesList)
                adapter.notifyDataSetChanged()
            }
        }

        dispSaptViitoareButton.setOnClickListener {
            if (dispMaineButton.isSelected)
                toggleFilter(dispMaineButton,"Maine")
            resetCustomDateButton()
            if(!dispSaptViitoareButton.isSelected){

                toggleFilter(dispSaptViitoareButton,"Sapt")
                filterListDateWeek()

                if (distance100mBtn.isSelected){
                    val intersect = listaFiltrataWeek.intersect(listaFiltrata100m).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else if (distance1kmBtn.isSelected){
                    val intersect = listaFiltrataWeek.intersect(listaFiltrata1km).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else if(distanceCustomBtn.isSelected){
                    val intersect = listaFiltrataWeek.intersect(listaFiltrataCustom).toMutableList()
                    adapter.setData(intersect)
                    adapter.notifyDataSetChanged()
                }
                else{
                    adapter.setData(listaFiltrataWeek)
                    adapter.notifyDataSetChanged()
                }
            }
            else if(distance100mBtn.isSelected){
                toggleFilter(dispSaptViitoareButton,"Sapt")
                adapter.setData(listaFiltrata100m)
                adapter.notifyDataSetChanged()
            }
            else if (distance1kmBtn.isSelected){
                toggleFilter(dispSaptViitoareButton,"Sapt")
                adapter.setData(listaFiltrata1km)
                adapter.notifyDataSetChanged()
            }
            else if (distanceCustomBtn.isSelected){
                toggleFilter(dispSaptViitoareButton,"Sapt")
                adapter.setData(listaFiltrataCustom)
                adapter.notifyDataSetChanged()
            }
            else{
                toggleFilter(dispSaptViitoareButton,"Sapt")
                adapter.setData(userAttributesList)
                adapter.notifyDataSetChanged()
            }
        }

        dispCustomButton.setOnClickListener {
            if (dispCustomButton.isSelected){
            AlertDialog.Builder(this)
                .setPositiveButton("Editeaza"){_,_->
                    showCustomDatePickerDialog(this)
                }
                .setNegativeButton("Inlatura"){_,_->
                    toggleFilter(dispCustomButton,"Custom Date")
                    dispCustomButton.text="+ Selecteaza data"

                    if(distance100mBtn.isSelected){
                        adapter.setData(listaFiltrata100m)
                        adapter.notifyDataSetChanged()
                    }
                    else if(distance1kmBtn.isSelected){
                        adapter.setData(listaFiltrata1km)
                        adapter.notifyDataSetChanged()
                    }
                    else if(distanceCustomBtn.isSelected){
                        adapter.setData(listaFiltrataCustom)
                        adapter.notifyDataSetChanged()
                    }
                    else{
                        adapter.setData(userAttributesList)
                        adapter.notifyDataSetChanged()
                    }
                }
                .setMessage("Doriti sa inlaturati acest tag sau sa il editati ?")
                .create().show()

        }
            else
                showCustomDatePickerDialog(this)
        }

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

    }
    private fun filterListDateTommorrow():MutableList<UserAttributes>{
        listaFiltrataMaine=allUsersDatalist.toMutableList()
        for(user in allUsersDatalist) {
            var cntEvents = 0
            var index = 0
            for (el in user.datiEvenimente) {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy")
                val date = dateFormat.parse(el)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val tommorow = calendar.time
                val resultMaine = dateFormat.format(date).compareTo(dateFormat.format(tommorow))
                if (resultMaine == 0 && user.statusEvenimente[index]=="Accepted")
                    cntEvents++
            }
            if (cntEvents > 3)
            {
                listaFiltrataMaine .apply {
                        remove(user) }
            }
        }
        Log.i("EvenimenteMaine", "${allUsersDatalist.size}")
        Log.i("EvenimenteMaine", "${listaFiltrataMaine.size}")
        return listaFiltrataMaine
    }

    private fun filterListDateWeek():MutableList<UserAttributes>{
        listaFiltrataWeek=allUsersDatalist.toMutableList()
        for(user in allUsersDatalist) {
            var cntEvents = 0
            var index = 0
            for (el in user.datiEvenimente) {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy")
                val date = dateFormat.parse(el)
                val currentDate = Date()
                val calendar = Calendar.getInstance()
                calendar.time=currentDate
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val resultWeek = date.before(calendar.time)
                //val resultBeforeToday = date.before(Date())
                //Log.d("NrEvenimente","$resultBeforeToday")
                Log.d("NrEvenimente","$resultWeek")
                if (resultWeek && user.statusEvenimente[index]=="Accepted")//&& !resultBeforeToday
                    cntEvents++
                index++
            }
            Log.d("NrEvenimente","$cntEvents")

            if (cntEvents >= 21)
            {
                listaFiltrataWeek .apply {
                        remove(user) }
            }
        }
        Log.i("EvenimenteMaine", "${allUsersDatalist.size}")
        Log.i("EvenimenteMaine", "${listaFiltrataWeek.size}")
        return listaFiltrataWeek
    }

    private fun sort(userAttributesList: MutableList<UserAttributes>){

        var sortedByDistance    = mutableListOf<UserAttributes>()
        var sortedByPretMediu   = mutableListOf<UserAttributes>()
        var sortedByRating      = mutableListOf<UserAttributes>()
        var sortedByDisponibil  = mutableListOf<UserAttributes>()
        var sortedByCriteriaList =mutableListOf<UserAttributes>()

        val selectedSortingCriteria = sortingOptions.selectedItem.toString()

        if(selectedSortingCriteria=="Distanta"){
            sortedByDistance = userAttributesList.sortedBy {
                it.distance.toDoubleOrNull()?:0.0
            }.toMutableList()
            sortedByCriteriaList = sortedByDistance.reversed().toMutableList()
            adapter.setData(sortedByDistance)
            adapter.notifyDataSetChanged()
        }
        else if(selectedSortingCriteria=="Pret Mediu Servicii") {
            sortedByPretMediu = userAttributesList.sortedBy {
                it.mediePretServiciu.toDoubleOrNull()?:0.0
            }.toMutableList()
            adapter.setData(sortedByPretMediu)
            adapter.notifyDataSetChanged()
        }
        else if (selectedSortingCriteria=="Rating"){
            sortedByRating = userAttributesList.sortedBy {
                it.rating.toDoubleOrNull()?:0.0
            }.toMutableList()
            adapter.setData(sortedByRating.reversed().toMutableList())
            adapter.notifyDataSetChanged()
        }
        else if (selectedSortingCriteria=="Disponibilitate"){
            sortedByDisponibil=userAttributesList.sortedBy {
                it.nrEvenimente.toDoubleOrNull()?:0.0
            }.toMutableList()
            adapter.setData(sortedByDisponibil)
            adapter.notifyDataSetChanged()
        }
        Log.i("sortedUserList","Got: $userAttributesList")
        Log.i("sortedBySelectedSorting","Got: $selectedSortingCriteria")
        Log.i("sortedDataList","Got: $allUsersDatalist")
        Log.i("sortedByCriteriaList","Got: $sortedByCriteriaList")
        Log.i("sortedByDistance","Got: $sortedByDistance")
        Log.i("sortedByRating","Got: $sortedByRating")
        Log.i("sortedByDisponibil","Got: $sortedByDisponibil")
        Log.i("sortedByPretMediu","Got: $sortedByPretMediu")
    }

    private fun resetCustomDistanceButton(){
        distanceCustomBtn.text="+ Selecteaza distanta"
        distanceCustomBtn.isSelected=false
        distanceCustomBtn.setBackgroundResource(R.drawable.button_backg_selected)
    }

    private fun resetCustomDateButton(){
        dispCustomButton.text="+ Select date"
        dispCustomButton.isSelected=false
        dispCustomButton.setBackgroundResource(R.drawable.button_backg_selected)
    }

    private fun initUI(){

        toolbar=findViewById(R.id.filter_main_toolbar)
        recyclerView=findViewById(R.id.filtered_petsitters_rv)
        orderButton=findViewById(R.id.applySortingButton)

        distance100mBtn = findViewById(R.id.distance_100)
        distance1kmBtn=findViewById(R.id.distance_1km)
        distanceCustomBtn=findViewById(R.id.distance_select)

        dispMaineButton=findViewById(R.id.disp_maine)
        dispSaptViitoareButton=findViewById(R.id.disp_sapt_viitore)
        dispCustomButton=findViewById(R.id.disp_luna_viitoare)

        sortingOptions=findViewById(R.id.sortingOptionsSpinner)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.sorting_options,
            R.layout.custom_spinner_item_layout)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortingOptions.adapter=adapter

    }

    private fun toggleFilter(button: Button, filter: String) {
        // Toggle the filter selection
        if (activeFilters.contains(filter)) {
            activeFilters.remove(filter) // Filter already active, so deselect it
            updateButtonState(button = button,filter=filter)
        } else {
            activeFilters.add(filter) // Filter not active, so select it
            updateButtonState(button = button,filter=filter)
        }
    }

    private fun updateButtonState(button: Button, filter: String) {
        // Update button appearance based on filter state
        if (activeFilters.contains(filter)) {
            // Button is active, set its selected state
            val initialText = button.text.toString()
            val updatedText = initialText.substringAfter("+")
            button.isSelected = true
            button.text=updatedText

            // Set the background drawable for selected state
            button.setBackgroundResource(R.drawable.button_backg_selected)

        } else {
            // Button is inactive, clear its selected state

            val initialText = button.text.toString()
            val updatedText = "+$initialText"
            button.isSelected = false
            button.text=updatedText
            // Set the background drawable for default state
            button.setBackgroundResource(R.drawable.button_backg_selected)
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        //val prefType = sharedManager.getString("UserType")
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("PetSitter")
    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Filtrare PetSitters"
        }
    }

    private fun initRecycler() {
        adapter = FilterAdapter(allUsersDatalist)
        adapter.setOnItemClickListener { position ->
//            showDialogForAction(position)
            //Toast.makeText(this,"Clicked on PetSitter $position",Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter

    }

    private fun getUserDetails(){
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        adapter.notifyItemInserted(index)
                        index++
                    }
                    else{
                        distance = 42069420.69
                        val currentUserDetails = UserAttributes(uID,distance.toString(),nume,prenume,email,rating,mediePretServiciu,
                            nrEvenimente,URI,dataEvenimenteList,statusEvenimenteList)
                        userAttributesList.add(currentUserDetails)
                        allUsersDatalist.add(currentUserDetails)
                        adapter.notifyItemInserted(index)
                        index++
                    }


                }
                
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ErrorSorting","$error")
            }
        })
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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
    
    private fun getLocation() {
        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if(location!=null)
                currCoords= LatLng(location.latitude,location.longitude)
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
                    exception.startResolutionForResult(this@FilterActivity, REQUEST_CHECK_LOCATION_IS_ON)
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
    }

    private fun showCustomDatePickerDialog(listener: DateSelectionListener) {
        val dialog = Dialog(this) // 'this' refers to your activity, use 'requireActivity()' if in a fragment
        dialog.setContentView(R.layout.custom_date_dialog)

        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)
        val btnSelectDate = dialog.findViewById<Button>(R.id.btnSelectDate)

        btnSelectDate.setOnClickListener {
            val year = datePicker.year
            val month = datePicker.month
            val dayOfMonth = datePicker.dayOfMonth

            listener.onDateSelected(year, month, dayOfMonth)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun selectDistanceDialog(listener: CustomDialogListener){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.select_distance_dialog_layout, null)
        val editTextDistance = dialogView.findViewById<EditText>(R.id.editTextDistance)
        val spinnerUnit = dialogView.findViewById<Spinner>(R.id.spinnerUnit)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setTitle("Introdu distanta")
            .setPositiveButton("OK") { dialog, _ ->
                val distance = editTextDistance.text.toString().toInt()
                val unit = spinnerUnit.selectedItem.toString()
                listener.onValuesSelected(distance, unit)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onValuesSelected(distance: Int, unit: String) {
        resetDistanceButtons()
        val yourButton = findViewById<Button>(R.id.distance_select)
        // Update the button text with the selected values
        yourButton.text = "La mai putin de: $distance $unit"
//        yourButton.isSelected = true
//        // Set the background drawable for selected state
//        yourButton.setBackgroundResource(R.drawable.button_backg_selected)
        toggleFilter(distanceCustomBtn,"Custom Distance")
        if(unit=="km") {
            listaFiltrataCustom = filterListDistance(distance*1000)
        }
        else{
            listaFiltrataCustom = filterListDistance(distance)
        }

        if(dispMaineButton.isSelected){
            val intersect = listaFiltrataCustom.intersect(listaFiltrataMaine).toMutableList()
            adapter.setData(intersect)
            adapter.notifyDataSetChanged()
        }
        else if (dispSaptViitoareButton.isSelected){
            val intersect = listaFiltrataCustom.intersect(listaFiltrataWeek).toMutableList()
            adapter.setData(intersect)
            adapter.notifyDataSetChanged()
        }else if (dispCustomButton.isSelected){
            val intersect = listaFiltrataCustom.intersect(listaFiltrataCustomDisp).toMutableList()
            adapter.setData(intersect)
            adapter.notifyDataSetChanged()
        }else{
        adapter.setData(listaFiltrataCustom)
        adapter.notifyDataSetChanged()}
    }

    override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
        resetDispButtons()
        val selectedDateText = "$dayOfMonth-${month + 1}-$year" // Month is zero-based
        val yourButton=findViewById<Button>(R.id.disp_luna_viitoare)
        yourButton.text= "Disp pe data de: $selectedDateText"
//        yourButton.isSelected=true
//        yourButton.setBackgroundResource(R.drawable.button_backg_selected)
        if(!dispCustomButton.isSelected)
            toggleFilter(dispCustomButton,"Custom Date")
        filterListDisp(selectedDateText)

        if(distance100mBtn.isSelected){
            val intersect = listaFiltrataCustomDisp.intersect(listaFiltrata100m).toMutableList()
            adapter.setData(intersect)
            adapter.notifyDataSetChanged()
        }
        else if(distance1kmBtn.isSelected){
            val intersect = listaFiltrataCustomDisp.intersect(listaFiltrata1km).toMutableList()
            adapter.setData(intersect)
            adapter.notifyDataSetChanged()
        }
        else if(distanceCustomBtn.isSelected){
            val intersect = listaFiltrataCustomDisp.intersect(listaFiltrataCustom).toMutableList()
            adapter.setData(intersect)
            adapter.notifyDataSetChanged()
        }
        else{
        adapter.setData(listaFiltrataCustomDisp)
        adapter.notifyDataSetChanged()
        }
        Log.i("DataAleasaDeUser", selectedDateText)
    }

    private fun filterListDisp(selectedDate:String):MutableList<UserAttributes>{
        listaFiltrataCustomDisp=allUsersDatalist.toMutableList()
        for(user in allUsersDatalist) {
            var cntEvents = 0
            var index = 0
            for (el in user.datiEvenimente) {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy")
                val date = dateFormat.parse(el)
                val customDate = dateFormat.parse(selectedDate)
                val resultSelectedDate = dateFormat.format(date).compareTo(dateFormat.format(customDate))
                val status = user.statusEvenimente[index]
                if (resultSelectedDate == 0 && status=="Accepted")
                    cntEvents++
                index++
            }
            if (cntEvents > 3)
            {
                listaFiltrataCustomDisp.apply {
                    remove(user) }
            }
        }
        Log.i("EvenimenteCustom", "${allUsersDatalist.size}")
        Log.i("EvenimenteCustom", "${listaFiltrataCustomDisp}")
        return listaFiltrataCustomDisp
    }

    private fun filterListDistance(filter:Int):MutableList<UserAttributes>{
        val filteredList = allUsersDatalist.filter {
            ceil(it.distance.toDouble())<filter
        }
        return filteredList.toMutableList()
    }

    private fun resetDistanceButtons() {
        if (distance100mBtn.isSelected) {
            toggleFilter(distance100mBtn,"100m")

        } else if (distance1kmBtn.isSelected)
        {
            toggleFilter(distance1kmBtn,"1km")

        }
    }
    private fun resetDispButtons() {
        if (dispMaineButton.isSelected) {
            toggleFilter(dispMaineButton,"Maine")

        } else if (dispSaptViitoareButton.isSelected)
        {
            toggleFilter(dispSaptViitoareButton,"Sapt")

        }
    }


}
interface CustomDialogListener {
    fun onValuesSelected(distance: Int, unit: String)
}

interface DateSelectionListener {
    fun onDateSelected(year: Int, month: Int, dayOfMonth: Int)
}
