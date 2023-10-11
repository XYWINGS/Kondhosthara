package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.example.myapplication.databinding.ActivityDriverMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.lang.Integer.parseInt
import java.util.Calendar

class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverMapsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var driverData : DataSnapshot
    private lateinit var busData : DataSnapshot
    private lateinit var notifyData : DataSnapshot
    private  lateinit var  logOutBtn : Button
    private  lateinit var  endJourneyBtn : FloatingActionButton
    private  lateinit var  muteBtn : Button
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationsRequest: LocationRequest
    private var dataReceived = false
    private lateinit var notifyMessage : LinearLayout
    private val journeyLocations: MutableList<Location> = mutableListOf()
    private var totalDistance = 0.0
    private var startTime = 0
    private var endTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
        }

        binding = ActivityDriverMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapD) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        auth = Firebase.auth

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)

        muteBtn = findViewById(R.id.muteBtnDriver)
        notifyMessage = findViewById(R.id.driverNotifyMessage)
        logOutBtn = findViewById(R.id.driverMapLogout)
        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        endJourneyBtn = findViewById(R.id.fabEndJourneyDriver)

        getDriverData{ success ->
            if (success) {
                //   Log.d("Debug","Get Driver Data OK---------------------------------------------------")
                getBusData{ busSuccess ->
                    if (busSuccess) {
                        getBusStopNotification { notification ->
                            if (notification) {
                                dataReceived = true
                                // Log.d("Debug","Driver Notification recevied Successfully ---------------------------------------------------")
                                val newValue: String = notifyData.value.toString()
                                if (newValue =="Stop"){
                                    if (notifyMessage.visibility == View.GONE) {
                                        notifyMessage.visibility = View.VISIBLE
                                        val handler = Handler()
                                        handler.postDelayed({
                                            notifyMessage.visibility = View.GONE
                                            setNotifyStatus()
                                        }, 28000)
                                    }
                                    if (mediaPlayer != null) {
                                        if (!mediaPlayer!!.isPlaying) {
                                            mediaPlayer!!.start()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Operation Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } else {
                Toast.makeText(
                    this,
                    "Operation Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        muteBtn.setOnClickListener {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                }
            }
            if ( notifyMessage.visibility == View.VISIBLE ){
                notifyMessage.visibility = View.GONE
            }
        }

        endJourneyBtn.setOnClickListener {
            endDriverJourney()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isTrafficEnabled = true
        if (checkPermission()){
            mMap.isMyLocationEnabled = true
            getAndSetLocationUpdates()
            //  followCamera()
            val currentTime = Calendar.getInstance()
            startTime = currentTime.get(Calendar.HOUR_OF_DAY)
        }

        val lanka= LatLng(7.5, 80.9)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lanka, 15.0f))
    }
    @SuppressLint("MissingPermission")
    private fun getAndSetLocationUpdates() {
        Toast.makeText(this, "Waiting for the location", Toast.LENGTH_LONG).show()
        locationsRequest = LocationRequest()
        locationsRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY
        locationsRequest.interval = 12000
        locationsRequest.fastestInterval = 10000
        locationsRequest.numUpdates = 200
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        fusedLocationProviderClient.requestLocationUpdates(
            locationsRequest, locationCallback, Looper.myLooper()
        )

    }

    private val locationCallback = object : LocationCallback() {
        @SuppressLint("SuspiciousIndentation", "SetTextI18n")
        override fun onLocationResult(p0: LocationResult) {
            val currentLocation = p0.lastLocation
            if (currentLocation != null) {
                val userLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15.0f))
                updateBusData(userLocation)
                if (journeyLocations.isEmpty()){
                    journeyLocations.add(currentLocation)
                }else{
                    val previousLocation = journeyLocations.last()
                    val distance = previousLocation.distanceTo(currentLocation)
                    totalDistance += distance/1000
                }
                journeyLocations.add(currentLocation)

            }
        }
    }

    private fun setNotifyStatus() {
        val busReference =
            FirebaseDatabase.getInstance().reference.child("Buses")
                .child(driverData.child("ownerUid").value.toString())
                .child(driverData.child("busID").value.toString())

        busReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val updates = hashMapOf(
                        "status" to "go",
                    )
                    busReference.updateChildren(updates as Map<String, String>)

                } else {
                    Toast.makeText(
                        this@DriverMapsActivity,
                        "Bus Data not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DriverMapsActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    private fun endDriverJourney() {

        if (dataReceived) {
            val currentTime = Calendar.getInstance()
            val newTotalHrs =
                parseInt(driverData.child("drvHrs").value.toString()) + currentTime.get(Calendar.HOUR_OF_DAY) - startTime
            val newDistTravel =
                parseInt(driverData.child("distTravel").value.toString()) + totalDistance/1000
            val newBusDistTravel =
                parseInt(busData.child("distTravel").value.toString()) + totalDistance/1000

      //      Log.d("Debug","$newTotalHrs , $newBusDistTravel , $newDistTravel--------------------------------------------")

            endBus(newTotalHrs, newDistTravel) { success1 ->
                if (success1) {
                    endDriver(newBusDistTravel){ success2 ->
                        if (success2) {
                            val intent = Intent(this, DriverActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun endBus(newTotalHrs :  Int,newDistTravel : Double , callback: (Boolean) -> Unit){
        val busReference =
            FirebaseDatabase.getInstance().reference.child("Buses")
                .child(driverData.child("ownerUid").value.toString())
                .child(driverData.child("busID").value.toString())

        busReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val updates = hashMapOf(
                        "driverID" to "",
                        "isJourneyStarted" to false,
                        "drvHrs" to newTotalHrs,
                        "distTravel" to newDistTravel
                    )
                    busReference.updateChildren(updates as Map<String, Any>).addOnSuccessListener {
                        callback(true)
                    }

                } else {
                    Toast.makeText(
                        this@DriverMapsActivity,
                        "Bus Data not Found",
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

    private fun endDriver(newBusDistTravel : Double, callback: (Boolean) -> Unit){
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(auth.currentUser?.uid.toString())

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val updates = hashMapOf(
                        "busID" to "",
                        "status" to "idle",
                        "distTravel" to newBusDistTravel
                    )
                    userReference.updateChildren(updates as Map<String, Any>).addOnSuccessListener {
                        callback(true)
                    }

                } else {
                    Toast.makeText(
                        this@DriverMapsActivity,
                        "User data not Found",
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


    private fun updateBusData(crntloc : LatLng) {
        val busReference =
            FirebaseDatabase.getInstance().reference.child("Buses")
                .child(driverData.child("ownerUid").value.toString())
                .child(driverData.child("busID").value.toString())

       busReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val updates = hashMapOf(
                        "currentLocation" to crntloc,
                    )
                    busReference.updateChildren(updates as Map<String, LatLng>)

                } else {
                    Toast.makeText(
                        this@DriverMapsActivity,
                        "Bus Data not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DriverMapsActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

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
                 //   val newValue: String = dataSnapshot.value.toString()
                 //   Log.d("Debug", "Driver Notified Data Received $newValue")
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



//    private fun followCamera(){
//        if (checkPermission()) {
//            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//                if (location != null) {
//                    val userLocation = LatLng(location.latitude, location.longitude)
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 21.0f))
//                }
//            }
//
//            val locationCallback = object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult) {
//                    locationResult.lastLocation?.let { location ->
//                        val userLocation = LatLng(location.latitude, location.longitude)
//                        mMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation))
//                    }
//                }
//            }
//
//            val locationRequest = LocationRequest()
//            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            locationRequest.interval = 20000
//
//            fusedLocationProviderClient.requestLocationUpdates(
//                locationRequest, locationCallback, null
//            )
//        }
//    }