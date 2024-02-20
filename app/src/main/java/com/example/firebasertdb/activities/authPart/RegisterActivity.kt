package com.example.firebasertdb.activities.authPart

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetOwner.DashboardPetOwnerActivity
import com.example.firebasertdb.activities.PetSitter.activities.DashboardPetSitterActivity
import com.example.firebasertdb.databinding.ActivityRegisterBinding
import com.example.firebasertdb.models.HelperClass
import com.example.firebasertdb.utils.Constants
import com.firebase.ui.auth.util.data.PhoneNumberUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var currCoords: Map<String,Double>

    private var allOkay=0
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currCoords= mapOf("latitude" to 44.436181,
                          "longitude" to 26.0198499)

        val type = Constants.type_select

        mAuth = FirebaseAuth.getInstance()

        binding.loginRedirectText.setOnClickListener {
            val intent = Intent(applicationContext, LogInActivity::class.java)
            intent.putExtra(Constants.type, type)
            startActivity(intent)
        }

        binding.signupButton.setOnClickListener {
            allOkay=0

            var email = binding.emailAddress.text.toString()
            var password = binding.password.text.toString()
            var nume = binding.nume.text.toString()
            var prenume =binding.prenume.text.toString()
            var username = binding.username.text.toString()

            val zipCode = binding.codPostal.text.toString()
            val addressLocation=binding.adresa.text.toString()
            var address ="$addressLocation,$zipCode"

            var phoneNumber = binding.telefon.text.toString()
            var confirmPassword=binding.confirmPassword.text.toString()

            if (email.isEmpty()) {
                binding.emailAddress.setError("Te rugam introdu un email")
                binding.emailAddress.requestFocus()
                allOkay++
            }
            else if (password.isEmpty()) {
                binding.password.setError("Te rugam introdu o parola")
                binding.password.requestFocus()
                allOkay++
            }
            else if (confirmPassword.isEmpty()) {
                binding.confirmPassword.setError("Te rugam confirma-ti parola")
                binding.confirmPassword.requestFocus()
                allOkay++
            }
            else if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password!=confirmPassword) {
                binding.confirmPassword.setError("Parolele nu sunt identice")
                binding.confirmPassword.requestFocus()
                allOkay++
            }
            else if (username.isEmpty()) {
                binding.username.setError("Te rugam alege un username")
                binding.username.requestFocus()
                allOkay++
            }
            else if (nume.isEmpty()) {
                binding.nume.setError("Te rugam introdu numele")
                binding.nume.requestFocus()
                allOkay++
            }
            else if (prenume.isEmpty()) {
                binding.prenume.setError("Te rugam introdu prenumele")
                binding.prenume.requestFocus()
                allOkay++
            }
            else if (addressLocation.isEmpty()) {
                binding.adresa.setError("Te rugam introdu adresa")
                binding.adresa.requestFocus()
                allOkay++
            }
            else if (zipCode.isEmpty()) {
                binding.codPostal.setError("Te rugam introdu codul postal")
                binding.codPostal.requestFocus()
                allOkay++
            }
            else if(binding.codPostal.text.toString().toCharArray().size!=6){
                binding.codPostal.setError("Codul postal trebuie sa contina 6 cifre")
                binding.codPostal.requestFocus()
                allOkay++
            }
            else if (phoneNumber.isEmpty()) {
                binding.telefon.setError("Te rugam introdu nr de telefon")
                binding.telefon.requestFocus()
                allOkay++
            }
            else if (binding.telefon.text.toString().toCharArray().size!=10) {
                binding.telefon.setError("Te rugam introdu un nr valid de telefon")
                binding.telefon.requestFocus()
                allOkay++
            }


            if(addressLocation.isNotEmpty()){
                val geocoder = Geocoder(this, Locale.getDefault())

                try {
                    val addresses = geocoder.getFromLocationName(address,1)
                    if(addresses!=null && addresses.isNotEmpty()){
                        val validatedAddress = addresses[0].getAddressLine(0)
                        val locationData = mapOf(
                            "latitude" to addresses[0].latitude,
                            "longitude" to addresses[0].longitude
                        )
                        currCoords=locationData
                        address=validatedAddress

                        if(allOkay==0)
                        {
                            mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            "Te-ai inregistrat cu succes",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        database = FirebaseDatabase.getInstance(Constants.databaseURL)
                                        databaseReference = database.getReference(type!!)
                                        val user = HelperClass(nume = nume, prenume = prenume, username = username, email = email,   phoneNumber = phoneNumber, address = address,type=Constants.type_select)
                                        databaseReference.child(mAuth.currentUser!!.uid).setValue(user)
                                            .addOnSuccessListener {
                                                databaseReference.child(mAuth.currentUser!!.uid).child("Location").setValue(currCoords)
                                            }

                                        if(Constants.type_select=="PetSitter"){
                                            hideKeyboard()
                                            val intent = Intent(applicationContext, DashboardPetSitterActivity::class.java)
                                            intent.putExtra(Constants.type, type)
                                            startActivity(intent)
                                            finish()
                                        }
                                        else if (Constants.type_select=="Petowner"){
                                            hideKeyboard()
                                            val intent = Intent(applicationContext, DashboardPetOwnerActivity::class.java)
                                            intent.putExtra(Constants.type, type)
                                            startActivity(intent)
                                            finish()
                                        }

                                    } else {
                                        val inflater = layoutInflater
                                        val layout = inflater.inflate(R.layout.custom_toast_layout,null)
                                        val textView = layout.findViewById<TextView>(R.id.textViewToastMessage)
                                        textView.text = task.exception.toString().substringAfter(":")

                                        val toast = Toast(applicationContext)
                                        toast.duration = Toast.LENGTH_LONG
                                        toast.view = layout
                                        toast.show()
                                    }
                                }
                        }

                        Toast.makeText(this, "Adresa Validata: $validatedAddress", Toast.LENGTH_SHORT).show()
                        }
                    else{
                        Toast.makeText(this, "Adresa invalida, te rugam incearca din nou", Toast.LENGTH_SHORT).show()
                        allOkay++
                    }
                    }
                catch (e: Exception) {
                Toast.makeText(this, "Eroare la validarea adresei: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                    allOkay++
                }
            }



    }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        val type = intent.getStringExtra(Constants.type)
        if (currentUser != null) {
            val intent = Intent(applicationContext, DashboardPetSitterActivity::class.java)
            intent.putExtra(Constants.type, type)
            startActivity(intent)
            finish()
        }
    }

    fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration
            .setActivity(this)                 // Activity to handle code retrieval
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Verification completed automatically
                    // You can also use credential.smsCode to automatically fill verification code
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    allOkay++
                    Toast.makeText(this@RegisterActivity,
                        "Error validating phoneNumber: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    // Save the verification ID somewhere or prompt the user to input the code
                    // For example, store it in SharedPreferences or send it to the next activity
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyVerificationCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Phone number verified successfully
                    // Proceed with registration or other actions
                } else {
                    // Verification failed
                }
            }
    }
}