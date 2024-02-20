package com.example.firebasertdb.activities.PetSitter.activities.calendar

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
import com.example.firebasertdb.models.activitateZi
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddActivitateActivity : AppCompatActivity() {
    private lateinit var titluActivitate:TextView
    private lateinit var numeActivitate: TextInputEditText
    private lateinit var descriereActivitate: TextInputEditText
    private lateinit var oraActivitate: TextInputEditText
    private lateinit var salveazaActivitateBTN: Button

    private lateinit var dataZi:String
    private lateinit var databaseCurrentDailyActivities:DatabaseReference

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
            startActivity(Intent(this@AddActivitateActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_add_activitate)

        initUI()
        initRTDB()
        initToolbar(toolbar)

        titluActivitate.text="Adauga o activitate pe data de $dataZi"
        salveazaActivitateBTN.setOnClickListener {
            salveazaActivitateRTDB()
        }
    }
    private fun initUI(){
        if(intent.getStringExtra(Constants.ZI_ALEASA)!=null) {
            dataZi = intent.getStringExtra(Constants.ZI_ALEASA)!!.toString()
        }else{
            dataZi = "DD/MM/YY"
        }

        toolbar=findViewById(R.id.add_activitate_toolbar)
        numeActivitate=findViewById(R.id.ti_edit_text_nume_activitate_add)
        descriereActivitate=findViewById(R.id.ti_edit_text_descriere_activitate_add)
        oraActivitate=findViewById(R.id.ti_edit_text_ora_activitate_add)
        salveazaActivitateBTN=findViewById(R.id.activitate_button_add)
        titluActivitate=findViewById(R.id.add_activitate_title)
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        //val prefType = sharedManager.getString("UserType")
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType.toString())
        databaseCurrentDailyActivities=databaseReference.child(mAuth.currentUser!!.uid).child("Evenimente")
    }

    private fun salveazaActivitateRTDB(){
        databaseCurrentDailyActivities.addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val numeActivitateRTDB = snapshot.child(numeActivitate.text.toString()+" "+dataZi).child("numeEvent").value.toString()
                val dataActivitateRTDB = snapshot.child(numeActivitate.text.toString()+" "+dataZi).child("dataEvent").value.toString()
                Log.i("NumeRTDB", "Nume: ${numeActivitateRTDB} vs ${numeActivitate.text.toString()}")
                Log.i("DataRTDB", "Data: ${dataActivitateRTDB} vs dataZI: $dataZi")
                if (numeActivitate.text!!.isEmpty()) {
                    numeActivitate.setError("Numele activitatii nu poate fi gol !")
                    numeActivitate.requestFocus()
                }else if (oraActivitate.text!!.isEmpty()) {
                    oraActivitate.setError("Ora activitatii nu poate fi goala !")
                    oraActivitate.requestFocus()
                }else if (descriereActivitate.text!!.isEmpty()) {
                    descriereActivitate.setError("Descrierea activitatii nu poate fi goala !")
                    descriereActivitate.requestFocus()
                } else if (numeActivitate.text.toString() == numeActivitateRTDB && dataZi==dataActivitateRTDB) {
                    numeActivitate.setError("Activitatea deja exista !")
                    numeActivitate.requestFocus()
                } else if (numeActivitate.text.toString() != numeActivitateRTDB) {
                    databaseCurrentDailyActivities.child(numeActivitate.text.toString()+" pe "+dataZi)
                        .setValue(activitateZi(dataZi, numeActivitate.text.toString(), descriereActivitate.text.toString(),oraActivitate.text.toString()))
                    var intent = Intent()
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
               // Toast.makeText(this@AddActivitateActivity, "Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
        Log.i("NumeActivitateCurenta","${numeActivitate.text.toString()}")
    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Adauga o activitate pe $dataZi"
        }
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

    private fun didDataChange():Boolean{
        var didDataChange = true

        didDataChange= (numeActivitate.text!!.isNotEmpty()
                || oraActivitate.text!!.isNotEmpty()
                || descriereActivitate.text!!.isNotEmpty())

        Log.i("didDataChange","$didDataChange")
        return didDataChange
    }
}