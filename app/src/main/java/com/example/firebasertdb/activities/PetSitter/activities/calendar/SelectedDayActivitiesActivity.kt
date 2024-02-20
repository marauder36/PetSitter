package com.example.firebasertdb.activities.PetSitter.activities.calendar

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetSitter.activities.calendar.adapters.AdapterCalendar
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.ServiceRequestClass
import com.example.firebasertdb.models.ServiceRequestClassEvent
import com.example.firebasertdb.models.activitateZi
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectedDayActivitiesActivity : AppCompatActivity() {
    private lateinit var dataZi:String
    private lateinit var dataList: MutableList<ServiceRequestClassEvent>

    private lateinit var recyclerViewActivitati: RecyclerView
    private lateinit var adapter: AdapterCalendar
    private lateinit var numeActivitateDeTrimis:String
    private lateinit var activitatiID:MutableList<String>
    private lateinit var numeActivitate:String
    private lateinit var dataActivitate:String
    private lateinit var oraActivitate:String

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var sharedManager : SharedStorageManager

    private lateinit var dailyActivitiesTextView: TextView

    private lateinit var databaseCurrentDailyActivities:DatabaseReference

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@SelectedDayActivitiesActivity, SelectorActivity::class.java))
            finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Handle back button click
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_day_activities)

        dataList = mutableListOf()
        activitatiID= mutableListOf()

        initUI()
        initRTDB()
        initToolbar(toolbar)
        initRecycler()
        fetchFromFirebase()

    }

    private fun initUI(){
        dailyActivitiesTextView=findViewById(R.id.daily_activities_text)

        if(intent.getStringExtra(Constants.ZI_ALEASA)!=null) {
            dataZi = intent.getStringExtra(Constants.ZI_ALEASA)!!
        }else{
            dataZi = "DD-MM-YY"
        }

        toolbar=findViewById(R.id.calendar_zi_aleasa_toolbar)
        recyclerViewActivitati=findViewById(R.id.recycler_view_daily_activities)
    }

    private fun fetchFromFirebase() {
        dataList.clear()
        activitatiID.clear()

        databaseCurrentDailyActivities.get().addOnSuccessListener {

            for(request in it.children) {
                numeActivitateDeTrimis=request.key.toString()
                Log.i("fetchFromFirebase", "$numeActivitateDeTrimis")
                Log.i(
                    "TitluID",
                    "Got: $numeActivitateDeTrimis"
                )
                numeActivitate = request.key.toString()
                dataActivitate = request.child("date").value.toString()
                oraActivitate = request.child("hour").value.toString()

                val requestingUserID = request.child("requestingUserID").value.toString()
                val requestingUserName=request.child("requestingUserName").value.toString()
                val serviceRequested = request.child("serviceRequested").value.toString()
                val status = request.child("status").value.toString()
                val date = request.child("date").value.toString()
                val hour = request.child("hour").value.toString()
                val petID = request.child("petID").value.toString()
                val petName = request.child("petName").value.toString()
                val petRace = request.child("petRace").value.toString()
                val descriereExtra = request.child("descriereExtra").value.toString()

                Log.i("DataList","$dataList")
                if (dataActivitate==dataZi&&status=="Accepted"){
                    activitatiID.add(request.key.toString())
                    dataList.add(ServiceRequestClassEvent(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,descriereExtra))
                    adapter.setData(dataList)
                    adapter.notifyDataSetChanged()
                }
            }
            if (dataList.isEmpty()){
                dailyActivitiesTextView.text="Nu ai nicio rezervare in aceasta zi"
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

    }

    private fun retrieveDataFromListField(position: Int): String {
        Log.d("PositionRetrieveData","Position: $position")
        if (position >= 0 && position < dataList.size) {
            return activitatiID[position]
        } else {
            //Toast.makeText(this," $position + ${dataList}", Toast.LENGTH_LONG).show()
            return "Am iesit din index"
        }
    }

    private fun showDialogForAction(position: Int,request:ServiceRequestClassEvent) {
        val dialog = Dialog(this@SelectedDayActivitiesActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_activitate_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnEdit: Button = dialog.findViewById(R.id.customDialogEditButton)
        val btnDelete: Button = dialog.findViewById(R.id.customDialogDeleteButton)

        Log.d("PositionInDialog",position.toString())

        val titluComplet = retrieveDataFromListField(position)

        val numeUserRezervare=retrieveDataFromListField(position).substringBefore(" a")
        Log.i("NumePentruTitlu","Got: $numeUserRezervare")

        val dataRezervareCuNumePet=retrieveDataFromListField(position).substringAfter("pe data de ")
        Log.i("NumePentruTitlu","Got: $dataRezervareCuNumePet")

        val dataRezervare =dataRezervareCuNumePet.substringBefore(" pentru")
        Log.i("NumePentruTitlu","Got: $dataRezervare")

        cdTitle.text = "$numeUserRezervare a creat o rezervare pe data de $dataRezervare"

        btnEdit.setOnClickListener {
            var requestCode = 1
            var toEditIntent = Intent(this, EditActivitateActivity::class.java)
            Log.i("PozitieIntent","Got: $position")
            Log.i("PozitieIntent","Got: ${activitatiID[position]}")
            toEditIntent.putExtra(Constants.nume_activitate_zi_aleasa_plus_data,activitatiID[position])
            Log.d("PositionInDialog","${activitatiID[position]}")
            toEditIntent.putExtra(Constants.nume_petowner_activitate_zi_aleasa,request.requestingUserID)
            startActivityForResult(toEditIntent,requestCode)
            //Toast.makeText(this@ServiciiActivity, "Pressed EDIT", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            deleteActivitate(position)
            //Toast.makeText(this@ServiciiActivity, "Pressed DELETE", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteActivitate(position: Int) {
        Log.d("PositionDeleteActivit","Position: $position")
        if (position >= 0 && position < dataList.size ) {

            val childName = activitatiID[position]

            databaseCurrentDailyActivities.child(childName).child("status").setValue("Canceled by PetSitter")
                .addOnSuccessListener {

                    dataList.removeAt(position)
                    activitatiID.removeAt(position)
                    adapter.notifyItemRemoved(position)

//                    Toast.makeText(
//                        this@SelectedDayActivitiesActivity,
//                        "Deleted activitate from database",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
                .addOnFailureListener {
//                    Toast.makeText(
//                        this@SelectedDayActivitiesActivity,
//                        "Could not delete data from database",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
        } else {
//            Toast.makeText(
//                this@SelectedDayActivitiesActivity,
//                "Could not delete, index out of bounds",
//                Toast.LENGTH_LONG
//            ).show()
        }
    }

    private fun initRecycler() {
        adapter = AdapterCalendar(dataList)
        adapter.setOnItemClickListener { position ->
            val request = adapter.getClickedItem(position)
            if(request!=null)
                showDialogForAction(position,request)
        }
        recyclerViewActivitati.layoutManager = LinearLayoutManager(this)
        recyclerViewActivitati.setHasFixedSize(false)
        recyclerViewActivitati.adapter = adapter
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        //val prefType = sharedManager.getString("UserType")
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType.toString())
        databaseCurrentDailyActivities=databaseReference.child(mAuth.currentUser!!.uid).child("Requests")
    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "$dataZi"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                countEvents()
                fetchFromFirebase()
            }
            if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this@ServiciiActivity, "Bad REsult Code", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun countEvents(){
        var currentUser=databaseReference.child(mAuth.currentUser!!.uid)
        currentUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser.child("nrEvenimente").setValue(snapshot.child("Requests").childrenCount.toInt())
                Log.i("Data Accessed","Again")
            }
            override fun onCancelled(error: DatabaseError) {
               // Toast.makeText(this@SelectedDayActivitiesActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onBackPressed() {
        countEvents()
        super.onBackPressed()
    }
}