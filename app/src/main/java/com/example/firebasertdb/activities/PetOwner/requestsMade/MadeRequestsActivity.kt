package com.example.firebasertdb.activities.PetOwner.requestsMade

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.requestsMade.adapter.AdapterRequestWithPrice
import com.example.firebasertdb.activities.PetSitter.activities.adapters.AdapterRequest
import com.example.firebasertdb.activities.PetSitter.activities.servicii.adapters.AdapterServiciu
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.ServiceRequestClassPrice
import com.example.firebasertdb.models.ServiciiClass
import com.example.firebasertdb.models.serviciuTest
import com.example.firebasertdb.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MadeRequestsActivity : AppCompatActivity() {
    private lateinit var toolbar:Toolbar
    private lateinit var requestsMadeListRV: RecyclerView
    private lateinit var requestsThatNeedPay:MutableList<ServiceRequestClassPrice>

    private var alertDialogPay: AlertDialog? = null

    private lateinit var serviciuDescriere:             String
    private lateinit var serviciuTitlu:          String
    private lateinit var serviciuPret:           String
    private lateinit var serviciiDePlatit: MutableList<serviciuTest>

    private lateinit var adapterServiciiClass: AdapterServiciu

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var dataList:MutableList<ServiceRequestClassPrice>
    private lateinit var requestID:MutableList<String>
    private lateinit var adapter: AdapterRequestWithPrice
    val EDIT_REQUEST_CODE = 9834

    override fun onStart() {
        val firstAuth=FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
            super.onStart()
        }
        else{
            startActivity(Intent(this, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_made_requests)

        dataList= mutableListOf()
        requestID= mutableListOf()
        requestsThatNeedPay= mutableListOf()
        serviciiDePlatit= mutableListOf()

        serviciuTitlu    ="Nume Serviciu"
        serviciuPret     ="Pret"
        serviciuDescriere="Descriere"

        initui()
        initToolbar(toolbar)
        initRTDB()
        initRecycler(dataList)

        adapter.setOnItemClickListener { position->
            val clickedItem = adapter.getClickedItem(position)
            if(clickedItem?.status!="Pending")
                displayNotModDialog(clickedItem!!.status)
            else
                showDialogForMadeRequest(clickedItem,requestID[position])
        }
    }

    private fun displayNotModDialog(toString: String) {
        AlertDialog.Builder(this)
            .setMessage("Rezervarile $toString nu mai pot fi modificate !")
            .setPositiveButton("Am inteles"){dialog,_->
                dialog.dismiss()
            }
            .create().show()
    }

    private fun initui(){
        toolbar=findViewById(R.id.requests_made_toolbar)
        requestsMadeListRV=findViewById(R.id.requests_made_rv)

    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Your requests"
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("PetSitter")
        readDataFromRTDB()
    }

    private fun initRecycler(dataList:MutableList<ServiceRequestClassPrice>){
        adapter = AdapterRequestWithPrice(dataList,this)
        requestsMadeListRV.layoutManager = LinearLayoutManager(this)
        requestsMadeListRV.setHasFixedSize(false)
        requestsMadeListRV.adapter = adapter
    }

    private fun setDataToServiciuViews(numeServiciu:String){

        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for(user in snapshot.children){
                    for(service in user.child("Servicii").children)
                    {
                        if(service.child("numeServiciu").value.toString()==numeServiciu){

                            serviciuTitlu    =service.child("numeServiciu").value.toString()
                            serviciuPret     =service.child("pretServiciu").value.toString()
                            serviciuDescriere=service.child("descriereServiciu").value.toString()


                            serviciiDePlatit.add(serviciuTest(serviciuTitlu,serviciuPret,serviciuDescriere))
                            adapterServiciiClass.setData(serviciiDePlatit)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                Log.i("ServiciuDetails","$serviciiDePlatit")




            }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@MadeRequestsActivity,"Erorr: $error",Toast.LENGTH_LONG).show()
            }

        })


    }

    private fun readDataFromRTDB() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                requestID.clear()
                requestsThatNeedPay.clear()
                for(user in snapshot.children){
                    for(request in user.child("Requests").children){
                        if (request.child("requestingUserID").value.toString()==mAuth.currentUser?.uid.toString()){

                            val requestingUserID = request.child("requestingUserID").value.toString()
                            val requestingUserName=request.child("requestingUserName").value.toString()
                            val serviceRequested = request.child("serviceRequested").value.toString()
                            val status = request.child("status").value.toString()
                            val date = request.child("date").value.toString()
                            val hour = request.child("hour").value.toString()
                            val petID = request.child("petID").value.toString()
                            val petName = request.child("petName").value.toString()
                            val petRace = request.child("petRace").value.toString()
                            val pretServiciuCerut=request.child("pretServiciuCerut").value.toString()

                            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                            val dateToCheck = dateFormat.parse(date)

                            if(dateToCheck.before(Date())&&status=="Pending")
                            {
                                databaseReference.child(user.key.toString()).child("Requests").child(request.key.toString()).child("status").setValue("Expired")
                            }

                            val calendar =Calendar.getInstance()
                            calendar.time=Date()
                            calendar.add(Calendar.DAY_OF_MONTH,1)
                            val tomorrow = calendar.get(Calendar.DAY_OF_MONTH)
                            Log.i("RequestsThatneedpay","Got: $tomorrow")
                            val ziRequest = date.substringBefore("-").toInt()
                            Log.i("RequestsThatneedpay","Got: $ziRequest")
                            Log.i("RequestsThatneedpay","Got: ${ziRequest==tomorrow}")

                            if (status=="Accepted" && dateToCheck!=null && ziRequest==tomorrow)
                            {
                                requestsThatNeedPay.add(ServiceRequestClassPrice(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,pretServiciuCerut))
                            }

                            Log.i("RequestsThatneedpay","Got: $requestsThatNeedPay")


                            requestID.add(request.key.toString())
                            dataList.add(ServiceRequestClassPrice(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,pretServiciuCerut))
                            Log.i("DataList","$dataList")
                        }
                    }
                }

                showPaymentDialog(requestsThatNeedPay)
                Log.i("DataList","$dataList")
                adapter.setData(dataList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@MadeRequestsActivity,"Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }

        })

    }

    private fun showPaymentDialog(requestsThatNeedPay: MutableList<ServiceRequestClassPrice>) {
        //moneyBackGuarantee()
        alertDialogPay?.dismiss()

        if(requestsThatNeedPay.isNotEmpty()){
        val dialogView: View = LayoutInflater.from(this@MadeRequestsActivity)
            .inflate(R.layout.payment_dialog_layout,null)
        val showAcceptedRequestsRV = dialogView.findViewById<RecyclerView>(R.id.show_accepted_requests)
        val totalDePlata = dialogView.findViewById<TextView>(R.id.total_de_plata)
        val butonPlata=dialogView.findViewById<AppCompatButton>(R.id.pay_button)
        val butonNuAcum=dialogView.findViewById<AppCompatButton>(R.id.not_now_button)
        val adapterAcceptedRequests=AdapterRequestWithPrice(requestsThatNeedPay,this)

        val serviciiDePlatitRV=dialogView.findViewById<RecyclerView>(R.id.servicii_de_platit_RV)
        serviciiDePlatit.clear()
        adapterServiciiClass=AdapterServiciu(serviciiDePlatit)
        serviciiDePlatitRV.layoutManager=LinearLayoutManager(this@MadeRequestsActivity)
        serviciiDePlatitRV.setHasFixedSize(false)
        serviciiDePlatitRV.adapter=adapterServiciiClass

        for (request in requestsThatNeedPay){
            setDataToServiciuViews(request.serviceRequested)
        }

        showAcceptedRequestsRV.layoutManager = LinearLayoutManager(this)
        showAcceptedRequestsRV.setHasFixedSize(false)
        showAcceptedRequestsRV.adapter = adapterAcceptedRequests

        var sumaTotala = 0
        for (request in requestsThatNeedPay)
        {
            sumaTotala+=request.pretServiciuCerut.toInt()
        }
        totalDePlata.text="In total ai de platit: $sumaTotala RON"

        butonPlata.setOnClickListener {
            val toPayActivity = Intent(this,PayActivity::class.java)
            databaseReference.get().addOnSuccessListener { snapshot->
                for(user in snapshot.children)
                {
                    for(request in user.child("Requests").children)
                    {
                        val requestingUserID = request.child("requestingUserID").value.toString()
                        val requestingUserName=request.child("requestingUserName").value.toString()
                        val serviceRequested = request.child("serviceRequested").value.toString()
                        val status = request.child("status").value.toString()
                        val date = request.child("date").value.toString()
                        val hour = request.child("hour").value.toString()
                        val petID = request.child("petID").value.toString()
                        val petName = request.child("petName").value.toString()
                        val petRace = request.child("petRace").value.toString()
                        val pretServiciuCerut=request.child("pretServiciuCerut").value.toString()

                        for (currRequest in requestsThatNeedPay)
                            if(ServiceRequestClassPrice(requestingUserID,requestingUserName,serviceRequested,
                                    status,date,hour,petID,petName,petRace,pretServiciuCerut)==currRequest)
                                        databaseReference.child("${user.key.toString()}").child("Requests").
                                        child(request.key.toString()).child("status").setValue("Payed")
                                            .addOnSuccessListener {
                                                Log.i("PayedRequest","${request.key.toString()}")
                                            }


                    }
                }
            }
        }
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        // Create and show the dialog
        alertDialogPay = builder.setOnCancelListener {
            AlertDialog.Builder(this)
                .setTitle("Atentie !")
                .setMessage("Toate rezervarile trebuie achitate inainte de 23:59 a de dinainte a rezervarii pentru a oferi suficient timp PetSitter-ului pentru a se pregati." +
                        "Daca nu achitati pana la 23:59 in ziua de dinainte rezervarea, chiar daca PetSitter-ul a dat accept, rezervarea va expira automat !")
                .setPositiveButton("Am inteles"){dialog,_->
                    dialog.dismiss()
                }
                .setOnDismissListener {
                    it.dismiss()
                }
                .create().show()
        }.create()
        
        alertDialogPay?.show()

        butonNuAcum.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Atentie !")
                .setMessage("Toate rezervarile trebuie achitate inainte de 23:59 a de dinainte a rezervarii pentru a oferi suficient timp PetSitter-ului pentru a se pregati." +
                        "Daca nu achitati pana la 23:59 in ziua de dinainte rezervarea, chiar daca PetSitter-ul a dat accept, rezervarea va expira automat !")
                .setPositiveButton("Am inteles"){dialog,_->
                    dialog.dismiss()
                }
                .setOnDismissListener {
                    alertDialogPay?.dismiss()
                }
                .create().show()

        }
    }

    }

    private fun deleteDataFromRTDB(request: ServiceRequestClassPrice?){
        databaseReference.get().addOnSuccessListener {snapshot->

            for(user in snapshot.children){
                for(currRequest in user.child("Requests").children){

                    val requestingUserID = currRequest.child("requestingUserID").value.toString()
                    val requestingUserName=currRequest.child("requestingUserName").value.toString()
                    val serviceRequested = currRequest.child("serviceRequested").value.toString()
                    val status = currRequest.child("status").value.toString()
                    val date = currRequest.child("date").value.toString()
                    val hour = currRequest.child("hour").value.toString()
                    val petID = currRequest.child("petID").value.toString()
                    val petName = currRequest.child("petName").value.toString()
                    val petRace = currRequest.child("petRace").value.toString()
                    val pretServiciuCerut=currRequest.child("pretServiciuCerut").value.toString()

                    if(ServiceRequestClassPrice(requestingUserID,requestingUserName,serviceRequested,status,date,hour,petID,petName,petRace,pretServiciuCerut)==request)
                        databaseReference.child("${user.key.toString()}").child("Requests").child(currRequest.key.toString()).removeValue()
                        }
            }
        }
            .addOnFailureListener {
                //Toast.makeText(this@MadeRequestsActivity,"Couldn't retrieve data: ${it}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", it)
            }
    }

    private fun showDialogForMadeRequest(request:ServiceRequestClassPrice?,requestID:String){

            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.edit_or_delete_request)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val cdTitle: TextView = dialog.findViewById(R.id.customDialogTitle)
            val cdMessage: TextView = dialog.findViewById(R.id.customDialogMessageText)
            val btnEdit: Button = dialog.findViewById(R.id.customDialogEditButton)
            val btnDelete: Button = dialog.findViewById(R.id.customDialogDeleteButton)

            cdTitle.text = "Ati ales rezervarea: ${request?.serviceRequested} pe data de: ${request?.date}"

            btnEdit.setOnClickListener {
                var toEditIntent = Intent(this, EditMadeRequestActivity::class.java)

                toEditIntent.putExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_PETOWNERID,request?.requestingUserName)
                toEditIntent.putExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_SERVICE_NAME,request?.serviceRequested)
                toEditIntent.putExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_DATE,request?.date)
                toEditIntent.putExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_HOUR,request?.hour)
                toEditIntent.putExtra(Constants.REQUEST_SELECTED_BY_PETOWNER_PET_NAME,request?.petID)
                toEditIntent.putExtra(Constants.Selected_request_ID,requestID)

                startActivityForResult(toEditIntent,EDIT_REQUEST_CODE)

                dialog.dismiss()
            }

            btnDelete.setOnClickListener {
                deleteDataFromRTDB(request)

                dialog.dismiss()
            }

            dialog.show()
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                adapter.notifyDataSetChanged()
            }
            if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this@ServiciiActivity, "Bad REsult Code", Toast.LENGTH_LONG).show()
            }
        }
    }

}