package com.example.firebasertdb.activities.PetSitter.activities.galerie

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.Pets.AddPetActivity
import com.example.firebasertdb.activities.PetSitter.activities.galerie.adapters.AdapterGaleriePozePetSitter
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class GalerieActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserGalerie: DatabaseReference
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var sharedManager : SharedStorageManager
    private lateinit var prefType:String

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAddPoza: FloatingActionButton
    private lateinit var adapter: AdapterGaleriePozePetSitter

    private lateinit var datalist: MutableList<String>
    private lateinit var dataClass: String
    private lateinit var numeImagineDeTrimis:String
    private lateinit var galerieID:MutableList<String>

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@GalerieActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_galerie)

        galerieID= mutableListOf()
        datalist = mutableListOf()

        initUI()
        initToolbar(toolbar)
        initRTDB()
        initRecycler()
        fetchFromFirebase()

        buttonAddPoza.setOnClickListener {
            var requestCode = 1
            var intent = Intent(this, AddPhotoToGalleryActivity::class.java)
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
        currentUserGalerie = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Galerie")
    }

    private fun fetchFromFirebase() {
        datalist.clear()
        galerieID.clear()
        var index = 0
        currentUserGalerie.get().addOnSuccessListener {
            for(poza in it.children) {
                numeImagineDeTrimis=poza.key.toString()
                galerieID.add(numeImagineDeTrimis)
                Log.i(
                    "TitluID",
                    "Got: $numeImagineDeTrimis"
                )

                val uriImagineCurr = poza.child("uriImagine").value.toString()

                Log.i(
                    "ValoriRedate",
                    "Got: ${poza.child("uriImagine").value.toString()}| $uriImagineCurr"
                )

                dataClass = uriImagineCurr
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
            title = "Galerie"
        }
    }

    private fun initRecycler() {
        adapter = AdapterGaleriePozePetSitter(datalist)
        adapter.setOnItemClickListener { position ->
            showDialogForAction(position)
        }
        recyclerView.layoutManager = GridLayoutManager(this,3)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

    }

    private fun initUI() {
        recyclerView = findViewById(R.id.recycler_view_galerie)
        buttonAddPoza = findViewById(R.id.rv_list_button_add_galerie)
        toolbar = findViewById(R.id.galerie_toolbar)
    }

    private fun showDialogForAction(position: Int) {
        val dialog = Dialog(this@GalerieActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_poza_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnEdit: Button = dialog.findViewById(R.id.customDialogEditButton)
        val btnDelete: Button = dialog.findViewById(R.id.customDialogDeleteButton)

        Log.d("PositionInDialog",position.toString())

        btnEdit.setOnClickListener {
            var requestCode = 1
            var toEditIntent = Intent(this, PozaFullScreenActivity::class.java)
            toEditIntent.putExtra(Constants.poza_aleasa,datalist[position])
            startActivityForResult(toEditIntent,requestCode)
            //Toast.makeText(this@GalerieActivity, "Pressed EDIT", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            deleteImageFromGallery(position)
            //Toast.makeText(this@GalerieActivity, "Pressed DELETE", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun retrieveDataFromListField(position: Int): String {
        Log.d("PositionRetrieveData","Position: $position")
        if (position >= 0 && position < datalist.size) {
            return datalist[position]
        } else {
           // Toast.makeText(this," $position + ${datalist}",Toast.LENGTH_LONG).show()
            return "Am iesit din index"
        }
    }

    private fun deleteImageFromGallery(position: Int) {
        Log.d("PositionDeletePet","Position: $position")
        if (position >= 0 && position < datalist.size ) {

            val childName = galerieID[position]

            currentUserGalerie.child(childName).removeValue()
                .addOnSuccessListener {

                    datalist.removeAt(position)
                    galerieID.removeAt(position)
                    adapter.notifyItemRemoved(position)

//                    Toast.makeText(
//                        this@GalerieActivity,
//                        "Deleted poza from database",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
                .addOnFailureListener {
//                    Toast.makeText(
//                        this@GalerieActivity,
//                        "Could not delete data from database",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
        } else {
//            Toast.makeText(
//                this@GalerieActivity,
//                "Could not delete, index out of bounds",
//                Toast.LENGTH_LONG
//            ).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                fetchFromFirebase()
            }
            if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this@GalerieActivity, "Bad REsult Code", Toast.LENGTH_LONG).show()
            }
        }
    }

}