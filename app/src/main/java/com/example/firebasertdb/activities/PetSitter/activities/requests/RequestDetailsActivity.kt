package com.example.firebasertdb.activities.PetSitter.activities.requests

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.Pets.adapters.AdapterPet
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.chatpart.MainChatActivity
import com.example.firebasertdb.models.ChatMessage
import com.example.firebasertdb.models.PetClass
import com.example.firebasertdb.models.ServiceRequestClass
import com.example.firebasertdb.models.ServiceRequestClassEvent
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RequestDetailsActivity : AppCompatActivity(){
    private val REQUEST_CODE = 4269

    private lateinit var database: FirebaseDatabase
    private lateinit var petOwnerReference:      DatabaseReference
    private lateinit var petSitterReference:     DatabaseReference
    private lateinit var mAuth:                  FirebaseAuth

    private lateinit var currentUserPetOwner:    DatabaseReference

    private lateinit var currentRequestID:String
    private lateinit var titluTV:TextView
    private lateinit var dialButton: TextView
    private lateinit var messageButton:TextView

    //    private lateinit var datePickerBTN:          Button
//    private lateinit var hourPickerBTN:          Button
    private lateinit var acceptaButton:        Button
    private lateinit var refuzaButton:  Button
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

    private lateinit var numePetActual:String
    private lateinit var numePetDeTrimis:String
    private lateinit var rasaPetActual:String

    private lateinit var currentRequestSender:String
    private lateinit var currentRequestService:String
    private lateinit var currentRequestPetID:String

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@RequestDetailsActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_request_details)

        getDataFromIntent()
        dataList = mutableListOf()


        initUI()
        initToolbar(toolbar)
        initRTDB()
        setDataToViews()
        initRecycler()

        refuzaButton.setOnClickListener {
            petSitterReference.child(mAuth.currentUser?.uid.toString()).child("Requests").child(currentRequestID).child("status").setValue("Declined")
            finish()
        }
        acceptaButton.setOnClickListener {
            petSitterReference.child(mAuth.currentUser?.uid.toString()).child("Requests").child(currentRequestID).child("status").setValue("Accepted")
            finish()
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

            val intent=Intent(this,MainChatActivity::class.java)
            intent.putExtra(Constants.chat_with_other_user_ID,currentRequestSender)
            startActivity(intent)

        }


    }



    private fun getDataFromIntent(){
        currentRequestSender=intent.getStringExtra(Constants.petSitterRequestSender)!!
        currentRequestService=intent.getStringExtra(Constants.petSitterRequestService)!!
        Log.i("DetaliiServiciu","$currentRequestService")
        currentRequestPetID=intent.getStringExtra(Constants.petSitterRequestPet)!!
        currentRequestID=intent.getStringExtra(Constants.petSitterRequestID)!!
    }

    private fun initUI(){
        toolbar=findViewById(R.id.rezerva_main_toolbar)

        titluTV=findViewById(R.id.title_sus)
        dialButton=findViewById(R.id.dial_button)
        messageButton=findViewById(R.id.send_message_TV)

        acceptaButton=findViewById(R.id.accept_button)
        refuzaButton=findViewById(R.id.decline_button)
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
            title = "Detalii rezervare"
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)

        petOwnerReference=database.getReference("Petowner")
        currentUserPetOwner=petOwnerReference.child(mAuth.currentUser?.uid.toString())

        petSitterReference=database.getReference("PetSitter")
    }

    private fun setDataToViews(){
        petSitterReference.child(mAuth.currentUser?.uid.toString()).child("Servicii")
            .addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (serviciu in snapshot.children){

                    Log.i("DetaliiServiciu","${serviciu.child("numeServiciu").value.toString()}")

                    if (serviciu.child("numeServiciu").value.toString()==currentRequestService){
                        serviciuTitlu    =serviciu.child("numeServiciu").value.toString()
                        serviciuPret     =serviciu.child("pretServiciu").value.toString()
                        serviciuDescriere=serviciu.child("descriereServiciu").value.toString()

                        Log.i("DetaliiServiciu","$serviciuTitlu $serviciuPret $serviciuDescriere")
                        serviciuTitluLayout.text=serviciuTitlu
                        serviciuPretLayout.text="Pret: $serviciuPret RON"
                        serviciuDescriereLayout.text=serviciuDescriere
                        Log.i("DetaliiServiciu","$serviciuTitlu $serviciuPret $serviciuDescriere")
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("Error getting data","$error")
            }

        })

        petSitterReference.child(mAuth.currentUser?.uid.toString()).child("Requests").child(currentRequestID).get()
            .addOnSuccessListener { snapshot->

                titluTV.text="${snapshot.child("requestingUserName").value.toString()} doreste sa rezerve serviciul:"
                showDate.setText(snapshot.child("date").value.toString())
                showHour.setText(snapshot.child("hour").value.toString())

            }

    }

    private fun initRecycler(){
        petOwnerReference.child(currentRequestSender).child("Pets").child(currentRequestPetID).get().addOnSuccessListener {pet->

            dataList.clear()
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
