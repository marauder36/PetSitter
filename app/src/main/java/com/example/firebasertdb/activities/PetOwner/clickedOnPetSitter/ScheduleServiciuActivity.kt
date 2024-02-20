package com.example.firebasertdb.activities.PetOwner.clickedOnPetSitter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.Pets.adapters.AdapterPet
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.PetClass
import com.example.firebasertdb.models.ServiceRequestClass
import com.example.firebasertdb.models.ServiceRequestClassPrice
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ScheduleServiciuActivity : AppCompatActivity(),DateSelectionListener {
    private val REQUEST_CODE = 4269

    private lateinit var database: FirebaseDatabase
    private lateinit var petOwnerReference:      DatabaseReference
    private lateinit var petSitterReference:     DatabaseReference
    private lateinit var mAuth:                  FirebaseAuth
    private lateinit var petSitterData:          DatabaseReference
    private lateinit var currentUserPetOwner:    DatabaseReference

//    private lateinit var datePickerBTN:          Button
//    private lateinit var hourPickerBTN:          Button
    private lateinit var selectPetButton:        Button
    private lateinit var rezervaServiciuButton:  Button
    private lateinit var showDate:               TextInputEditText
    private lateinit var showHour:               TextInputEditText
    private lateinit var serviciuTitluLayout:    TextView
    private lateinit var serviciuPretLayout:     TextView
    private lateinit var serviciuDescriereLayout:TextView
    private lateinit var toolbar:                Toolbar

    private lateinit var serviciuTitlu:          String
    private lateinit var serviciuPret:           String
    private lateinit var serviciuDescriere:      String
    private lateinit var recyclerViewPetSelectat:RecyclerView
    private lateinit var adapterPetSelectat:     AdapterPet
    private lateinit var dataList: MutableList<PetClass>

    private lateinit var numePetDeTrimis:        String
    private lateinit var numePetActual:          String
    private lateinit var rasaPetActual:          String

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@ScheduleServiciuActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_schedule_serviciu)
        
        dataList = mutableListOf()
        numePetDeTrimis="PetNamePlaceholder"
        numePetActual="PetNamePlaceholder"
        rasaPetActual="PetRacePlaceholder"
        serviciuTitlu=intent.getStringExtra(Constants.serviciu_ales_nume)!!
        serviciuPret=intent.getStringExtra(Constants.serviciu_ales_pret)!!
        serviciuDescriere=intent.getStringExtra(Constants.serviciu_ales_descriere)!!

        initUI()
        initToolbar(toolbar)
        initRTDB()
        setDataToViews()

        showDate.setOnClickListener {
            showCustomDatePickerDialog(this)
        }

        showHour.setOnClickListener {
            showCustomHourPickerDialog()
        }

        selectPetButton.setOnClickListener {
            val toPetSelectActivity = Intent(this,PetSelectActivity::class.java)
            startActivityForResult(toPetSelectActivity,REQUEST_CODE)
        }
        var requestingUserId : String
        var requestingUserName:String

        rezervaServiciuButton.setOnClickListener {
            if(showDate.text.toString()!="Alege data"&&showDate.text.toString()!="Alege o data pentru a contiua"
                && showHour.text.toString()!="Alege ora"&& showHour.text.toString()!="Alege o ora pentru a contiua"
                && numePetDeTrimis!="PetNamePlaceholder"){
                 currentUserPetOwner.get().addOnSuccessListener {currentUser->

                     requestingUserId= currentUser.key.toString()
                     requestingUserName=currentUser.child("prenume").value.toString()+" "+currentUser.child("nume").value.toString()

                     createServiceRequest(requestingUserId,requestingUserName,serviciuTitlu,showDate.text.toString(),showHour.text.toString(),numePetDeTrimis,numePetActual,rasaPetActual,serviciuPret)
                 }
                //Toast.makeText(this,"Picked $showDate",Toast.LENGTH_SHORT).show()
            }
            else if(showDate.text.toString()=="Alege data"){
                showDate.setText("Alege o data pentru a contiua")
            }
            else if(showHour.text.toString()=="Alege ora"){
                showHour.setText("Alege o ora pentru a contiua")
            }
            else if(numePetDeTrimis=="PetNamePlaceholder"){
                selectPetButton.setText("Alege un pet pentru a contiua")
            }
        }
    }

    private fun showCustomHourPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hour_picker, null)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker=dialogView.findViewById<NumberPicker>(R.id.minutePicker)
        // Set minimum and maximum values for the hour picker (0-23 for hours)
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue=0
        minutePicker.maxValue=59
        // Set a default value or current hour
        hourPicker.value = getCurrentHour()
        minutePicker.value=getCurrentMinute()
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Select Hour")
            setView(dialogView)
            setPositiveButton("OK") { dialog, _ ->
                val selectedHour = hourPicker.value.toString()+":"+minutePicker.value.toString()
                showHour.setText(selectedHour)
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun getCurrentHour(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY)
    }
    private fun getCurrentMinute(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MINUTE)
    }

    private fun initUI(){
        toolbar=findViewById(R.id.rezerva_main_toolbar)
        rezervaServiciuButton=findViewById(R.id.rezervareBtn)
//        datePickerBTN=findViewById(R.id.select_date_button_schedule)
//        hourPickerBTN=findViewById(R.id.select_hour_button_schedule)
        showDate=findViewById(R.id.ti_edit_text_date)
        showDate.isFocusable=false
        showDate.isClickable=true
        showDate.isCursorVisible=false
        showDate.keyListener=null

        showHour=findViewById(R.id.ti_edit_text_hour)
        showHour.isFocusable=false
        showHour.isClickable=false
        showHour.isCursorVisible=false
        showHour.keyListener=null

        selectPetButton=findViewById(R.id.select_pet_button)
        recyclerViewPetSelectat=findViewById(R.id.select_pet_recycler_view)
        serviciuTitluLayout=findViewById(R.id.titlu_serviciu)
        serviciuPretLayout=findViewById(R.id.pret_serviciu)
        serviciuDescriereLayout=findViewById(R.id.descriere_serviciu)
    }

    private fun initToolbar(toolbar:Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Rezerva un serviciu"
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)

        petOwnerReference=database.getReference("Petowner")
        currentUserPetOwner=petOwnerReference.child(mAuth.currentUser?.uid.toString())

        petSitterReference=database.getReference("PetSitter")
        petSitterData = petSitterReference.child(intent.getStringExtra(Constants.serviciu_ales_proprietar)!!)
    }

    private fun setDataToViews(){
        serviciuTitluLayout.text=serviciuTitlu
        serviciuPretLayout.text="Pret: $serviciuPret RON"
        serviciuDescriereLayout.text=serviciuDescriere
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

    override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {

        val selectedDateText = "$dayOfMonth-${month + 1}-$year" // Month is zero-based

        showDate.setText("$selectedDateText")
        Log.i("DataAleasaDeUser", selectedDateText)
    }

    private fun createServiceRequest(requestingUserId:String,requestingUserName:String,serviceRequested:String,
                                     date:String,hour:String, petID:String, numePetActual:String, rasaPetActual:String,pretServiciuCerut:String){
        petSitterData.child("Requests").child("$requestingUserName a rezervat $serviceRequested pe data de ${showDate.text} la ora: $hour pentru pet-ul: $petID")
            .setValue(ServiceRequestClassPrice(requestingUserId,requestingUserName,serviceRequested,"Pending",date,hour,petID,numePetActual,rasaPetActual,pretServiciuCerut))
            .addOnSuccessListener {
                Log.i("RequestStatus","Request Created Successfully: $requestingUserId, $requestingUserName, $serviceRequested, Pending")
                finish()
            }
            .addOnFailureListener {
                //Toast.makeText(this,"Rezervation failed $it",Toast.LENGTH_LONG).show()
                Log.i("RequestStatus","Request Creation FAILED")
            }
    }

    override fun onBackPressed() {
        Log.i("DateLaBack","showDate.text: ${showDate.text.toString()}")
        Log.i("DateLaBack","showDate.text: ${showHour.text.toString()}")
        Log.i("DateLaBack","showDate.text: ${numePetDeTrimis}")

        if((showDate.text.toString()!="Alege data"&&showDate.text.toString()!="Te rugam alege o data")
            ||( showHour.text.toString()!="Alege ora"&& showHour.text.toString()!="Te rugam alege o ora")
            ||numePetDeTrimis!="PetNamePlaceholder") {
            AlertDialog.Builder(this)
                .setTitle("Caution !")
                .setMessage("Do you want to discard changes made ?")
                .setPositiveButton("Discard") { dialog, _ ->
                    dialog.dismiss()
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setOnCancelListener { dialog ->
                    dialog.dismiss()
                }.create().show()
        }
        else{
            super.onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==REQUEST_CODE){
            if (resultCode== RESULT_OK){

                    initRecycler(data?.getStringExtra(Constants.pet_ales_pet_owner)!!)

            }
        }

    }
    private fun initRecycler(idPet:String){
        currentUserPetOwner.child("Pets").get().addOnSuccessListener {snapshot->
            
            dataList.clear()
            for (pet in snapshot.children){
                if (pet.key.toString()==idPet){
                    numePetDeTrimis=pet.key.toString()
                    numePetActual=pet.child("numePet").value.toString()
                    rasaPetActual=pet.child("rasaPet").value.toString()
                    val numePet = pet.child("numePet").value.toString()
                    val rasaPet = pet.child("rasaPet").value.toString()
                    val istoricMedicalScrisPet = pet.child("istoricMedicalScrisPet").value.toString()
                    val necesitatiPet = pet.child("necesitatiPet").value.toString()
                    val imaginePet = pet.child("imaginePet").value.toString()


                    Log.i(
                        "ValoriRedate",
                        "Got: $numePetDeTrimis| $numePet"
                    )
                    Log.i(
                        "ValoriRedate",
                        "Got: ${pet.child("numePet").value.toString()}| $numePet"
                    )
                    Log.i(
                        "ValoriRedate",
                        "Got: ${pet.child("rasaPet").value.toString()}| $rasaPet"
                    )
                    Log.i(
                        "ValoriRedate",
                        "Got: ${pet.child("istoricMedicalScrisPet").value.toString()}| $istoricMedicalScrisPet"
                    )
                    Log.i(
                        "ValoriRedate",
                        "Got: ${pet.child("necesitatiPet").value.toString()}| $necesitatiPet"
                    )
                    Log.i(
                        "ValoriRedate",
                        "Got: ${pet.child("imaginePet").value.toString()}| $imaginePet"
                    )

                    
                    dataList.add(PetClass(imaginePet,numePet,rasaPet,istoricMedicalScrisPet,necesitatiPet))
                    adapterPetSelectat = AdapterPet(dataList)
//                    adapterPetSelectat.setOnItemClickListener { position ->
//                        showDialogForAction(position)
//                    }
                    recyclerViewPetSelectat.layoutManager = LinearLayoutManager(this)
                    recyclerViewPetSelectat.setHasFixedSize(false)
                    recyclerViewPetSelectat.adapter = adapterPetSelectat

                }

                }
            }

        }

    }

interface DateSelectionListener {
    fun onDateSelected(year: Int, month: Int, dayOfMonth: Int)
}