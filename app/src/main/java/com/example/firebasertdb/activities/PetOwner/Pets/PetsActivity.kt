package com.example.firebasertdb.activities.PetOwner.Pets

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.Pets.adapters.AdapterPet
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.PetClass
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class PetsActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserPets: DatabaseReference
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var sharedManager : SharedStorageManager
    private lateinit var prefType:String

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterPet
    private lateinit var buttonAddPet: FloatingActionButton


    private lateinit var datalist: MutableList<PetClass>
    private lateinit var dataClass: PetClass
    private lateinit var numePetDeTrimis:String
    private lateinit var petsID:MutableList<String>

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@PetsActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_pets)

        petsID= mutableListOf()
        datalist = mutableListOf()

        initUI()
        initToolbar(toolbar)
        initRTDB()
        initRecycler()
        fetchFromFirebase()

        buttonAddPet.setOnClickListener {
            var requestCode = 1
            var intent = Intent(this, AddPetActivity::class.java)
            startActivityForResult(intent,requestCode)
        }
    }

    private fun initRTDB(){
        sharedManager = SharedStorageManager(this)
        mAuth = FirebaseAuth.getInstance()
        //val prefType = sharedManager.getString("UserType")
        prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")!!
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType.toString())
        currentUserPets = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Pets")
    }

    private fun fetchFromFirebase() {
        datalist.clear()
        petsID.clear()
        var index = 0
        currentUserPets.get().addOnSuccessListener {
            for(pet in it.children) {
                numePetDeTrimis=pet.key.toString()
                petsID.add(numePetDeTrimis)
                Log.i(
                    "TitluID",
                    "Got: $numePetDeTrimis"
                )
                val numePet = pet.child("numePet").value.toString()
                val rasaPet = pet.child("rasaPet").value.toString()
                val istoricMedicalScrisPet = pet.child("istoricMedicalScrisPet").value.toString()
                val necesitatiPet = pet.child("necesitatiPet").value.toString()
                val imaginePet = pet.child("imaginePet").value.toString()

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

                dataClass = PetClass(imaginePet,numePet,rasaPet,istoricMedicalScrisPet,necesitatiPet)
                datalist.add(dataClass)
                adapter.notifyItemInserted(index)
                index++
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Pets"
        }
    }

    private fun initRecycler() {
        adapter = AdapterPet(datalist)
        adapter.setOnItemClickListener { position ->
            showDialogForAction(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter

    }

    private fun initUI() {
        recyclerView = findViewById(R.id.recycler_view_pets)
        buttonAddPet = findViewById(R.id.rv_list_button_add_pets)
        toolbar = findViewById(R.id.pets_toolbar)
    }

    private fun showDialogForAction(position: Int) {
        val dialog = Dialog(this@PetsActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_pet_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnEdit: Button = dialog.findViewById(R.id.customDialogEditButton)
        val btnDelete: Button = dialog.findViewById(R.id.customDialogDeleteButton)

        Log.d("PositionInDialog",position.toString())

        cdTitle.text = "Ati ales pet: ${retrieveDataFromListField(position)}"

        btnEdit.setOnClickListener {
            var requestCode = 1
            var toEditIntent = Intent(this, EditPetActivity::class.java)
            toEditIntent.putExtra(Constants.pet_ales,petsID[position])
            startActivityForResult(toEditIntent,requestCode)
            //Toast.makeText(this@PetsActivity, "Pressed EDIT", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            deletePet(position)
            //Toast.makeText(this@PetsActivity, "Pressed DELETE", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun retrieveDataFromListField(position: Int): String {
        Log.d("PositionRetrieveData","Position: $position")
        if (position >= 0 && position < datalist.size) {
            return datalist[position].numePet
        } else {
            //Toast.makeText(this," $position + ${datalist}",Toast.LENGTH_LONG).show()
            return "Am iesit din index"
        }
    }

    private fun deletePet(position: Int) {
        Log.d("PositionDeletePet","Position: $position")
        if (position >= 0 && position < datalist.size ) {

            val childName = petsID[position]

            currentUserPets.child(childName).removeValue()
                .addOnSuccessListener {

                    datalist.removeAt(position)
                    petsID.removeAt(position)
                    adapter.notifyItemRemoved(position)

                    //Toast.makeText(this@PetsActivity, "Deleted pet from database", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    //Toast.makeText(this@PetsActivity, "Could not delete data from database", Toast.LENGTH_LONG).show()
                }
        } else {
            //Toast.makeText(this@PetsActivity, "Could not delete, index out of bounds", Toast.LENGTH_LONG).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                fetchFromFirebase()
            }
            if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this@PetsActivity, "Bad REsult Code", Toast.LENGTH_LONG).show()
            }
        }
    }
}