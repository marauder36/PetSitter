package com.example.firebasertdb.activities.PetOwner.Pets

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.models.PetClass
import com.example.firebasertdb.utils.Constants
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddPetActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var numeRTDB: Any? = null

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var prefType:String
    private lateinit var currentUserPets:DatabaseReference

    private lateinit var imaginePet:ImageView
    private lateinit var numePet: TextInputEditText
    private lateinit var rasaPet: TextInputEditText
    private lateinit var istoricPet: TextInputEditText
    private lateinit var necesitatiPet:TextInputEditText
    private lateinit var addPetBTN: Button

    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var uri: Uri
    private lateinit var storageRef: StorageReference
    private val STORAGE_REQUEST_CODE= 6969

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@AddPetActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_add_pet)
        uri=Uri.parse("Placeholder")
        initUI()
        initToolbar()
        initRTDB()

        currentUserPets.child(numePet.text.toString()).child("imaginePet").get()
            .addOnSuccessListener {
                if(it.value!=null){
                    val uriString = it.value.toString()
                    Glide.with(this@AddPetActivity).load(uriString).into(imaginePet)
                    Log.i("Data Accessed","Again")}
            }

        imaginePet.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermission()
            } else {
                getImage()
            }
        }

        getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    imaginePet.setImageURI(result.data?.data)
                    uri = result.data?.data!!
                }
            }

        addPetBTN.setOnClickListener {

            if(this::uri.isInitialized){
                val filePath=storageRef.child("petImages").child(uri.lastPathSegment!!)
                filePath.putFile(uri).addOnSuccessListener { task->
                    val result: Task<Uri> = task.metadata?.reference?.downloadUrl!!
                    result.addOnSuccessListener {
                        uri = it
                        //currentUserPets.child(numePet.text.toString()).child("imaginePet").setValue("$uri")
                        savePetToRTDB()
                        //Toast.makeText(this@AddPetActivity,"Image saved",Toast.LENGTH_LONG).show()
                    }
                        .addOnFailureListener {
                            savePetToRTDB()
                            Log.i("Eroare la download URI","S-a cacat pe el") }
                }.addOnFailureListener {
                    savePetToRTDB()
                    Log.i("Eroare la salvare URI","S-a cacat pe el")}
            }
            else{
                Log.i("Eroare la la initializare URI","S-a cacat pe el")
                savePetToRTDB()
               // Toast.makeText(this@AddPetActivity,"Image not saved",Toast.LENGTH_LONG).show()
            }

            Log.i("NumePetCurent","${numePet.text.toString()} vs $numeRTDB")

        }


    }

    private fun initUI() {
        storageRef = FirebaseStorage.getInstance().reference
        imaginePet=findViewById(R.id.imagine_pet_add)
        numePet = findViewById(R.id.ti_edit_text_nume_pet_add)
        rasaPet = findViewById(R.id.ti_edit_text_rasa_pet_add)
        istoricPet = findViewById(R.id.ti_edit_text_istoric_medical_pet_add)
        necesitatiPet=findViewById(R.id.ti_edit_text_necesitati_pet_add)
        addPetBTN = findViewById(R.id.pet_button_add)
    }
    private fun initToolbar(){
        toolbar = findViewById(R.id.add_pet_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Adauga un nou pet"
        }
    }
    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType", "Petowner or PetSitter")!!
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType)
        currentUserPets = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Pets")
    }

    private fun savePetToRTDB(){
        currentUserPets.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                numeRTDB = snapshot.child(numePet.text.toString()).child("numePet").value.toString()

                Log.i("NumePetRTDB", "Nume: ${numeRTDB}")

                if (numePet.text!!.isEmpty()) {
                    numePet.setError("Numele animalutului nu poate fi gol !")
                    numePet.requestFocus()
                } else if (rasaPet.text!!.isEmpty()) {
                    rasaPet.setError("Rasa animalutului nu poate fi goala !")
                    rasaPet.requestFocus()
                } else if (istoricPet.text!!.isEmpty()) {
                    istoricPet.setError("Istoricul medical necesita cel putin vaccinurile")
                    istoricPet.requestFocus()
                } else if (necesitatiPet.text!!.isEmpty()) {
                    necesitatiPet.setError("Descrie cel putin o particularitate a animalutului")
                    necesitatiPet.requestFocus()
                } else if (numePet.text.toString() == numeRTDB) {
                    numePet.setError("Acest pet deja exista !")
                    numePet.requestFocus()
                } else if (numePet.text.toString() != numeRTDB) {

                    currentUserPets.child(numePet.text.toString())
                        .setValue(PetClass(uri.toString(),numePet.text.toString(), rasaPet.text.toString(), istoricPet.text.toString(),necesitatiPet.text.toString()))
                    var intent = Intent()
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //Toast.makeText(this@AddPetActivity, "Couldn't retrieve data: ${error.toException()}", Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
    }
    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_button_yes) {_, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        STORAGE_REQUEST_CODE)
                }.setNegativeButton(R.string.dialog_button_no) {dialog, _ ->
                    dialog.cancel()
                }.setTitle("Permission needed")
                .setMessage("This permission is needed for accessing the internal storage")
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE && grantResults.size > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getImage()
        } else {

            AlertDialog.Builder(this)
                .setPositiveButton("Settings") {_, _ ->
                    goToSettings(context = applicationContext)
                }.setNegativeButton("No thanks") {dialog, _ ->
                    dialog.cancel()
                }.setTitle("Go to settings ?")
                .setMessage("This permission is needed for accessing the internal storage." +
                        " Please allow it in the settings of this app.")
                .show()

            //Toast.makeText(this,"Permission not granted", Toast.LENGTH_LONG).show()
        }
    }


    private fun getImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getResult.launch(intent)
    }

    private fun goToSettings(context: Context){

        val newintent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        with(newintent) {
            data = Uri.fromParts("package", context.packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        startActivity(newintent)
    }
}
