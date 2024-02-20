package com.example.firebasertdb.activities.authPart

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetSitter.activities.DashboardPetSitterActivity
import com.example.firebasertdb.activities.PetOwner.DashboardPetOwnerActivity
import com.example.firebasertdb.databinding.ActivityLogInBinding
import com.example.firebasertdb.utils.Constants
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth


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
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = Constants.type_select

        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference("${type}")
        mAuth = FirebaseAuth.getInstance()


        binding.signupRedirectText.setOnClickListener {
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            intent.putExtra(Constants.type, type)
            startActivity(intent)
            finish()
        }

        binding.loginBtn.setOnClickListener {
            var email = findViewById<EditText>(R.id.emailAddress_login).text.toString()
            var password = binding.passwordLogin.text.toString()

            if (email.isEmpty()) {
                binding.emailAddressLogin.setError("Email required")
                binding.emailAddressLogin.requestFocus()
            } else if (password.isEmpty()) {
                binding.passwordLogin.setError("Password required")
                binding.passwordLogin.requestFocus()
            } else
            {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val currentUserUID = mAuth.currentUser?.uid.toString()
                            databaseReference.child(currentUserUID).child("type").get().addOnSuccessListener {

                                if(it.value==Constants.type_select && it.value=="PetSitter"){
                                    hideKeyboard()
                                    Toast.makeText(this@LogInActivity, "Login Success", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(applicationContext, DashboardPetSitterActivity::class.java)
                                    intent.putExtra(Constants.type, type)
                                    startActivity(intent)
                                    finish()
                                }
                                else if(it.value==Constants.type_select && it.value=="Petowner"){
                                    hideKeyboard()
                                    Toast.makeText(this@LogInActivity, "Login Success", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(applicationContext, DashboardPetOwnerActivity::class.java)
                                    intent.putExtra(Constants.type, type)
                                    startActivity(intent)
                                    finish()
                                }
                                else{
                                    hideKeyboard()
                                    FirebaseAuth.getInstance().signOut()
                                    Identity.getSignInClient(this).signOut()
                                    Toast.makeText(this@LogInActivity, "Login Failed, account not a $type", Toast.LENGTH_LONG).show()
                                }
                            }
                                .addOnFailureListener {
                                    Toast.makeText(this@LogInActivity, "Login FAILED : ${it}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        else {
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

        }

    }
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}

