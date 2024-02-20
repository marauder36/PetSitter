package com.example.firebasertdb.activities.PetOwner

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.firebasertdb.R
import com.example.firebasertdb.activities.authPart.SelectorActivity
import com.example.firebasertdb.databinding.ActivityMapsBinding
import com.example.firebasertdb.utils.Constants
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var permissionButton:Button
    private val ACCESS_LOCATION_REQUEST_CODE=41

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var prefType:String
    private lateinit var currentUser: DatabaseReference

    private lateinit var mapsBinding: ActivityMapsBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mainMapView:MapView
    private lateinit var mMap:GoogleMap
    private lateinit var userLatLng:LatLng
    private lateinit var currentUserLocationName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)


        initUI()
        initRTDB()

        mainMapView=findViewById(R.id.main_maps_view)
        mainMapView.onCreate(savedInstanceState)
        mainMapView.getMapAsync {googleMap->

            googleMap.uiSettings.isZoomControlsEnabled = true
            addMarkers(googleMap)
        }

        permissionButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissionLocation()
            }
            else{
                Toast.makeText(this@MapsActivity,"Permission Already Granted",Toast.LENGTH_SHORT).show()
                getLocation()
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        currentUser.child("address").get().addOnSuccessListener {
            Toast.makeText(this@MapsActivity,"Retrieved location ${it.value}",Toast.LENGTH_SHORT).show()
            currentUserLocationName= it.value.toString()
        }


    }

    private fun initUI(){
        permissionButton=findViewById(R.id.permissions_map_button)

    }

    private fun initRTDB(){
        mAuth = FirebaseAuth.getInstance()
        prefType = applicationContext.getSharedPreferences("PetMePrefs", Context.MODE_PRIVATE).getString("UserType","Petowner or PetOwner")!!
        database = FirebaseDatabase.getInstance(Constants.databaseURL)
        databaseReference = database.getReference(prefType)
        currentUser = databaseReference.child(mAuth.currentUser?.uid.toString())
    }

    private fun addMarkers(googleMap: GoogleMap){
        var cnt = 0
        val locations = mutableListOf(
            LatLng(37.4216548, -122.0856375),
            LatLng(38.4216548, -123.0856375),
            // Add as many LatLng objects as needed
        )
        for (location in locations) {
            cnt += 1
            googleMap.addMarker(MarkerOptions().position(location).title("Marker Title $cnt"))
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1234567
    }

    private fun requestPermissionLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_LOCATION_REQUEST_CODE)
                }.setNegativeButton(R.string.dialog_button_no) { dialog, _ ->
                    dialog.cancel()
                }.setTitle("Permission needed")
                .setMessage("This permission is needed for accessing your location")
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_LOCATION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE && grantResults.size > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this@MapsActivity,"Permission Granted",Toast.LENGTH_SHORT).show()
            getLocation()
        } else {

            AlertDialog.Builder(this)
                .setPositiveButton("Settings") {_, _ ->
                    goToSettingsForLocation(context = applicationContext)
                }.setNegativeButton("No thanks") {dialog, _ ->
                    dialog.cancel()
                }.setTitle("Go to settings ?")
                .setMessage("This permission is needed for accessing your current location." +
                        " Please allow it in the settings of this app.")
                .show()

            Toast.makeText(this,"Permission not granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun getLocation() {

        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                Toast.makeText(this@MapsActivity,"$location",Toast.LENGTH_SHORT).show()
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val list: MutableList<Address>? =
                        geocoder.getFromLocation(location.latitude,location.longitude, 1)
                    mapsBinding.apply {
                        tvLatitude.text = "Latitude\n${list!![0].latitude}"
                        tvLongitude.text = "Longitude\n${list!![0].longitude}"
                        tvCountryName.text = "Country Name\n${list!![0].countryName}"
                        tvLocality.text = "Locality\n${list!![0].locality}"
                        tvAddress.text = "Address\n${list!![0].getAddressLine(0)}"
                        userLatLng=LatLng(list!![0].latitude,list!![0].longitude)

                        val locationData = mapOf(
                            "latitude" to list[0].latitude,
                            "longitude" to list[0].longitude
                        )
                        currentUser.child("Location").setValue(locationData)
                            .addOnSuccessListener {
                                Toast.makeText(this@MapsActivity,"Saved location",Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@MapsActivity,"Failed to save location",Toast.LENGTH_SHORT).show()
                                Log.i("ErrorSavingLocation","${it}")
                            }
                    }
                }
            }
        }
        else {
            checkLocationSettings()
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
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
                    exception.startResolutionForResult(this@MapsActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(this, "Location turned on", Toast.LENGTH_LONG).show()
                    // Location settings enabled by the user.
                    // Proceed with location-related actions.
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "Location NOT turned on", Toast.LENGTH_LONG).show()
                    // User chose not to enable location services.
                    // Handle this scenario as needed.
                }
            }
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

        startActivity(newintent)
    }

    override fun onMapReady(googleMap: GoogleMap) {


        mMap = googleMap
        mMap.isMyLocationEnabled=true
        // Add a marker for the user's location and move the camera
        mMap.addMarker(MarkerOptions().position(userLatLng).title("User Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        mMap.setMapStyle(null)
    }

    override fun onStart() {
        val firstAuth=FirebaseAuth.getInstance()
        if(firstAuth.currentUser!=null){
            super.onStart()
            mainMapView.onStart()

        }
        else{
            startActivity(Intent(this@MapsActivity, SelectorActivity::class.java))
            finish()
        }

    }
    override fun onResume() {
        super.onResume()
        mainMapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mainMapView.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        mainMapView.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mainMapView.onLowMemory()
    }
    override fun onStop() {
        super.onStop()
        mainMapView.onStop()
    }


}