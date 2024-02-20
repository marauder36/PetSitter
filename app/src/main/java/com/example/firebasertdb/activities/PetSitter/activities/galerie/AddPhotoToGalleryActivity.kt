package com.example.firebasertdb.activities.PetSitter.activities.galerie

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
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


class AddPhotoToGalleryActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var uriRTDB: Any? = null

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var prefType:String
    private lateinit var currentUserGalerie: DatabaseReference

    private lateinit var imagineDeAdaugatInGalerie: ImageView
    private lateinit var addImagineBTN: Button

    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var uri: Uri
    private lateinit var storageRef: StorageReference
    private val STORAGE_REQUEST_CODE= 6969

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@AddPhotoToGalleryActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_add_photo_to_gallery)
        uri= Uri.parse("Placeholder")
        initUI()
        initToolbar()
        initRTDB()

        imagineDeAdaugatInGalerie.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermission()
            } else {
                getImage()
            }
        }

        getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    imagineDeAdaugatInGalerie.setImageURI(result.data?.data)
                    uri = result.data?.data!!
                }
            }

        addImagineBTN.setOnClickListener {

            if(this::uri.isInitialized){
                val filePath=storageRef.child("GalleryImages").child(uri.lastPathSegment!!)
                filePath.putFile(uri).addOnSuccessListener { task->
                    val result: Task<Uri> = task.metadata?.reference?.downloadUrl!!
                    result.addOnSuccessListener {
                        uri = it
                        //currentUserPets.child(numePet.text.toString()).child("imaginePet").setValue("$uri")
                        saveImageToRTDB()
                       // Toast.makeText(this@AddPhotoToGalleryActivity,"Image saved",Toast.LENGTH_LONG).show()
                    }
                        .addOnFailureListener {
                            //saveImageToRTDB()
                            //Toast.makeText(this@AddPhotoToGalleryActivity,"Error Downloading URI",Toast.LENGTH_LONG).show()
                            Log.i("Eroare la download URI","S-a cacat pe el") }

                }.addOnFailureListener {
                    //saveImageToRTDB()
                    //Toast.makeText(this@AddPhotoToGalleryActivity,"Eroare Salvare URI",Toast.LENGTH_LONG).show()
                    Log.i("Eroare la salvare URI","S-a cacat pe el")}
            }
            else{
                Log.i("Eroare la la initializare URI","S-a cacat pe el")
                //saveImageToRTDB()
                //Toast.makeText(this@AddPhotoToGalleryActivity,"Image not saved",Toast.LENGTH_LONG).show()
            }

        }


    }

    private fun initUI() {
        storageRef = FirebaseStorage.getInstance().reference
        imagineDeAdaugatInGalerie=findViewById(R.id.imagine_add_poza_to_gallery)
        addImagineBTN = findViewById(R.id.add_poza_to_gallery_button)
    }
    private fun initToolbar(){
        toolbar = findViewById(R.id.add_poza_to_gallery_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Adauga o imagine in galerie"
        }
    }
    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType", "Petowner or PetSitter")!!
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType)
        currentUserGalerie = databaseReference.child(mAuth.currentUser?.uid.toString()).child("Galerie")
    }

    private fun saveImageToRTDB(){
        currentUserGalerie.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var cnt = 0
                for (el in snapshot.children){cnt += 1}

                currentUserGalerie.child("Imagine${cnt+1}").child("uriImagine").setValue("$uri")
                    var intent = Intent()
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(
//                    this@AddPhotoToGalleryActivity,
//                    "Couldn't retrieve data: ${error.toException()}",
//                    Toast.LENGTH_LONG
//                ).show()
                Log.w("FirebaseData", "Failed to read value.", error.toException())
            }
        })
    }
    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        STORAGE_REQUEST_CODE)
                }.setNegativeButton(R.string.dialog_button_no) { dialog, _ ->
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
