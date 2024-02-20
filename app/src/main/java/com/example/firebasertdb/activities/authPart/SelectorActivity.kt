package com.example.firebasertdb.activities.authPart

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.firebasertdb.MainActivity
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.PetSitter.activities.DashboardPetSitterActivity
import com.example.firebasertdb.activities.PetOwner.DashboardPetOwnerActivity
import com.example.firebasertdb.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SelectorActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var petOwner:Button
    private lateinit var petSitter:Button

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
        setContentView(R.layout.activity_selector)

        mAuth = FirebaseAuth.getInstance()


        petOwner = findViewById(R.id.pet_owner_button)
        petSitter= findViewById(R.id.pet_sitter)

        val sharedPreferences = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE)


        petOwner.setOnClickListener{

            val editor = sharedPreferences.edit()
            editor.putString("UserType","Petowner")
            editor.commit()
            Constants.type_select="Petowner"
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(Constants.type,"Petowner")
            startActivity(intent)
        }

        petSitter.setOnClickListener {

            val editor = sharedPreferences.edit()
            editor.putString("UserType","PetSitter")
            editor.commit()
            Constants.type_select="PetSitter"
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra(Constants.type,"PetSitter")
            startActivity(intent)
        }

    }
}