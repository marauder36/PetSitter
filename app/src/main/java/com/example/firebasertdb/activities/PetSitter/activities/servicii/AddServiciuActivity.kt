package com.example.firebasertdb.activities.PetSitter.activities.servicii

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.ServiciiClass
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddServiciuActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var numeRTDB: Any? = null

    private lateinit var numeServiciu: TextInputEditText
    private lateinit var pretServiciu: TextInputEditText
    private lateinit var descriereServiciu: TextInputEditText
    private lateinit var addServiciuBTN: Button

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@AddServiciuActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_add_serviciu)

        initUI()

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.add_serviciu_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Adauga un nou serviciu"
        }

        mAuth = FirebaseAuth.getInstance()
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE)
            .getString("UserType", "Petowner or PetSitter")
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType.toString())

        val currentUserServicii =
            databaseReference.child(mAuth.currentUser?.uid.toString()).child("Servicii")


        addServiciuBTN.setOnClickListener {

            currentUserServicii.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    numeRTDB = snapshot.child(numeServiciu.text.toString())
                        .child("numeServiciu").value.toString()
                    Log.i("NumeServiciuRTDB", "Nume: ${numeRTDB}")
                    if (numeServiciu.text!!.isEmpty()) {
                        numeServiciu.setError("Numele serviciului nu poate fi gol !")
                        numeServiciu.requestFocus()
                    } else if (pretServiciu.text!!.isEmpty()) {
                        pretServiciu.setError("Pretul serviciului nu poate fi gol !")
                        pretServiciu.requestFocus()
                    } else if (descriereServiciu.text!!.isEmpty()) {
                        descriereServiciu.setError("Descrierea serviciului nu poate fi goala !")
                        descriereServiciu.requestFocus()
                    } else if (numeServiciu.text.toString() == numeRTDB) {
                        numeServiciu.setError("Serviciul deja exista !")
                        numeServiciu.requestFocus()
                    } else if (numeServiciu.text.toString() != numeRTDB) {
                        currentUserServicii.child(numeServiciu.text.toString())
                            .setValue(ServiciiClass(numeServiciu.text.toString(), pretServiciu.text.toString(), descriereServiciu.text.toString()))
                        var intent = Intent()
                        setResult(Activity.RESULT_OK,intent)
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(
//                        this@AddServiciuActivity,
//                        "Couldn't retrieve data: ${error.toException()}",
//                        Toast.LENGTH_LONG
//                    ).show()
                    Log.w("FirebaseData", "Failed to read value.", error.toException())
                }
            })
            Log.i("NumeServiciuCurent","${numeServiciu.text.toString()} vs $numeRTDB")



        }

    }

    private fun didDataChange():Boolean{
        var didDataChange = true

        didDataChange= (numeServiciu.text!!.isNotEmpty()
                || pretServiciu.text!!.isNotEmpty()
                || descriereServiciu.text!!.isNotEmpty())

        Log.i("didDataChange","$didDataChange")
        return didDataChange
    }

    override fun onBackPressed() {

            if(didDataChange())
            {
                AlertDialog.Builder(this)
                    .setTitle("Caution !")
                    .setMessage("If you click BACK again, you will lose all unsaved changes ! Are you sure ?")
                    .setPositiveButton("Discard") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                        super.onBackPressed()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setOnCancelListener { dialog ->
                        dialog.dismiss()
                    }
                    .create().show()
            }
            else{
                super.onBackPressed()
            }
        }


    private fun initUI() {
        numeServiciu = findViewById(R.id.ti_edit_text_nume_serviciu_add)
        pretServiciu = findViewById(R.id.ti_edit_text_pret_serviciu_add)
        descriereServiciu = findViewById(R.id.ti_edit_text_descriere_serviciu_add)
        addServiciuBTN = findViewById(R.id.servicii_button_add)
    }
}
