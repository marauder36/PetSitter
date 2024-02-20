package com.example.firebasertdb.activities.PetSitter.activities.servicii

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
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.activities.PetSitter.activities.servicii.adapters.AdapterServiciu
import com.example.firebasertdb.models.serviciuTest
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.SharedStorageManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class ServiciiActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserServicii: DatabaseReference
    private lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var sharedManager : SharedStorageManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAddServiciu: FloatingActionButton
    private lateinit var adapter: AdapterServiciu

    private lateinit var datalist: MutableList<serviciuTest>
    private lateinit var dataClass: serviciuTest
    private lateinit var numeServiciuDeTrimis:String
    private lateinit var serviciiID:MutableList<String>
    private lateinit var currentUser:DatabaseReference
    private lateinit var listaPreturi:MutableList<Int>

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@ServiciiActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_servicii)

        initUI()
        initToolbar(toolbar)

        datalist = mutableListOf()
        sharedManager = SharedStorageManager(this)

        mAuth = FirebaseAuth.getInstance()

        //val prefType = sharedManager.getString("UserType")
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")

        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType.toString())
        currentUser=databaseReference.child(mAuth.currentUser?.uid.toString())
        currentUserServicii = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Servicii")

        initRecycler()
        serviciiID= mutableListOf()
        fetchFromFirebase()

        buttonAddServiciu.setOnClickListener {
            var requestCode = 1
            var intent = Intent(this, AddServiciuActivity::class.java)
            startActivityForResult(intent,requestCode)
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
            title = "Serviciile tale"
        }
    }

    private fun initRecycler() {
        adapter = AdapterServiciu(datalist)
        adapter.setOnItemClickListener { position ->
            showDialogForAction(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter

    }

    private fun initUI() {
        recyclerView = findViewById(R.id.recycler_view_servicii)
        buttonAddServiciu = findViewById(R.id.rv_list_button_add)
        toolbar = findViewById(R.id.servicii_toolbar)
    }

    private fun showDialogForAction(position: Int) {
        val dialog = Dialog(this@ServiciiActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_serviciu_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
        val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
        val btnEdit: Button = dialog.findViewById(R.id.customDialogEditButton)
        val btnDelete: Button = dialog.findViewById(R.id.customDialogDeleteButton)

        Log.d("PositionInDialog",position.toString())

        cdTitle.text = "Ati ales serviciul: ${retrieveDataFromListField(position)}"

        btnEdit.setOnClickListener {
            var requestCode = 1
            var toEditIntent = Intent(this, EditServiciuActivity::class.java)
            toEditIntent.putExtra(Constants.SERVICIU_ALES,serviciiID[position])
            startActivityForResult(toEditIntent,requestCode)
            //Toast.makeText(this@ServiciiActivity, "Pressed EDIT", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            deleteServiciu(position)
            //Toast.makeText(this@ServiciiActivity, "Pressed DELETE", Toast.LENGTH_LONG).show()
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

    private fun deleteServiciu(position: Int) {
        Log.d("PositionDeleteServiciu","Position: $position")
        if (position >= 0 && position < datalist.size ) {

            val childName = serviciiID[position]

            currentUserServicii.child(childName).removeValue()
                .addOnSuccessListener {

                    datalist.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    saveToRTDB()
//                    Toast.makeText(
//                        this@ServiciiActivity,
//                        "Deleted serviciu from database",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
                .addOnFailureListener {
//                    Toast.makeText(
//                        this@ServiciiActivity,
//                        "Could not delete data from database",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
        } else {
//            Toast.makeText(
//                this@ServiciiActivity,
//                "Could not delete, index out of bounds",
//                Toast.LENGTH_LONG
//            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                saveToRTDB()
                fetchFromFirebase()
            }
            if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this@ServiciiActivity, "Bad REsult Code", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        saveToRTDB()
        super.onBackPressed()
    }

    private fun saveToRTDB()
    {
        currentUser.get().addOnSuccessListener {snapshot->
            var listaPreturi = mutableListOf<Int>()
            var pret = 0
            for(serviciu in snapshot.child("Servicii").children){
                pret = serviciu.child("pretServiciu").value.toString().toInt()
                Log.i("PretInFor","$pret")
                listaPreturi.add(pret)
            }
            Log.i("PretInForLista","$listaPreturi")
            if(listaPreturi.isNotEmpty()) {
                val mediePreturiServicii = avgServiciuCost(listaPreturi)
                currentUser.child("mediePreturiServicii").setValue(mediePreturiServicii)
            }else{
                currentUser.child("mediePreturiServicii").setValue(0)
            }
        }
    }
    private fun avgServiciuCost(serviciiCostList: MutableList<Int>):Int{
        var medie = 0
        for(pret in serviciiCostList){
            medie += pret
        }
        medie /= (serviciiCostList.size)
        return medie
    }
}