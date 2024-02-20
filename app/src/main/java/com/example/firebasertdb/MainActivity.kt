package com.example.firebasertdb

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.adapters.NumberPickerBindingAdapter.setValue
import com.example.firebasertdb.activities.PetSitter.activities.DashboardPetSitterActivity
import com.example.firebasertdb.activities.PetOwner.DashboardPetOwnerActivity
import com.example.firebasertdb.activities.authPart.LogInActivity
import com.example.firebasertdb.activities.authPart.RegisterActivity
import com.example.firebasertdb.databinding.ActivityMainBinding
import com.example.firebasertdb.utils.Constants
import com.example.firebasertdb.utils.Constants.RC_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {



    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth

    private val REQUEST_CHECK_LOCATION_IS_ON = 1234567

    private lateinit var databaseReference: DatabaseReference
    private val databaseUrl = "https://fir-rtdb-cc377-default-rtdb.europe-west1.firebasedatabase.app/"
    private lateinit var editTextName:EditText
    private lateinit var buttonAdd:Button
    private lateinit var spinnerGenres:Spinner

    private lateinit var gso:GoogleSignInOptions
    private lateinit var gsc:GoogleSignInClient

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        if (currentUser != null) {
            if(prefType!=null && prefType=="PetSitter")
            {
                val intent = Intent(applicationContext, DashboardPetSitterActivity::class.java)
                intent.putExtra(Constants.type, prefType)
                startActivity(intent)
                finish()
            }
            else if(prefType!=null && prefType=="Petowner"){
                val intent = Intent(applicationContext, DashboardPetOwnerActivity::class.java)
                intent.putExtra(Constants.type, prefType)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val user_type = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")
        //val user_type = Constants.type_select
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setupActionBarWithNavController(findNavController(R.id.fragmentContainerView))

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        gsc= GoogleSignIn.getClient(this,gso)

        mAuth = FirebaseAuth.getInstance()

        binding.displayNameFromCurrentUser.setText("Welcome $user_type")

        databaseReference = FirebaseDatabase.getInstance(databaseUrl).getReference(Constants.type_select)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initUi(user_type)

    }

    private fun initUi(user_type:String?){
        binding.loginButtonEmailPassword.setOnClickListener{
            var user=mAuth.currentUser
            if(user!=null){
               // Toast.makeText(this@MainActivity,"You are already signed in !",Toast.LENGTH_LONG).show()
            }else{
            val intent = Intent(applicationContext, LogInActivity::class.java)
            intent.putExtra(Constants.type,user_type)
            startActivity(intent)
            }
        }

        binding.registerButtonMain.setOnClickListener{
            var user=mAuth.currentUser
            if(user!=null){
                //Toast.makeText(this@MainActivity,"You are already signed in !",Toast.LENGTH_LONG).show()
            }else{
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            intent.putExtra(Constants.type,user_type)
            startActivity(intent)
            }
        }

//        binding.logoutButtonMain.setOnClickListener{
//            FirebaseAuth.getInstance().signOut()
//            Identity.getSignInClient(this).signOut()
//            val user=mAuth.currentUser
//            if(user==null){
//                Toast.makeText(this@MainActivity,"You have logged out !",Toast.LENGTH_SHORT).show()
//                val intent = Intent(applicationContext, SelectorActivity::class.java)
//                startActivity(intent)
//            }
//        }

        binding.loginButtonGoogle.setOnClickListener {
            var user=mAuth.currentUser
            if(user!=null){
                //Toast.makeText(this@MainActivity,"You are already signed in !",Toast.LENGTH_LONG).show()
            }else{
            gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.oauth_user_fake))
                .requestEmail().build()
            gsc= GoogleSignIn.getClient(this,gso)
            googleSignIn()}
        }
    }

    private fun googleSignIn(){
        val signInIntent = gsc.signInIntent
        startActivityForResult(signInIntent,RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG,"FirebaseAuthWithGoogle:"+account.id)
                firebaseAuthWithGoogle(account.idToken)
            }catch (e:ApiException){
                Log.w(ContentValues.TAG,"Google sign in failed",e)
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {

        val credentials= GoogleAuthProvider.getCredential(idToken,null)
        mAuth.signInWithCredential(credentials).addOnCompleteListener(this){task->

            if(task.isSuccessful){
                Log.d(ContentValues.TAG,"SignInWithCredential:SUCCESS")
                val user = mAuth.currentUser
                //TO DO("Sa adauge numele si alte detalii de pe contul de google in database sau sa facem link la conturi")
                updateui(user)
                getLocation(user)
            }else{
                Log.w(ContentValues.TAG,"SignInWithCredential:FAILED",task.exception)
                //updateui(null)
            }

        }

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getLocation(currentUser: FirebaseUser?) {

        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
               // Toast.makeText(this@MainActivity,"$location",Toast.LENGTH_SHORT).show()
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    try {
                        val list: MutableList<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        Log.i("ADDRESS","${list!![0].getAddressLine(0)}")
                        val locationData = mapOf(
                            "latitude" to list[0].latitude,
                            "longitude" to list[0].longitude
                        )
                        databaseReference.child(currentUser!!.uid).child("address").setValue(list[0].getAddressLine(0))
                        databaseReference.child(currentUser!!.uid).child("Location").setValue(locationData)
                            .addOnSuccessListener {
                                //Toast.makeText(this@MainActivity,"Saved location",Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                //Toast.makeText(this@MainActivity,"Failed to save location",Toast.LENGTH_SHORT).show()
                                Log.i("ErrorSavingLocation","${it}")
                            }
                    }
                    catch (e: IOException){
                        e.printStackTrace()
                    }
                }
            }
        }

        else {
            checkLocationSettings()
           // Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
//         val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//         startActivity(intent)
        }
    }
    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity, REQUEST_CHECK_LOCATION_IS_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun updateui(user: FirebaseUser?) {
        val prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetSitter")

        if(user!=null){
            databaseReference.child(user.uid).child("username").setValue("Username")
            databaseReference.child(user.uid).child("email").setValue(user.email)
            databaseReference.child(user.uid).child("google").setValue("yes")
            databaseReference.child(user.uid).child("type").setValue(Constants.type_select)
            databaseReference.child(user.uid).child("nume").setValue(user.displayName.toString().substringBefore(" "))
            databaseReference.child(user.uid).child("prenume").setValue(user.displayName.toString().substringAfter(" "))


            if (Constants.type_select=="PetSitter") {
                val intent = Intent(applicationContext, DashboardPetSitterActivity::class.java)
                intent.putExtra(Constants.EXTRA_NAME, user.displayName)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(applicationContext, DashboardPetOwnerActivity::class.java)
                intent.putExtra(Constants.EXTRA_NAME, user.displayName)
                startActivity(intent)
                finish()
            }
        }
    }



//    private fun getData(objectName: String){
//
//        databaseReference.child("$objectName").get().addOnSuccessListener {
//            Toast.makeText(this,"Got value ${it.value}",Toast.LENGTH_LONG).show()
//        }.addOnFailureListener{
//            Log.d("firebase", "Error getting data", it)
//        }
//    }
}