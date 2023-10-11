package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityDriverMapsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverMapsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var driverData : DataSnapshot
    private lateinit var busData : DataSnapshot
    private lateinit var notifyData : DataSnapshot
    private  lateinit var  logOutBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapD) as SupportMapFragment
        mapFragment.getMapAsync(this)

        logOutBtn = findViewById(R.id.driverMapLogout)
        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isTrafficEnabled = true
        if (checkPermission()){
            mMap.isMyLocationEnabled = true
        }

        val lanka= LatLng(7.5, 80.9)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lanka, 10.0f))
    }
    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun getDriverData(callback: (Boolean) -> Unit){
        val userID = auth.currentUser?.uid
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(userID.toString())

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    driverData = dataSnapshot
                    callback(true)
                } else {
                    Toast.makeText(
                        this@DriverMapsActivity,
                        "Error Occurred.Try Again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DriverMapsActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(false)
            }
        })
    }
    private fun getBusData(callback: (Boolean) -> Unit){

        val busReference = FirebaseDatabase.getInstance().reference.child("Buses").child(driverData.child("ownerUid").value.toString()).child(driverData.child("busID").value.toString())
        busReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    busData = dataSnapshot
                    callback(true)
                } else {
                    Toast.makeText(
                        this@DriverMapsActivity,
                        "Error Occurred.Try Again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DriverMapsActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }
    private fun getBusStopNotification(callback: (Boolean) -> Unit) {
        val busNotiReference =
            FirebaseDatabase.getInstance().reference.child("BusNotify")
                .child(driverData.child("busID").value.toString())

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    notifyData = dataSnapshot

                    val newValue: String = dataSnapshot.value.toString()
                    Log.d("Debug", "Driver Notified Data Received $newValue")
                    callback(true)

                } else {
                    Toast.makeText(
                        this@DriverMapsActivity,
                        "Error Occurred. Try Again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DriverMapsActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }

        busNotiReference.addValueEventListener(valueEventListener)
    }

}