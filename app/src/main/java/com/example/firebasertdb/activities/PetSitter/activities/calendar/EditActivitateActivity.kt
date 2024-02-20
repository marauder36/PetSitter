package com.example.firebasertdb.activities.PetSitter.activities.calendar

import android.app.Activity
import com.example.firebasertdb.activities.PetOwner.clickedOnPetSitter.PetSelectActivity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
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
import com.example.firebasertdb.chatpart.MainChatActivity
import com.example.firebasertdb.models.PetClass
import com.example.firebasertdb.models.ServiceRequestClass
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.integrity.internal.m
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class EditActivitateActivity: AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var petOwnerReference:      DatabaseReference
    private lateinit var petSitterReference:     DatabaseReference
    private lateinit var mAuth:                  FirebaseAuth

    private lateinit var dialButton: TextView
    private lateinit var messageButton:TextView
    private lateinit var currentRequestSender:String

    //    private lateinit var datePickerBTN:          Button
//    private lateinit var hourPickerBTN:          Button
    private lateinit var modificaServiciuButton:  Button
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
    private lateinit var petIDPrimit:String
    private lateinit var requestIDPrimit:String

    private lateinit var descriereExtra:TextInputEditText
    private lateinit var descriereExtraOrig: String
    private lateinit var titluMare:TextView

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@EditActivitateActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_edit_activitate)

        currentRequestSender=intent.getStringExtra(Constants.nume_petowner_activitate_zi_aleasa)!!
        Log.i("DataFromIntent","$currentRequestSender")
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
        getDateHourServiceID()
        setDataToServiciuViews()


        modificaServiciuButton.setOnClickListener {
            if(showDate.text.toString()!="Modifica data"
                && showHour.text.toString()!="Modifica ora"
                && numePetDeTrimis!="PetNamePlaceholder"){

                editServiceRequest(descriereExtra.text.toString())

            }
        }

        dialButton.setOnClickListener {

            petOwnerReference.child(currentRequestSender).child("phoneNumber").get()
                .addOnSuccessListener {phoneNumber->
                    val intent=Intent(Intent.ACTION_DIAL)
                    intent.data= Uri.parse("tel:${phoneNumber.value.toString()}")
                    startActivity(intent)
                }

        }

        messageButton.setOnClickListener {

            val intent=Intent(this, MainChatActivity::class.java)
            intent.putExtra(Constants.chat_with_other_user_ID,currentRequestSender)
            startActivity(intent)

        }
    }

    private fun getStringFromIntent(){
        requestIDPrimit=intent.getStringExtra(Constants.nume_activitate_zi_aleasa_plus_data)!!

        requestingUserNamePrimit=intent.getStringExtra(Constants.nume_petowner_activitate_zi_aleasa)!!

    }


    private fun initUI(){
        toolbar=findViewById(R.id.rezerva_main_toolbar)
        descriereExtra=findViewById(R.id.ti_edit_text_descriere_extra)
        titluMare=findViewById(R.id.titlu_mare_edit_event)
        modificaServiciuButton=findViewById(R.id.rezervareBtn)

        dialButton=findViewById(R.id.dial_button)
        messageButton=findViewById(R.id.send_message_TV)
//        datePickerBTN=findViewById(R.id.select_date_button_schedule)
//        hourPickerBTN=findViewById(R.id.select_hour_button_schedule)
        showDate=findViewById(R.id.ti_edit_text_date)
        showDate.isFocusable=false
        showDate.isClickable=false
        showDate.isCursorVisible=false
        showDate.keyListener=null

        showHour=findViewById(R.id.ti_edit_text_hour)
        showHour.isFocusable=false
        showHour.isClickable=false
        showHour.isCursorVisible=false
        showHour.keyListener=null

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
            title = "Eveniment"
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)

        petOwnerReference=database.getReference("Petowner")

        petSitterReference=database.getReference("PetSitter")
    }

    private fun getDateHourServiceID(){
        petSitterReference.child(mAuth.currentUser?.uid.toString()).child("Requests").child(requestIDPrimit).get()
            .addOnSuccessListener {snapshot->
                showDate.setText(snapshot.child("date").value.toString())
                showHour.setText(snapshot.child("hour").value.toString())
                serviciuID=snapshot.child("serviceRequested").value.toString()
                petIDPrimit=snapshot.child("petID").value.toString()
                if(snapshot.child("descriereExtra").value!=null)
                    descriereExtra.setText(snapshot.child("descriereExtra").value.toString())
                descriereExtraOrig=descriereExtra.text.toString()
                titluMare.text="${snapshot.child("requestingUserName").value.toString()} a rezervat serviciul:"
                initRecycler(petIDPrimit)
            }
    }

    private fun setDataToServiciuViews(){

        petSitterReference.child(mAuth.currentUser?.uid.toString()).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                    for(service in snapshot.child("Servicii").children)
                    {
                        if(service.child("numeServiciu").value.toString()==serviciuID){

                            serviciuTitlu    =service.child("numeServiciu").value.toString()
                            serviciuPret     =service.child("pretServiciu").value.toString()
                            serviciuDescriere=service.child("descriereServiciu").value.toString()

                            serviciuTitluLayout.text=serviciuTitlu
                            serviciuPretLayout.text="Pret: $serviciuPret RON"
                            serviciuDescriereLayout.text=serviciuDescriere

                        }
                    }
                }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@EditActivitateActivity,"Erorr: $error",Toast.LENGTH_LONG).show()
            }

        })


    }


    private fun editServiceRequest(descriereExtra:String){

        petSitterReference.get().addOnSuccessListener{  snapshot->
            for(user in snapshot.children)
            {
                for(request in user.child("Requests").children)
                {
                    Log.i("RequestKey","Got: ${request.key.toString()}")

                    if(request.key.toString()==requestIDPrimit)
                    {
                        petSitterReference.child(user.key.toString()).child("Requests").child(request.key.toString()).child("descriereExtra")
                            .setValue(descriereExtra)

                            .addOnSuccessListener {
                                Log.i("RequestStatus","Request Updated Successfully: $descriereExtra")
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
        Log.i("DescriereExtraOrig","Got: $descriereExtraOrig")
        if(descriereExtra.text.toString()!=descriereExtraOrig) {
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
            editServiceRequest(descriereExtraOrig)
            super.onBackPressed()
        }

    }

    private fun initRecycler(idPet:String){
        petOwnerReference.child(requestingUserNamePrimit).child("Pets").get().addOnSuccessListener {snapshot->

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