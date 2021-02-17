package com.vikination.userlocationsampleproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.vikination.userlocationsampleproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding :ActivityMainBinding
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var locationRequest: LocationRequest? = null
    lateinit var locationCallback: LocationCallback
    var requestingLocationUpdate = false

    private val arrayOfPermissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        /* checking permission location before get Last Known Location
            if All permissions is not granted show dialog to granted permissions by user
         */
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // show dialog if all permissons is not granted
                    ActivityCompat.requestPermissions(this, arrayOfPermissions, 10)
                    return
                }

        // call loadLastKnownLocation on onCreate()
//        loadLastKnownLocation()
        createLocationRequest()

        locationCallback = object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                requestingLocationUpdate = false
                p0 ?: return
                for (location in p0.locations){
                    // update UI
                    updateLocationResult(location)
                }
            }
        }
    }

    // after user granted/deny the permissions this function is invoked
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when(requestCode){
            // request code of checking permissions of location
            10 -> {
                // if all permission is granted show toast and get last known location
                if (grantResults.isNotEmpty() &&
                        grantResults.find { it == PackageManager.PERMISSION_DENIED } == null) {
                    Toast.makeText(this@MainActivity, "All Permisson is granted",
                        Toast.LENGTH_SHORT).show()
                    createLocationRequest()
                } else { // if one or all permissons is not granted show Toast
                    Toast.makeText(this@MainActivity,
                        "You must granted all permissons in order to this function working properly ",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationResult(location : Location){
        binding.locationText.text =
            "Last Known Location of this device is \n(${location.latitude},${location.longitude})"
    }

    // location request setting
    private fun createLocationRequest(){

        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }

        val client :SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> =
                client.checkLocationSettings(builder?.build())

        task.addOnSuccessListener {
            Toast.makeText(this, "All Location Setting is satisfied",
                    Toast.LENGTH_SHORT).show()
        }
        task.addOnFailureListener{exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@MainActivity, 20)
                }catch (sendEx : IntentSender.SendIntentException){

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        if (requestingLocationUpdate) stopLocationUpdate()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate(){
        requestingLocationUpdate = true
        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdate(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}