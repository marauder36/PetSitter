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
import android.widget.TextView
import android.widget.Toast
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EditServiciuActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserServicii: DatabaseReference

    private lateinit var editServiciuTitle:TextView
    private lateinit var editServiciuNume:TextInputEditText
    private lateinit var editServiciuPret:TextInputEditText
    private lateinit var editServiciuDescriere:TextInputEditText
    private lateinit var editServiciuSaveChangesBTN:Button

    private lateinit var numeServiciuRTDB:String
    private lateinit var pretServiciuRTDB:String
    private lateinit var descriereServiciuRTDB:String
    private lateinit var serviciuCurr:String

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@EditServiciuActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_edit_serviciu)

        initUI()
        initRTDB()
        setDataToUI()

        editServiciuSaveChangesBTN.setOnClickListener {
            checkEmptyEditsServiciu()
            saveChangesServiciu()
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.edit_serviciu_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Editeaza un serviciu"
        }
    }

    private fun initUI(){
        editServiciuSaveChangesBTN=findViewById(R.id.serviciu_button_edit)
        editServiciuTitle=findViewById(R.id.edit_serviciu_title)
        editServiciuNume=findViewById(R.id.ti_edit_text_nume_serviciu_edit)
        editServiciuPret=findViewById(R.id.ti_edit_text_pret_serviciu_edit)
        editServiciuDescriere=findViewById(R.id.ti_edit_text_descriere_serviciu_edit)
    }

    private fun setDataToUI(){
        serviciuCurr=intent.getStringExtra(Constants.SERVICIU_ALES).toString()
        editServiciuTitle.text=serviciuCurr
        currentUserServicii.child(serviciuCurr).get()
            .addOnSuccessListener {
                Log.i("EditServiciuData","Got: $it")

                numeServiciuRTDB=it.child("numeServiciu").value.toString()
                Log.i("EditServiciuNume","Got: $numeServiciuRTDB")
                editServiciuNume.setText(numeServiciuRTDB)

                pretServiciuRTDB=it.child("pretServiciu").value.toString()
                editServiciuPret.setText(pretServiciuRTDB)

                descriereServiciuRTDB=it.child("descriereServiciu").value.toString()
                editServiciuDescriere.setText(descriereServiciuRTDB)
            }
            .addOnFailureListener {
               // Toast.makeText(this@EditServiciuActivity,"Faile to load data from RTDB",Toast.LENGTH_LONG).show()
            }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        //val prefType = sharedManager.getString("UserType")
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType.toString())
        currentUserServicii = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Servicii")
    }

    private fun checkEmptyEditsServiciu(){
        if(editServiciuNume.text!!.isEmpty()){
            editServiciuNume.setError("Numele nou nu poate fi gol !")
            editServiciuNume.requestFocus()
        }
        else if(editServiciuPret.text!!.isEmpty()){
            editServiciuPret.setError("Pretul nou nu poate fi gol !")
            editServiciuPret.requestFocus()
        }
        else if(editServiciuDescriere.text!!.isEmpty()){
            editServiciuDescriere.setError("Noua descriere nu poate fi goala !")
            editServiciuDescriere.requestFocus()
        }
    }
    private fun saveChangesServiciu(){
        val refCurr=currentUserServicii.child(serviciuCurr)

        if(numeServiciuRTDB!=editServiciuNume.text.toString()){
            refCurr.child("numeServiciu").setValue(editServiciuNume.text.toString())
           // Toast.makeText(this@EditServiciuActivity,"Numele nu e la fel",Toast.LENGTH_SHORT).show()
        }
        if(pretServiciuRTDB!=editServiciuPret.text.toString()){
            refCurr.child("pretServiciu").setValue(editServiciuPret.text.toString())
            //Toast.makeText(this@EditServiciuActivity,"Pretul nu e la fel",Toast.LENGTH_SHORT).show()
        }
        if(descriereServiciuRTDB!=editServiciuDescriere.text.toString()){
            refCurr.child("descriereServiciu").setValue(editServiciuDescriere.text.toString())
            //Toast.makeText(this@EditServiciuActivity,"Descrierea nu e la fel",Toast.LENGTH_SHORT).show()
        }

        var intent = Intent()
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    private fun didDataChange(callback: (Boolean?)->Unit){
        var didDataChange = true
        val currentServiciu = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Servicii").child(serviciuCurr)
        currentServiciu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                didDataChange= (numeServiciuRTDB!=editServiciuNume.text.toString()||
                                pretServiciuRTDB!=editServiciuPret.text.toString()||
                                descriereServiciuRTDB!=editServiciuDescriere.text.toString())

                callback(didDataChange)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
                //Toast.makeText(this@EditServiciuActivity,"Couldn't retrieve data: ${error.toException()}",Toast.LENGTH_LONG).show()
            }})
    }

    override fun onBackPressed() {

        didDataChange{
            if(it==true)
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

    }

}