package com.example.firebasertdb.activities.PetOwner

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.firebasertdb.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mikhaellopez.circularimageview.CircularImageView
import android.net.Uri
import android.widget.Button
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.firebasertdb.utils.Constants
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.Glide
import com.example.firebasertdb.activities.PetOwner.Pets.AddPetActivity
import com.example.firebasertdb.activities.PetOwner.Pets.PetsActivity
import com.example.firebasertdb.activities.PetSitter.activities.calendar.CalendarActivity

import com.example.firebasertdb.activities.PetSitter.activities.servicii.ServiciiActivity
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.google.android.gms.maps.model.Dash


class EditProfilePetOwnerActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth1: FirebaseAuth

    private lateinit var editTextTitle:TextView
    private lateinit var profilePicture: CircularImageView
    private lateinit var currNume:TextInputEditText
    private lateinit var currPrenume:TextInputEditText
    private lateinit var currUsername:TextInputEditText
    private lateinit var currEmail:TextInputEditText
    private lateinit var currAdresa:TextInputEditText
    private lateinit var currTelefon:TextInputEditText
    private lateinit var currDescriere:TextInputEditText
    private lateinit var toolbar: Toolbar
    private lateinit var prefType:String
    private lateinit var currentUser: DatabaseReference

    private lateinit var uri: Uri
    private lateinit var storageRef:StorageReference

    private lateinit var getResult: ActivityResultLauncher<Intent>

    private lateinit var save_changes:Button
    private lateinit var petsBTN:Button

    private val STORAGE_REQUEST_CODE= 3636

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onStart() {
        val firstAuth=FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
            super.onStart()
        }
        else{
            startActivity(Intent(this@EditProfilePetOwnerActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_edit_profile_pet_owner)

        initUI()
        initToolbar()
        initRTDB()

        currentUser.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                updateUserProfile(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
               // Toast.makeText(this@EditProfilePetOwnerActivity,"Couldn't retrieve data: ${error.toException()}",Toast.LENGTH_LONG).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })

        currentUser.child("imagine").get()
            .addOnSuccessListener {
                if(it.value!=null) {
                    val uriString = it.value.toString()
                    Glide.with(this@EditProfilePetOwnerActivity).load(uriString)
                        .into(profilePicture)
                    Log.i("Data Accessed", "Again")
                }
            }


        storageRef = FirebaseStorage.getInstance().reference

        profilePicture.setOnClickListener {
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
                    profilePicture.setImageURI(result.data?.data)
                    uri = result.data?.data!!
                }
            }


        petsBTN.setOnClickListener {
            startActivity(Intent(this@EditProfilePetOwnerActivity, PetsActivity::class.java))
        }

        save_changes.setOnClickListener {

            if(this::uri.isInitialized){
                val filePath=storageRef.child("profile_images").child(uri.lastPathSegment!!)
                filePath.putFile(uri).addOnSuccessListener { task->
                    val result:Task<Uri> = task.metadata?.reference?.downloadUrl!!
                    result.addOnSuccessListener {
                        uri = it
                        currentUser.child("imagine").setValue("$uri")
                        //Toast.makeText(this@EditProfilePetOwnerActivity,"Image saved",Toast.LENGTH_LONG).show()
                        saveDataToRTDB(currentUser)
                    }
                        .addOnFailureListener {
                            saveDataToRTDB(currentUser)
                        }
                }
                    .addOnFailureListener{
                        saveDataToRTDB(currentUser)
                    }
            }
            else{
                saveDataToRTDB(currentUser)
                //Toast.makeText(this@EditProfilePetOwnerActivity,"Image not saved",Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun initRTDB(){
        mAuth1 = FirebaseAuth.getInstance()
        prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetOwner")!!
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType)
        currentUser = databaseReference.child(mAuth1.currentUser?.uid.toString())
    }
    
    private fun initToolbar(){
        toolbar = findViewById(R.id.edit_profile_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Profile"
        }
    }

    private fun initUI() {
        save_changes=findViewById(R.id.save_changes_button)
        petsBTN=findViewById(R.id.servicii_button)
        editTextTitle = findViewById(R.id.edit_text_title)
        profilePicture = findViewById(R.id.editProfileImage)
        currNume = findViewById(R.id.ti_edit_text_nume)
        currPrenume = findViewById(R.id.ti_edit_text_prenume)
        currUsername=findViewById(R.id.ti_edit_text_username)
        //currEmail = findViewById(R.id.ti_edit_text_email)
        currAdresa=findViewById(R.id.ti_edit_text_address)
        currTelefon=findViewById(R.id.ti_edit_text_phone)
        currDescriere=findViewById(R.id.ti_edit_text_descriere)

    }

    private fun saveDataToRTDB(currentUser: DatabaseReference) {
        var nothingEmpty=0
        database.getReference("Petowner").get()
            .addOnSuccessListener {snapshot->

            var cnt = 0
            for(user in snapshot.children){
                val usernameCurent=currUsername.text.toString()
                val currUserID=user.key.toString()
                val currentUserID=mAuth1.currentUser?.uid.toString()
                if(currUserID!=currentUserID &&
                    usernameCurent==user.child("username").value.toString()){
                    cnt++
                }
            }
            if (cnt!=0){
                Toast.makeText(this,"Username-ul este deja folosit !",Toast.LENGTH_SHORT).show()
            }
            else{
                currentUser.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val numeRTDB = snapshot.child("nume").value.toString()
                        val prenumeRTDB = snapshot.child("prenume").value.toString()
                        val usernameRTDB = snapshot.child("username").value.toString()

                        val adresaRTDB = snapshot.child("address").value.toString()
                        val telefonRTDB = snapshot.child("phoneNumber").value.toString()
                        val descriereRTDB = snapshot.child("descriere").value.toString()

                        val numeCurent = currNume.text.toString()
                        val prenumeCurent = currPrenume.text.toString()
                        val usernameCurent=currUsername.text.toString()

                        val adresaCurenta=currAdresa.text.toString()
                        val telefonCurent=currTelefon.text.toString()
                        val descriereCurenta=currDescriere.text.toString()

                        if(numeCurent!=numeRTDB && currNume.text!!.isNotEmpty())
                        {currentUser.child("nume").setValue(numeCurent)}
                        else if(currNume.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Numele nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(prenumeCurent!=prenumeRTDB && currPrenume.text!!.isNotEmpty())
                        {currentUser.child("prenume").setValue(prenumeCurent)}
                        else if(currPrenume.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Prenumele nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(usernameCurent!=usernameRTDB && currUsername.text!!.isNotEmpty())
                        {currentUser.child("username").setValue(usernameCurent)}
                        else if(currUsername.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Username-ul nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(adresaCurenta!=adresaRTDB && currAdresa.text!!.isNotEmpty())
                        {currentUser.child("address").setValue(adresaCurenta)}
                        else if(currAdresa.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Adresa nu poate fi goala !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(telefonCurent!=telefonRTDB && currTelefon.text!!.isNotEmpty())
                        {currentUser.child("phoneNumber").setValue(telefonCurent)}
                        else if(currTelefon.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Telefonul nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(descriereCurenta!=descriereRTDB && currDescriere.text!!.isNotEmpty())
                        {currentUser.child("descriere").setValue(descriereCurenta)}
                        else if(currDescriere.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Descrierea nu poate fi goala !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        Log.i("Data Accessed","Again")
                        if(nothingEmpty==0)
                            finish()
                    }
                    override fun onCancelled(error: DatabaseError) {
                       // Toast.makeText(this@EditProfilePetOwnerActivity,"Couldn't retrieve data: ${error.toException()}",Toast.LENGTH_LONG).show()
                        Log.w("FirebaseData", "Failed to read value.", error.toException())
                    }
                })
            }

        }
            .addOnFailureListener {
                currentUser.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val numeRTDB = snapshot.child("nume").value.toString()
                        val prenumeRTDB = snapshot.child("prenume").value.toString()
                        val usernameRTDB = snapshot.child("username").value.toString()

                        val adresaRTDB = snapshot.child("address").value.toString()
                        val telefonRTDB = snapshot.child("phoneNumber").value.toString()
                        val descriereRTDB = snapshot.child("descriere").value.toString()

                        val numeCurent = currNume.text.toString()
                        val prenumeCurent = currPrenume.text.toString()
                        val usernameCurent=currUsername.text.toString()

                        val adresaCurenta=currAdresa.text.toString()
                        val telefonCurent=currTelefon.text.toString()
                        val descriereCurenta=currDescriere.text.toString()

                        if(numeCurent!=numeRTDB && currNume.text!!.isNotEmpty())
                        {currentUser.child("nume").setValue(numeCurent)}
                        else if(currNume.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Numele nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(prenumeCurent!=prenumeRTDB && currPrenume.text!!.isNotEmpty())
                        {currentUser.child("prenume").setValue(prenumeCurent)}
                        else if(currPrenume.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Prenumele nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(usernameCurent!=usernameRTDB && currUsername.text!!.isNotEmpty())
                        {currentUser.child("username").setValue(usernameCurent)}
                        else if(currUsername.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Username-ul nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(adresaCurenta!=adresaRTDB && currAdresa.text!!.isNotEmpty())
                        {currentUser.child("address").setValue(adresaCurenta)}
                        else if(currAdresa.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Adresa nu poate fi goala !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(telefonCurent!=telefonRTDB && currTelefon.text!!.isNotEmpty())
                        {currentUser.child("phoneNumber").setValue(telefonCurent)}
                        else if(currTelefon.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Telefonul nu poate fi gol !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        if(descriereCurenta!=descriereRTDB && currDescriere.text!!.isNotEmpty())
                        {currentUser.child("descriere").setValue(descriereCurenta)}
                        else if(currDescriere.text!!.isEmpty())
                        {Toast.makeText(this@EditProfilePetOwnerActivity,"Descrierea nu poate fi goala !",Toast.LENGTH_SHORT).show()
                            nothingEmpty++}

                        Log.i("Data Accessed","Again")
                        if(nothingEmpty==0)
                            finish()
                    }
                    override fun onCancelled(error: DatabaseError) {
                       // Toast.makeText(this@EditProfilePetOwnerActivity,"Couldn't retrieve data: ${error.toException()}",Toast.LENGTH_LONG).show()
                        Log.w("FirebaseData", "Failed to read value.", error.toException())
                    }
                })
            }


    }

    private fun updateUserProfile(snapshot: DataSnapshot) {
        val numeRTDB = snapshot.child("nume").value.toString()
        val prenumeRTDB = snapshot.child("prenume").value.toString()
        val usernameRTDB = snapshot.child("username").value.toString()
        val emailRTDB = snapshot.child("email").value.toString()
        val adresaRTDB = snapshot.child("address").value.toString()
        val telefonRTDB = snapshot.child("phoneNumber").value.toString()
        val descriereRTDB=snapshot.child("descriere").value.toString()

        if(snapshot.child("nume").value!=null){
            editTextTitle.text=numeRTDB
            currNume.setText(numeRTDB)}

        if (snapshot.child("prenume").value!=null)
            currPrenume.setText(prenumeRTDB)

        if (snapshot.child("username").value!=null)
            currUsername.setText(usernameRTDB)

        //        if (snapshot.child("email").value!=null)
        //        currEmail.setText(snapshot.child("email").value.toString())
        if (snapshot.child("address").value!=null)
            currAdresa.setText(adresaRTDB)

        if (snapshot.child("phoneNumber").value!=null)
            currTelefon.setText(telefonRTDB)

        if (snapshot.child("descriere").value!=null)
            currDescriere.setText(descriereRTDB)

        Log.i("Data Accessed","Again")
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
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

        val newintent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
        with(newintent) {
            data = Uri.fromParts("package", context.packageName, null)
            addCategory(CATEGORY_DEFAULT)
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            addFlags(FLAG_ACTIVITY_NO_HISTORY)
            addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        startActivity(newintent)
    }

    private fun didDataChange(callback: (Boolean?)->Unit){
        var didDataChange = true
        val currentUser = databaseReference.child(mAuth1.currentUser?.uid.toString())
        currentUser.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val numeRTDB = snapshot.child("nume").value.toString()
                val prenumeRTDB = snapshot.child("prenume").value.toString()
                val usernameRTDB = snapshot.child("username").value.toString()

                val adresaRTDB = snapshot.child("address").value.toString()
                val telefonRTDB = snapshot.child("phoneNumber").value.toString()
                val descriereRTDB = snapshot.child("descriere").value.toString()

                val numeCurent = currNume.text.toString()
                val prenumeCurent = currPrenume.text.toString()
                val usernameCurent=currUsername.text.toString()

                val adresaCurenta=currAdresa.text.toString()
                val telefonCurent=currTelefon.text.toString()
                val descriereCurenta=currDescriere.text.toString()
                Log.i("didDataChange","$didDataChange")
                didDataChange= (numeCurent!=numeRTDB || prenumeCurent!=prenumeRTDB || usernameCurent!=usernameRTDB || adresaCurenta!=adresaRTDB
                        ||telefonCurent!=telefonRTDB || descriereCurenta!=descriereRTDB)

                callback(didDataChange)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
                //Toast.makeText(this@EditProfilePetOwnerActivity,"Couldn't retrieve data: ${error.toException()}",Toast.LENGTH_LONG).show()
            }})
    }

    override fun onBackPressed() {

        didDataChange{
            if(it==true)
            {
                AlertDialog.Builder(this)
                    .setTitle("Caution !")
                    .setMessage("If you go BACK now, you will lose all unsaved changes ! Are you sure ?")
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