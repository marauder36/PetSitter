package com.example.firebasertdb.activities.PetOwner.Pets

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.utils.Constants
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class EditPetActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserPets: DatabaseReference

    private lateinit var editPetTitle:TextView
    private lateinit var editPetImagine:ImageView
    private lateinit var editPetNume:TextInputEditText
    private lateinit var editPetRasa:TextInputEditText
    private lateinit var editPetIstoric:TextInputEditText
    private lateinit var editPetNecesitati:TextInputEditText
    private lateinit var editPetSaveChangesBTN:Button
    private lateinit var toolbar:Toolbar

    private lateinit var numePetRTDB:String
    private lateinit var rasaPetRTDB:String
    private lateinit var necesitatiPetRTDB:String
    private lateinit var istoricPetRTDB:String
    private lateinit var petCurr:String

    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var uri: Uri
    private lateinit var storageRef: StorageReference
    private val STORAGE_REQUEST_CODE= 8181

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@EditPetActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_edit_pet)

        uri=Uri.parse("Placeholder")
        initUI()
        initToolbar()
        initRTDB()
        setDataToUI()
        setDataToImageView()

        editPetImagine.setOnClickListener {
            animation(editPetImagine)
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
                    editPetImagine.setImageURI(result.data?.data)
                    uri = result.data?.data!!
                }
            }
        
        editPetSaveChangesBTN.setOnClickListener {
            checkEmptyEditsPet()

            if(this::uri.isInitialized){
                val filePath=storageRef.child("petImages").child(uri.lastPathSegment!!)
                filePath.putFile(uri).addOnSuccessListener { task->
                    val result: Task<Uri> = task.metadata?.reference?.downloadUrl!!
                    result.addOnSuccessListener {
                        uri = it
                        //currentUserPets.child(numePet.text.toString()).child("imaginePet").setValue("$uri")
                        saveChangesPet()
                            //Toast.makeText(this@EditPetActivity,"Image saved",Toast.LENGTH_LONG).show()
                    }
                        .addOnFailureListener {
                            saveChangesPet()
                            Log.i("Eroare la download URI","S-a cacat pe el") }
                }.addOnFailureListener {
                    saveChangesPet()
                    Log.i("Eroare la salvare URI","S-a cacat pe el")}
            }
            else{
                Log.i("Eroare la la initializare URI","S-a cacat pe el")
                saveChangesPet()
                //Toast.makeText(this@EditPetActivity,"Image not saved",Toast.LENGTH_LONG).show()
            }

        }


    }

    private fun initUI(){
        storageRef = FirebaseStorage.getInstance().reference
        editPetSaveChangesBTN=findViewById(R.id.pet_button_edit)
        editPetImagine=findViewById(R.id.imagine_pet_edit)
        editPetTitle=findViewById(R.id.edit_pet_title)
        editPetNume=findViewById(R.id.ti_edit_text_nume_pet_edit)
        editPetRasa=findViewById(R.id.ti_edit_text_rasa_pet_edit)
        editPetIstoric=findViewById(R.id.ti_edit_text_istoric_medical_pet_edit)
        editPetNecesitati=findViewById(R.id.ti_edit_text_necesitati_pet_edit)

    }

    private fun initToolbar(){
        toolbar = findViewById(R.id.edit_pet_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Editeaza un pet"
        }
    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        //val prefType = sharedManager.getString("UserType")
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType.toString())
        currentUserPets = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Pets")
    }

    private fun setDataToUI(){
        petCurr=intent.getStringExtra(Constants.pet_ales).toString()
        editPetTitle.text=petCurr
        currentUserPets.child(petCurr).get()
            .addOnSuccessListener {
                Log.i("EditPetData","Got: $it")

                numePetRTDB=it.child("numePet").value.toString()
                Log.i("EditPetNume","Got: $numePetRTDB")
                editPetNume.setText(numePetRTDB)

                rasaPetRTDB=it.child("rasaPet").value.toString()
                editPetRasa.setText(rasaPetRTDB)

                necesitatiPetRTDB=it.child("necesitatiPet").value.toString()
                editPetNecesitati.setText(necesitatiPetRTDB)

                istoricPetRTDB=it.child("istoricMedicalScrisPet").value.toString()
                editPetIstoric.setText(istoricPetRTDB)
            }
            .addOnFailureListener {
                //Toast.makeText(this@EditPetActivity,"Faile to load data from RTDB",Toast.LENGTH_LONG).show()
            }
    }

    private fun setDataToImageView(){
        currentUserPets.child(petCurr).child("imaginePet").get()
            .addOnSuccessListener {
                if(it.value!="Placeholder"){
                    val uriString = it.value.toString()
                    uri=Uri.parse(uriString)
                    Glide.with(applicationContext).load(uriString).into(editPetImagine)
                    Log.i("Data Accessed","Again")}
            }
    }

    private fun checkEmptyEditsPet(){
        if(editPetNume.text!!.isEmpty()){
            editPetNume.setError("Numele nou nu poate fi gol !")
            editPetNume.requestFocus()
        }
        else if(editPetRasa.text!!.isEmpty()){
            editPetRasa.setError("Rasa nou nu poate fi gol !")
            editPetRasa.requestFocus()
        }
        else if(editPetNecesitati.text!!.isEmpty()){
            editPetNecesitati.setError("Noile necesitati nu pot fi goale !")
            editPetNecesitati.requestFocus()
        }
        else if(editPetIstoric.text!!.isEmpty()){
            editPetIstoric.setError("Noul istoric nu pot fi gol !")
            editPetIstoric.requestFocus()
        }
    }

    private fun saveChangesPet(){
        val refCurr=currentUserPets.child(petCurr)


        if(rasaPetRTDB!=editPetRasa.text.toString()){
            refCurr.child("rasaPet").setValue(editPetRasa.text.toString())
            //Toast.makeText(this@EditPetActivity,"Rasa nu e la fel",Toast.LENGTH_SHORT).show()
        }
        if(necesitatiPetRTDB!=editPetNecesitati.text.toString()){
            refCurr.child("necesitatiPet").setValue(editPetNecesitati.text.toString())
            //Toast.makeText(this@EditPetActivity,"Necesitatia nu e la fel",Toast.LENGTH_SHORT).show()
        }
        if(istoricPetRTDB!=editPetIstoric.text.toString()){
            refCurr.child("istoricMedicalScrisPet").setValue(editPetIstoric.text.toString())
            //Toast.makeText(this@EditPetActivity,"Istoricul nu e la fel",Toast.LENGTH_SHORT).show()
        }
        if(numePetRTDB!=editPetNume.text.toString()){
            refCurr.child("numePet").setValue(editPetNume.text.toString())
            //Toast.makeText(this@EditPetActivity,"Numele nu e la fel",Toast.LENGTH_SHORT).show()
        }
        currentUserPets.child(petCurr)
        refCurr.child("imaginePet").setValue(uri.toString())

        var intent = Intent()
        setResult(Activity.RESULT_OK,intent)
        finish()
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
                .setMessage("This permission is needed for accessing the internal storage to store and retrieve images")
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
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

    private fun animation(imageView: ImageView){
        val zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.calendar_on_click)
        imageView.startAnimation(zoomAnimation)
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