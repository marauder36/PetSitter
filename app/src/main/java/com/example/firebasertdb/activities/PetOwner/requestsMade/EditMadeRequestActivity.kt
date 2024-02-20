package com.example.firebasertdb.activities.PetOwner.requestsMade

import android.app.Activity
import com.example.firebasertdb.activities.PetOwner.clickedOnPetSitter.PetSelectActivity

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
import androidx.databinding.adapters.NumberPickerBindingAdapter.setValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.Pets.adapters.AdapterPet
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.PetClass
import com.example.firebasertdb.models.ServiceRequestClass
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.NonCancellable.children
import java.util.Calendar

class EditMadeRequestActivity : AppCompatActivity(),DateSelectionListener {
    private val REQUEST_CODE = 4269

    private lateinit var database: FirebaseDatabase
    private lateinit var petOwnerReference:      DatabaseReference
    private lateinit var petSitterReference:     DatabaseReference
    private lateinit var mAuth:                  FirebaseAuth
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

    private lateinit var serviciuID:             String
    private lateinit var serviciuTitlu:          String
    private lateinit var serviciuPret:           String
    private lateinit var serviciuDescriere:      String
    private lateinit var recyclerViewPetSelectat:RecyclerView
    private lateinit var adapterPetSelectat:     AdapterPet
    private lateinit var dataList: MutableList<PetClass>

    private lateinit var numePetDeTrimis:        String
    private lateinit var numePetActual:          String
    private lateinit var rasaPetActual:          String

    private lateinit var requestingUserNamePrimit:String
    private lateinit var serviceRequestedPrimit:String
    private lateinit var dataPrimita:String
    private lateinit var hourPrimita:String
    private lateinit var petIDPrimit:String
    private lateinit var requestIDPrimit:String

    private lateinit var dataNoua:String
    private lateinit var oraNoua:String
    private lateinit var petNou:String

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@EditMadeRequestActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_edit_made_request)

        dataList = mutableListOf()
        numePetDeTrimis="PetNamePlaceholder"
        numePetActual="PetNamePlaceholder"
        rasaPetActual="PetRacePlaceholder"

        getStringFromIntent()

        serviciuTitlu    ="Nume Serviciu"
        serviciuPret     ="Pret"
        serviciuDescriere="Descriere"

        initUI()
        initToolbar(toolbar)
        initRTDB()
        setDataToServiciuViews()
        initRecycler(petIDPrimit)

        showDate.setOnClickListener {
            showCustomDatePickerDialog(this)
        }

        showHour.setOnClickListener {
            showCustomHourPickerDialog()
        }

        selectPetButton.setOnClickListener {
            val toPetSelectActivity = Intent(this, PetSelectActivity::class.java)
            startActivityForResult(toPetSelectActivity,REQUEST_CODE)
        }
        var requestingUserId : String
        var requestingUserName:String

        rezervaServiciuButton.setOnClickListener {
            if(showDate.text.toString()!="Modifica data"
                && showHour.text.toString()!="Modifica ora"
                && numePetDeTrimis!="PetNamePlaceholder"){

                currentUserPetOwner.get().addOnSuccessListener {currentUser->

                    requestingUserId= currentUser.key.toString()
                    requestingUserName=currentUser.child("prenume").value.toString()+" "+currentUser.child("nume").value.toString()

                    editServiceRequest(requestingUserId,requestingUserName,serviciuTitlu,showDate.text.toString(),showHour.text.toString(),numePetDeTrimis,numePetActual,rasaPetActual)
                }
            }
        }
    }

    private fun getStringFromIntent(){
        requestIDPrimit=intent.getStringExtra(Constants.Selected_request_ID)!!
        serviciuID=intent.getStringExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_SERVICE_NAME)!!
        requestingUserNamePrimit=intent.getStringExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_PETOWNERID)!!
        serviceRequestedPrimit=intent.getStringExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_SERVICE_NAME)!!
        dataPrimita=intent.getStringExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_DATE)!!
        hourPrimita=intent.getStringExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_HOUR)!!
        petIDPrimit=intent.getStringExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_PET_NAME)!!
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
    }

    private fun setDataToServiciuViews(){

        showDate.setText(dataPrimita)
        showHour.setText(hourPrimita)

        petSitterReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(user in snapshot.children){
                    for(service in user.child("Servicii").children)
                    {
                        if(service.key.toString()==serviciuID){

                            serviciuTitlu    =service.child("numeServiciu").value.toString()
                            serviciuPret     =service.child("pretServiciu").value.toString()
                            serviciuDescriere=service.child("descriereServiciu").value.toString()

                            serviciuTitluLayout.text=serviciuTitlu
                            serviciuPretLayout.text="Pret: $serviciuPret RON"
                            serviciuDescriereLayout.text=serviciuDescriere

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@EditMadeRequestActivity,"Erorr: $error",Toast.LENGTH_LONG).show()
            }

        })


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

    private fun editServiceRequest(requestingUserId:String, requestingUserName:String, serviceRequested:String,
                                   date:String, hour:String, petID:String, numePetActual:String, rasaPetActual:String){

        petSitterReference.get().addOnSuccessListener{  snapshot->
            for(user in snapshot.children)
            {
                for(request in user.child("Requests").children)
                {
                    Log.i("RequestKey","Got: ${request.key.toString()}")

                    if(request.key.toString()==requestIDPrimit)
                    {
                        petSitterReference.child(user.key.toString()).child("Requests").child(request.key.toString())
                            .setValue(ServiceRequestClass(requestingUserId,requestingUserName,serviceRequested,
                                    "Pending",date,hour,petID,numePetActual,rasaPetActual))

                            .addOnSuccessListener {
                                Log.i("RequestStatus","Request Updated Successfully: $requestingUserId, $requestingUserName, $serviceRequested, Pending")
                                var intent = Intent()
                                setResult(Activity.RESULT_OK,intent)
                                finish() }

                            .addOnFailureListener {
                                //Toast.makeText(this,"Rezervation failed $it",Toast.LENGTH_LONG).show()
                                Log.i("RequestStatus","Request Creation FAILED")}
                    }
                    else{
                        Log.i("ErrorFindingPet","Couldn't find pet")
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if(showDate.text.toString()!=dataPrimita
            || showHour.text.toString()!=hourPrimita
            || numePetDeTrimis!=petIDPrimit) {
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