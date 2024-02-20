package com.example.firebasertdb.activities.authPart

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import com.example.firebasertdb.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    private val ACCESS_LOCATION_REQUEST_CODE=41
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissionLocation()
        }
        else {
            Toast.makeText(this@SplashActivity, "Permission Already Granted", Toast.LENGTH_SHORT).show()
            Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe() {
                startActivity(Intent(this@SplashActivity, SelectorActivity::class.java))
                finish()
            }
        }
    }
    private fun requestPermissionLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_LOCATION_REQUEST_CODE)
                }.setNegativeButton(R.string.dialog_button_no) { dialog, _ ->
                    dialog.cancel()
                }.setTitle("Permission needed")
                .setMessage("This permission is needed for accessing your location")
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_LOCATION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE && grantResults.size > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this@SplashActivity,"Permission Granted",Toast.LENGTH_SHORT).show()
            Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe() {
                startActivity(Intent(this@SplashActivity, SelectorActivity::class.java))
                finish()}
        } else {
            AlertDialog.Builder(this)
                .setPositiveButton("Settings") {_, _ ->
                    goToSettingsForLocation(context = applicationContext)
                }.setNegativeButton("No thanks") {dialog, _ ->
                    dialog.cancel()
                    finishAffinity()
                }.setTitle("Go to settings ?")
                .setMessage("This permission is needed for accessing your current location." +
                        " Please allow it in the settings in order to use this app.")
                .show()

            Toast.makeText(this,"Permission not granted", Toast.LENGTH_LONG).show()
        }
    }
    private fun goToSettingsForLocation(context: Context){

        val newintent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        with(newintent) {
            data = Uri.fromParts("package", context.packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        startActivityForResult(newintent,ACCESS_LOCATION_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(this, "Permissions Granted", Toast.LENGTH_LONG).show()
                    // Location settings enabled by the user.
                    // Proceed with location-related actions.
                    Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe() {
                        startActivity(Intent(this@SplashActivity, SelectorActivity::class.java))
                        finish()}
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "Location NOT turned on, closing app", Toast.LENGTH_LONG).show()
                    // User chose not to enable location services.
                    // Handle this scenario as needed.
                    finish()
                }
            }
        }
    }
}