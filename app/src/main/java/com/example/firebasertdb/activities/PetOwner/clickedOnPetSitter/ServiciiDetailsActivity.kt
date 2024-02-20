package com.example.firebasertdb.activities.PetOwner.clickedOnPetSitter

import com.example.firebasertdb.activities.PetSitter.activities.servicii.EditServiciuActivity

import android.app.Dialog
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
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.activities.PetSitter.activities.servicii.adapters.AdapterServiciu
import com.example.firebasertdb.models.serviciuTest
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ServiciiDetailsActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserServicii: DatabaseReference
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var sharedManager : SharedStorageManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterServiciu

    private lateinit var datalist: MutableList<serviciuTest>
    private lateinit var dataClass: serviciuTest
    private lateinit var numeServiciuDeTrimis:String
    private lateinit var serviciiID:MutableList<String>
    private lateinit var uidPetSitter:String

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@ServiciiDetailsActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_servicii_details)

        uidPetSitter="User(unique email)"
        datalist = mutableListOf()
        serviciiID= mutableListOf()
        sharedManager = SharedStorageManager(this)
        initUI()
        initToolbar(toolbar)
        initRTDB()
        initRecycler()
        adapter.setOnItemClickListener { position ->
            val clickedItem = adapter.getClickedItem(position)
            clickedItem?.let {
                Log.d("Clicked Item", "Name: ${it.nume}, Pret: ${it.pret}, Desc: ${it.descriere}")

                showDialogForAction(position,it)
            }

        }
    }

    private fun fetchFromFirebase() {
        datalist.clear()
        serviciiID.clear()
        var index = 0
        currentUserServicii.get().addOnSuccessListener {
            for(serviciu in it.children) {
                numeServiciuDeTrimis=serviciu.key.toString()
                serviciiID.add(numeServiciuDeTrimis)
                Log.i(
                    "TitluID",
                    "Got: $numeServiciuDeTrimis"
                )
                val numeServiciu = serviciu.child("numeServiciu").value.toString()
                val pretServiciu = serviciu.child("pretServiciu").value.toString()
                val descriereServiciu = serviciu.child("descriereServiciu").value.toString()

                Log.i(
                    "ValoriRedate",
                    "Got: ${serviciu.child("numeServiciu").value.toString()}| $numeServiciu"
                )
                Log.i(
                    "ValoriRedate",
                    "Got: ${serviciu.child("pretServiciu").value.toString()}| $pretServiciu"
                )
                Log.i(
                    "ValoriRedate",
                    "Got: ${serviciu.child("descriereServiciu").value.toString()}| $descriereServiciu"
                )

                dataClass = serviciuTest(numeServiciu, pretServiciu, descriereServiciu)
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

        }
    }

    private fun initRecycler() {
        adapter = AdapterServiciu(datalist)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter
        title = "PetSitter's services"
    }

    private fun initUI() {
        recyclerView = findViewById(R.id.recycler_view_servicii_details)
        toolbar = findViewById(R.id.servicii_details_toolbar)
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("PetSitter")
        currentUserServicii=databaseReference.child("PetSitter")
            .child("User(unique email)").child("Servicii")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val passedEmail = intent.getStringExtra(Constants.email_selected_marker)
                for(user in snapshot.children){
                    val currentEmail = user.child("email").value.toString()
                    if(currentEmail==passedEmail){
                        val currentNume = user.child("nume").value.toString()
                        val currentPrenume=user.child("prenume").value.toString()
                        supportActionBar?.apply {
                            title = "Servicii $currentPrenume $currentNume"
                        }
                        val currentUserUID = user.key.toString()
                        uidPetSitter=currentUserUID
                        currentUserServicii=databaseReference.child(currentUserUID).child("Servicii")
                        fetchFromFirebase()
                        Log.i("currentUserServicii","$currentUserServicii")
                        break
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@ServiciiDetailsActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()

                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
    }

    private fun showDialogForAction(position: Int,serviciu:serviciuTest) {
        val dialog = Dialog(this@ServiciiDetailsActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.schedule_serviciu_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnSchedule: Button = dialog.findViewById(R.id.customDialogEditButton)
        val btnBack: Button = dialog.findViewById(R.id.customDialogDeleteButton)

        Log.d("PositionInDialog",position.toString())

        //cdTitle.text = "Ati ales serviciul: ${retrieveDataFromListField(position)}"
        cdTitle.text = "Ati ales serviciul: ${serviciu.nume}"

        btnSchedule.setOnClickListener {
            val toScheduleServiciuActivity = Intent(this,ScheduleServiciuActivity::class.java)
            toScheduleServiciuActivity.putExtra(Constants.serviciu_ales_nume,serviciu.nume)
            toScheduleServiciuActivity.putExtra(Constants.serviciu_ales_pret,serviciu.pret)
            toScheduleServiciuActivity.putExtra(Constants.serviciu_ales_descriere,serviciu.descriere)
            toScheduleServiciuActivity.putExtra(Constants.serviciu_ales_proprietar,uidPetSitter)
            Log.d("Passed data to activity","Nume: ${serviciu.nume}| Pret: ${serviciu.pret}| \n" +
                    "Descriere: ${serviciu.descriere}| UID: $uidPetSitter")
            startActivity(toScheduleServiciuActivity)
            //Toast.makeText(this@ServiciiDetailsActivity, "Pressed Schedule", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun retrieveDataFromListField(position: Int): String {
        Log.d("PositionRetrieveData","Position: $position")
        if (position >= 0 && position < datalist.size) {
            return datalist[position].nume
        } else {
            //Toast.makeText(this," $position + ${datalist}",Toast.LENGTH_LONG).show()
            return "Am iesit din index"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                fetchFromFirebase()
            }
            if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this@ServiciiDetailsActivity, "Bad REsult Code", Toast.LENGTH_LONG).show()
            }
        }
    }
}