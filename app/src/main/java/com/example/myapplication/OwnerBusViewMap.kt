package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityOwnerBusViewMapBinding
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class OwnerBusViewMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityOwnerBusViewMapBinding
    private val busDataList = mutableListOf<DataSnapshot>()
    private lateinit var auth: FirebaseAuth
    private val busMarkersList = mutableListOf<Marker>()
    private lateinit var refreshBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
        }
        binding = ActivityOwnerBusViewMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.ownerMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        auth = Firebase.auth
        refreshBtn = findViewById(R.id.btnRefreshMapOwner)
        refreshBtn.setOnClickListener {
            dataCaller()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        dataCaller()
        refreshBtn.visibility = View.VISIBLE
        val sriLankaLatLng = LatLng(7.8731, 80.7718)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, 8.0f))
    }

    private fun dataCaller(){
        getBusDetails{
            if (it){
                markerPrinter()
            }
        }
    }

    private fun clearMarkers() {
        for (marker in busMarkersList) {
            marker.remove()
        }
        busMarkersList.clear()
    }
    private fun markerPrinter() {

        val crntLat = 7.9
        val crntLong = 80.0
        clearMarkers()
        busDataList.forEach { busSnapshot ->

            val driverName = busSnapshot.child("driverName").value.toString()
            val crntLat = busSnapshot.child("currentLocation").child("latitude").value
            val crntLong = busSnapshot.child("currentLocation").child("longitude").value
            val busLocation = LatLng(crntLat as Double, crntLong as Double)
            val busID = busSnapshot.key.toString()

            val seatsLeft  =   Integer.parseInt(busSnapshot.child("seatCount").value.toString()) -  Integer.parseInt(
                busSnapshot.child("passngrCount").value.toString()
            )

            val newMarker = mMap.addMarker(MarkerOptions()
                .position(busLocation)
                .title("Bus ID : $busID")
                .snippet("Driver: $driverName Seats Left: $seatsLeft"))

            if (newMarker != null) {
                busMarkersList.add(newMarker)
            }

        }
    }

    private fun getBusDetails (callback: (Boolean) -> Unit) {
        val userID = auth.currentUser!!.uid
        val busReference = FirebaseDatabase.getInstance().reference.child("Buses").child(userID)
        busReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    busDataList.clear()
                    for (busSnapshot in dataSnapshot.children) {
                        busDataList.add(busSnapshot)
                        Log.d("debug","$busSnapshot")
                    }
                    callback(true)
                }else{
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@OwnerBusViewMap, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }
}