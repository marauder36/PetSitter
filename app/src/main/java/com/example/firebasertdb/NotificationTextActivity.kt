package com.example.firebasertdb

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.firebasertdb.activities.authPart.SplashActivity
import com.google.firebase.messaging.FirebaseMessaging

class NotificationTextActivity : AppCompatActivity() {

    private lateinit var sendNotificationButton:Button
    private lateinit var title:String
    private lateinit var message:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_text)

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this@NotificationTextActivity,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 86732)

            }
        }

        title="TestTitle"
        message="TestMessage"

        sendNotificationButton=findViewById(R.id.send_notification_button)
        sendNotificationButton.setOnClickListener {
            makeNotification()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(!it.isSuccessful){
                Log.i("ErrorRetrievingToken","$it")
                return@addOnCompleteListener
            }
            val token = it.result
            Log.d("FCM Token", token ?: "Token is null")
        }

    }
    private fun makeNotification(){

        val channelID = "CHANNEL_ID_NOTIFICATION"
        val channelName = "CHANNEL_NAME_NOTIFICATION"

        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("firstActivity","Ce vrem sa transmitem cand userul da click pe notificare")

        val pendingIntent = PendingIntent.getActivity(applicationContext,0,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelID)
            .setSmallIcon(R.drawable.paw_print_notification)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setContentTitle("PetMe")
            .setContentText("Georgica Muresan v-a trimis un mesaj")
        //builder = builder.setContent(getRemoteView(title,message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){

            val notificationChannel = notificationManager.getNotificationChannel(channelID)
            if(notificationChannel==null){

                val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)

            notificationManager.createNotificationChannel(notificationChannel)
            }

        }
        notificationManager.notify(0,builder.build())
    }


}