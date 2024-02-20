package com.example.firebasertdb.activities.PetSitter.activities.galerie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class PozaFullScreenActivity : AppCompatActivity() {

    private lateinit var toolbar:Toolbar
    private lateinit var imagineFullScreen:ImageView
    private var pozaCurr:String?=null

    override fun onStart() {
        val firstAuth = FirebaseAuth.getInstance()
        if (firstAuth.currentUser != null) {
            super.onStart()
        } else {
            startActivity(Intent(this@PozaFullScreenActivity, SelectorActivity::class.java))
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
        setContentView(R.layout.activity_poza_full_screen)

        pozaCurr=intent.getStringExtra(Constants.poza_aleasa)
        Log.i("Am primit poza","$pozaCurr")
        initUi()
        initToolbar(toolbar)
        if(pozaCurr!=null)
        Picasso.get().load(pozaCurr).into(imagineFullScreen)
    }

    private fun initUi(){
        imagineFullScreen=findViewById(R.id.full_screen_image)
        toolbar=findViewById(R.id.full_screen_toolbar)
    }
    private fun initToolbar(toolbar: Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Galerie"
        }
    }
}