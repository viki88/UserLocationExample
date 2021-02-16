package com.vikination.userlocationsampleproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vikination.userlocationsampleproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding :ActivityMainBinding
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val arrayOfPermissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOfPermissions, 10)
            return
        }

        loadLastKnownLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            10 -> {
                if (grantResults.isNotEmpty() &&
                        grantResults.find { it == PackageManager.PERMISSION_DENIED } == null) {
                    Toast.makeText(this@MainActivity, "All Permisson is granted", Toast.LENGTH_SHORT).show()
                    loadLastKnownLocation()
                } else {
                    Toast.makeText(this@MainActivity, "You must granted all permissons in order to this function working properly ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadLastKnownLocation(){
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    binding.locationText.text = "Last Known Location of this device is \n(${it.latitude},${it.longitude})"
                }
    }
}