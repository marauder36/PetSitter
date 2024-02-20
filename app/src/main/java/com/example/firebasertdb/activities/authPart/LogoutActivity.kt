package com.example.firebasertdb.activities.authPart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.firebasertdb.R
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth

class LogoutActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().signOut()
        Identity.getSignInClient(this).signOut()
        startActivity(Intent(this@LogoutActivity, SelectorActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
    }
}