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
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetSitter.activities.calendar.adapters.AdapterCalendar
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.ServiceRequestClassEvent
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendar:MaterialCalendarView
    private lateinit var selectedDate:String

    private lateinit var upcomingEvents:RecyclerView
    private lateinit var avemSauNuTV:TextView
    private lateinit var adapter: AdapterCalendar
    private lateinit var dataList: MutableList<ServiceRequestClassEvent>
    private lateinit var activitatiID:MutableList<String>
    private lateinit var requestDates:MutableList<String>

    private lateinit var myMap:MutableMap<String,ServiceRequestClassEvent>
    private lateinit var databaseCurrentDailyActivities:DatabaseReference

//    private lateinit var addEventBTN:FloatingActionButton

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var sharedManager : SharedStorageManager

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@CalendarActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_calendar)

        myMap= mutableMapOf()
        dataList= mutableListOf()
        activitatiID= mutableListOf()
        requestDates= mutableListOf()

        initUI()
        initRTDB()
        initToolbar(toolbar)
        getUpcomingEvents()
        initRecyclerView(dataList)
        getEvents()

        
        calendar.setOnDateChangedListener { widget, date, selected ->


            val dayOfMonth=  date.day
            val month =      date.month
            val year  =      date.year

            selectedDate="$dayOfMonth-${month}-$year"
            val selectedDayIntent=Intent(this@CalendarActivity,SelectedDayActivitiesActivity::class.java)
            selectedDayIntent.putExtra(Constants.ZI_ALEASA,selectedDate)
            startActivity(selectedDayIntent)
            calendar.clearSelection()
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
        calendar.clearSelection()

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

        calendar.addDecorator(EventDecorator(this,datesWithEvents))
    }

    class EventDecorator(private val context: Context,private val datesWithEvents:MutableList<CalendarDay>):DayViewDecorator{
        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return datesWithEvents.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setSelectionDrawable(context.resources.getDrawable(R.drawable.event_indicator_calendar))
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
                //Toast.makeText(this@CalendarActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onBackPressed() {
        countEvents()
        super.onBackPressed()
    }

    private fun checkIfDateIsWithinDay(requestDateString: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val requestDate = dateFormat.parse(requestDateString)

        val todayDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = todayDate
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val aDayFromNow = calendar.time
        if (requestDate!=null)
            return requestDate.before(aDayFromNow)
        else
            return false
    }

    private fun checkIfDateIsWithinWeek(requestDateString: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val requestDate = dateFormat.parse(requestDateString)

        val todayDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = todayDate
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val aWeekFromNow = calendar.time
        if (requestDate!=null)
            return requestDate.before(aWeekFromNow)
        else
            return false
    }

    private fun checkIfDateIsWithinMonth(requestDateString: String): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val requestDate = dateFormat.parse(requestDateString)

        val todayDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = todayDate
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val aWeekFromNow = calendar.time
        if (requestDate!=null)
            return requestDate.before(aWeekFromNow)
        else
            return false
    }

    private fun getUpcomingEvents(){
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        databaseReference.child(mAuth.currentUser?.uid.toString()).child("Requests")
            .addValueEventListener(object :ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    activitatiID.clear()
                    myMap= mutableMapOf()
                    for (request in snapshot.children)
                    {
                        val requestDateString = request.child("date").value.toString()
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

                        if(checkIfDateIsWithinDay(requestDateString)&&status=="Accepted"){

                            activitatiID.add(request.key.toString())
                            val currRequest=ServiceRequestClassEvent(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,descriereExtra)
                            myMap[request.key.toString()]=currRequest

                            dataList.add(ServiceRequestClassEvent(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,descriereExtra))
                            dataList.sortBy {
                                dateFormat.parse(it.date)
                            }
                            adapter.setData(dataList)
                            adapter.notifyDataSetChanged()
                        }
                        else if(checkIfDateIsWithinWeek(requestDateString)&&status=="Accepted")
                        {

                            activitatiID.add(request.key.toString())
                            val currRequest=ServiceRequestClassEvent(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,descriereExtra)
                            myMap[request.key.toString()]=currRequest

                            dataList.add(ServiceRequestClassEvent(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,descriereExtra))
                            dataList.sortBy {
                                dateFormat.parse(it.date)
                            }
                            adapter.setData(dataList)
                            adapter.notifyDataSetChanged()
                        }
                        else if(checkIfDateIsWithinMonth(requestDateString)&&status=="Accepted")
                        {
                            activitatiID.add(request.key.toString())
                            val currRequest=ServiceRequestClassEvent(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,descriereExtra)
                            myMap[request.key.toString()]=currRequest

                            dataList.add(ServiceRequestClassEvent(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,descriereExtra))
                            dataList.sortBy {
                                dateFormat.parse(it.date)
                            }
                            adapter.setData(dataList)
                            adapter.notifyDataSetChanged()
                        }

                    }
                    if(dataList.isEmpty()){
                        avemSauNuTV.text="You have no upcoming events."
                    }
                    Log.i("MappedValues","$myMap")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("ErrorGettingData","$error")
                }
            })

    }

    private fun initRecyclerView(dataList: MutableList<ServiceRequestClassEvent>){
        adapter = AdapterCalendar(dataList)
        adapter.setOnItemClickListener { position ->
            val request = adapter.getClickedItem(position)
            if(request!=null)
                //Toast.makeText(this,"Clicked on $position",Toast.LENGTH_SHORT).show()
                showDialogForAction(position,request!!)
        }
        upcomingEvents.layoutManager = LinearLayoutManager(this)
        upcomingEvents.setHasFixedSize(false)
        upcomingEvents.adapter = adapter
    }

    private fun showDialogForAction(position: Int,request:ServiceRequestClassEvent) {
        val dialog = Dialog(this@CalendarActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_activitate_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnEdit: Button = dialog.findViewById(R.id.customDialogEditButton)
        val btnDelete: Button = dialog.findViewById(R.id.customDialogDeleteButton)

//        myMap.entries.sortedBy { it.value.date }
        
        val selectedRequestKey=myMap.filterValues { it==request }.keys.firstOrNull()

        Log.d("PositionInDialog","$selectedRequestKey")

        val titluComplet = selectedRequestKey

        val numeUserRezervare=selectedRequestKey?.substringBefore(" a")
        Log.i("NumePentruTitlu","Got: $numeUserRezervare")

        val dataRezervareCuNumePet=selectedRequestKey?.substringAfter("pe data de ")
        Log.i("NumePentruTitlu","Got: $dataRezervareCuNumePet")

        val dataRezervare =dataRezervareCuNumePet?.substringBefore(" pentru")
        Log.i("NumePentruTitlu","Got: $dataRezervare")

        cdTitle.text = "$numeUserRezervare a creat o rezervare pe data de $dataRezervare"

        btnEdit.setOnClickListener {
            var requestCode = 1
            var toEditIntent = Intent(this, EditActivitateActivity::class.java)
            Log.i("PozitieIntent","Got: $position")
            Log.i("PozitieIntent","Got: ${activitatiID[position]}")
            toEditIntent.putExtra(Constants.nume_activitate_zi_aleasa_plus_data,selectedRequestKey)
            Log.d("PositionInDialog","$selectedRequestKey")
            toEditIntent.putExtra(Constants.nume_petowner_activitate_zi_aleasa,request.requestingUserID)
            startActivityForResult(toEditIntent,requestCode)
            //Toast.makeText(this@ServiciiActivity, "Pressed EDIT", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            cancelActivitate(selectedRequestKey!!)
            //Toast.makeText(this@ServiciiActivity, "Pressed DELETE", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun cancelActivitate(childName:String) {
        
            databaseCurrentDailyActivities.child(childName).child("status").setValue("Canceled by PetSitter")
                .addOnSuccessListener {

                   // Toast.makeText(this@CalendarActivity, "Deleted activitate from database", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                   // Toast.makeText(this@CalendarActivity, "Could not delete data from database", Toast.LENGTH_LONG).show()
                }
        }
    

    private fun initUI(){
        calendar = findViewById(R.id.calendar_view_main)
        selectedDate="day/month/year"
//        addEventBTN=findViewById(R.id.add_event_in_calendar_button)
        toolbar=findViewById(R.id.calendar_main_toolbar)
        upcomingEvents=findViewById(R.id.upcoming_events_RV)
        avemSauNuTV=findViewById(R.id.avem_sau_nu_events)
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("PetSitter")
        databaseCurrentDailyActivities=databaseReference.child(mAuth.currentUser!!.uid).child("Requests")
    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Calendar rezervari"
        }
    }



}