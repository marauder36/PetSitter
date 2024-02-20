package com.example.firebasertdb.activities.authPart

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.firebasertdb.R
import com.example.firebasertdb.models.DriverInfoModel
import com.example.firebasertdb.utils.Constants
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Arrays

class SplashScreenActivity : AppCompatActivity() {
    companion object {
        private val LOGIN_REQUEST_CODE = 214321
    }

    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    private lateinit var getResult: ActivityResultLauncher<Intent>

    private lateinit var userInfoRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)

        getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {

                }

            }

        init()

    }

    override fun onStart() {
        super.onStart()

        displaySplashScreen()

    }

    override fun onStop() {

        if (firebaseAuth != null && listener != null) {
            //oprim listener-ul sa nu mearga in fundal degeaba
            firebaseAuth.removeAuthStateListener(listener)
        }

        super.onStop()
    }

    private fun init() {

        database = FirebaseDatabase.getInstance(Constants.databaseURL)

        userInfoRef = database.getReference(Constants.DRIVER_INFO_REFERENCE)

        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null) {

                checkUserFromFirebase()

                Toast.makeText(this, "Welmcome: ${user.uid}", Toast.LENGTH_LONG).show()
            } else {
                showLoginLayout()
            }
        }

    }

    private fun checkUserFromFirebase() {

        userInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(
                            this@SplashScreenActivity,
                            "The user already exists",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showRegisterUserLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashScreenActivity, error.toString(), Toast.LENGTH_LONG)
                        .show()
                }

            })

    }

    private fun showRegisterUserLayout() {

        val builder = AlertDialog.Builder(this, R.style.DialogTheme)
        val itemview = LayoutInflater.from(this).inflate(R.layout.register_layout, null)

        val edit_text_name =
            itemview.findViewById<View>(R.id.edit_text_first_name) as TextInputEditText
        val edit_text_last_name =
            itemview.findViewById<View>(R.id.edit_text_last_name) as TextInputEditText
        val edit_text_phone_number =
            itemview.findViewById<View>(R.id.edit_text_phone_number) as TextInputEditText

        val btnContinue = itemview.findViewById<Button>(R.id.button_register)

        if (FirebaseAuth.getInstance().currentUser!!.phoneNumber == null &&
            !TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
        ) {
            edit_text_phone_number.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
        }

        builder.setView(itemview)
        val dialog = builder.create()
        dialog.show()

        btnContinue.setOnClickListener {
            if (TextUtils.isDigitsOnly(edit_text_name.text.toString())) {
                Toast.makeText(
                    this@SplashScreenActivity,
                    "Please enter first name",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener

            } else if (TextUtils.isDigitsOnly(edit_text_last_name.text.toString())) {
                Toast.makeText(
                    this@SplashScreenActivity,
                    "Please enter last name",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener

            } else if (TextUtils.isDigitsOnly(edit_text_phone_number.text.toString())) {

                return@setOnClickListener
            } else {

                val model = DriverInfoModel(
                    edit_text_name.text.toString(),
                    edit_text_last_name.text.toString(),
                    edit_text_phone_number.text.toString(),
                    0.0
                )
                userInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(model)
                    .addOnFailureListener {
                        Toast.makeText(
                            this@SplashScreenActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show(); dialog.dismiss()
                    }
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@SplashScreenActivity,
                            "Registered Successfully",
                            Toast.LENGTH_SHORT
                        ).show(); dialog.dismiss()
                    }
            }
        }

    }

    private fun showLoginLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.sign_in_layout)
            .setPhoneButtonId(R.id.button_phone_sign_in)
            .setGoogleButtonId(R.id.button_google_sign_in)
            .setEmailButtonId(R.id.button_user_pass_sign_in)
            .build()

        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setTheme(R.style.LoginTheme)
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        getResult.launch(signInIntent)
    }

    private fun displaySplashScreen() {
        Toast.makeText(this@SplashScreenActivity, "Splash screen done", Toast.LENGTH_SHORT).show()
        firebaseAuth.addAuthStateListener(listener)
    }
}