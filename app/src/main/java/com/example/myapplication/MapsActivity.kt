package com.example.myapplication
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.R.*
import com.example.myapplication.R.id.*
import com.example.myapplication.databinding.ActivityMapsBinding
import com.example.myapplication.dataclasses.BusEarn
import com.example.myapplication.dataclasses.UserTripRecord
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.lang.Integer.parseInt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class MapsActivity :AppCompatActivity(),
    OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var textCurrentLocation: TextView
    private lateinit var textTravelDistance : TextView
    private var isJourneyStarted = false
    private var totalDistance = 0.0
    private var previousUpdateTime = 0L
    private var previousLocation: Location? = null
    private var PERMISSIONCODE = 111
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationsRequest: LocationRequest
    private val journeyLocations: MutableList<Location> = mutableListOf()
    private val APIKEY = "AIzaSyBtydB5hJ7sw4uFbMQOINK9N-5SCObh524"
    private lateinit var auth: FirebaseAuth
    private var hasRestarted = false
    private lateinit var textCurrentSpeed : TextView
    private lateinit var textCreditLeft : TextView
    private lateinit var textOrigin : TextView
    private lateinit var notifyExitBtn : FloatingActionButton
    private var journeyStartedTime : HashMap<String,Any> ?= null
    private var usersData : DataSnapshot ?= null
    private var currentWalletBalance : Int?=null
    private lateinit var ownerID: String
    private  var busData : DataSnapshot ? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
        }


        auth = Firebase.auth
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(ownerMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        textCurrentLocation =  findViewById(textViewUserCurrentLocation)
        textTravelDistance = findViewById(textViewUserDistantTravel)
        textCurrentSpeed = findViewById(textViewUserCurrentSpeed)
        textOrigin = findViewById(textViewUserOriginLocation)
        textCreditLeft = findViewById(textViewUserCreditLeft)
        notifyExitBtn = findViewById(fabUserNotifyExit)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        Places.initialize(applicationContext, APIKEY)

        notifyExitBtn.setOnClickListener {
            stopJourney()
        }

        val receivedIntent = intent
        if (receivedIntent != null && receivedIntent.hasExtra("OwnerID")) {
            ownerID = receivedIntent.getStringExtra("OwnerID").toString()
            Log.d("debug","owner uid $ownerID")
            intent.removeExtra("OwnerID")
        }else{
            ownerID = ""
                //EWsWokLQPKMOjq4Jd8ggIlP35S43
        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isTrafficEnabled = true
        if (checkPermission() && isLocationEnabled()){
            mMap.isMyLocationEnabled = true
        }else{
            requestPermissions()
        }
        val sriLankaLatLng = LatLng(7.8731, 80.7718)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, 15.0f))
        getUserData()
    }

    fun getCurrentDateTimeSnapshot(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    fun getCurrentDateSnapshot(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDateTime.format(formatter)
    }

    private fun startJourney(){
        if (checkPermission()) {
            if (isLocationEnabled()) {
                isJourneyStarted = true
                totalDistance = 0.0
                previousUpdateTime = 0L
                previousLocation = null
                journeyLocations.clear()
                getNewLocation()
                val currentTime = Calendar.getInstance()
                val hours = currentTime.get(Calendar.HOUR_OF_DAY)
                val minutes = currentTime.get(Calendar.MINUTE)
                journeyStartedTime  = hashMapOf(
                    "hours" to hours,
                    "minutes" to minutes
                )
            } else {
                Toast.makeText(this, "Please turn on the Location Service", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        Toast.makeText(this, "Waiting for the location", Toast.LENGTH_LONG).show()
        locationsRequest = LocationRequest()
        locationsRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY
        locationsRequest.interval = 7000
        locationsRequest.fastestInterval = 5000
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

                val cityName: String ?= getCityName(currentLocation.latitude, currentLocation.longitude)
                val speed = currentLocation.speed
                val speedKmph = (speed * 3.6).toInt()

                if (!cityName.isNullOrEmpty()){
                    textCurrentLocation.text = "Area name: $cityName"
                }else{
                    textCurrentLocation.text = "Not Available"
                }
                if (isJourneyStarted && journeyLocations.isNotEmpty()) {

                    val previousLocation = journeyLocations.last()
                    val distance = previousLocation.distanceTo(currentLocation)

                    if (distance > 1000) {
                        totalDistance += distance
                        textTravelDistance.text = totalDistance.toString()
                    }

                    textCurrentSpeed.text = "$speedKmph km/h"

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude,currentLocation.longitude), 15.0f))

                }else{
                    mMap.addMarker(
                    MarkerOptions().position(LatLng(currentLocation.latitude,currentLocation.longitude))
                        .title("Origin Point")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                }
                journeyLocations.add(currentLocation)
            }
        }
    }

    private fun getCityName(lat:Double,long:Double):String{
        var cityName ="Not Found"
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address : MutableList<Address>?= geoCoder.getFromLocation(lat,long,1)
        if (address != null) {
            cityName = address[0]?.locality.toString()
        }
        return  cityName
    }

    @SuppressLint("SetTextI18n")
    private fun stopJourney() {

        AlertDialog.Builder(this@MapsActivity)
            .setTitle("Confirm the exit")
            .setMessage("Ring the bell and exit ? ")
            .setPositiveButton("Yes") { _, _ ->

                isJourneyStarted = false
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                val lastLocation = journeyLocations.last()
                // Calculate total distance traveled
                if (journeyLocations.size >= 2) {
                    for (i in 1 until journeyLocations.size) {
                        val previousLocation = journeyLocations[i - 1]
                        val currentLocation = journeyLocations[i]
                        val distance = previousLocation.distanceTo(currentLocation) // in meters
                        totalDistance += distance
                    }
                }


                textTravelDistance.text = "Distance Traveled: ${String.format("%.2f", totalDistance/1000)} km"

                mMap.addMarker(
                    MarkerOptions().position(LatLng(lastLocation.latitude,lastLocation.longitude))
                        .title("Origin Point")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )


                stopHandler(calculateCost())

            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this@MapsActivity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .show()
    }


    @SuppressLint("SetTextI18n")
    private fun calculateCost() : Int{
        var finalCost = 20
        if (totalDistance > 0) {
            val disInKm = totalDistance / 1000
            finalCost += if (disInKm < 5) {
                40
            } else if (disInKm < 10) {
                90
            } else if (disInKm < 20) {
                120
            } else if (disInKm < 50) {
                140
            } else if (disInKm < 100) {
                200
            } else {
                (Integer.parseInt(totalDistance.toString())/ 1000) * 3
            }
            textOrigin.text = "Total Cost Rs:$finalCost"
            textCurrentSpeed.text = "Stopped"
        }
        return finalCost
    }

    @SuppressLint("SetTextI18n")
    private fun stopHandler(totCost : Int) {
        val userID = auth.currentUser!!.uid
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(userID)
        val walletBalance = currentWalletBalance?.minus(totCost)
        textCreditLeft.text = "Wallet Balance Rs$walletBalance"


        userReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val busID : String = dataSnapshot.child("busID").value.toString()

                    val updates = hashMapOf(
                        "busID" to "",
                        "status" to "idle",
                        "walletBalance" to walletBalance,
                        "distTravel" to totalDistance/1000
                    )
                    userReference.updateChildren(updates as Map<String, Any>)
                        .addOnSuccessListener {

                            if (busID.isNotEmpty()){

                                setUserTripRecord(userID, busID,totCost){ it1 ->
                                    if (it1){
                                        busNotify(busID){ it2 ->
                                            if (it2){
                                                updateDailyEarnings(totCost,busID){
                                                    if (it){
                                                        val intent = Intent(this@MapsActivity, PassengerHomeActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }else{
                                                       // forceBack()
                                                    }
                                                }
                                            }else{
                                               // forceBack()
                                            }
                                        }
                                    }else{
                                        //forceBack()
                                    }
                                }


                            }else{
                                Toast.makeText(this@MapsActivity, "Error Occurred. Try Again", Toast.LENGTH_SHORT).show()
                            }

                        }.addOnFailureListener {
                            Toast.makeText(this@MapsActivity, "Error Occurred ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@MapsActivity, "User data not Found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@MapsActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun forceBack() {
        Toast.makeText(this@MapsActivity, "Please Notify the Driver to Exit", Toast.LENGTH_LONG).show()
        val intent = Intent(
            this@MapsActivity,
           PassengerHomeActivity::class.java
        )
        this@MapsActivity.startActivity(intent)
        this@MapsActivity.finish()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun setUserTripRecord(userID: String, busID: String, totCost: Int, callback: (Boolean) -> Unit) {

        val origin :String ?=  getCityName(journeyLocations.first().latitude,journeyLocations.first().longitude)
        val destination :String ?=  getCityName(journeyLocations.last().latitude,journeyLocations.last().longitude)

        val currentTime = Calendar.getInstance()
        val hours = currentTime.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minutes = currentTime.get(Calendar.MINUTE)

        val  journeyEndedTimes :HashMap <String,Any> = hashMapOf(
            "hours" to hours,
            "minutes" to minutes
        )
            FirebaseDatabase.getInstance().reference
                .child("UserTrips")
                .child(userID)
                .child(getCurrentDateTimeSnapshot())
                .setValue(
                    UserTripRecord(
                        origin,
                        destination,
                        totalDistance.toString(),
                        totCost.toString(),
                        journeyStartedTime,
                        journeyEndedTimes,
                        busID
                    )
                )

                .addOnCompleteListener {
                    callback(true)
                }.addOnFailureListener {
                    Toast.makeText(
                        this@MapsActivity,
                        "Error Occurred ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
    }

    private fun busNotify(busID: String,  callback: (Boolean) -> Unit ) {

        FirebaseDatabase.getInstance().reference.child("BusNotify")
            .child(busID!!)
            .setValue(getCurrentDateTimeSnapshot())
            .addOnCompleteListener {

                Toast.makeText(this@MapsActivity, "Journey Completed. Driver Notified", Toast.LENGTH_LONG).show()
                callback(true)

            }.addOnFailureListener {
                callback(false)
            }
    }

    private fun updateDailyEarnings( earnVal: Int, busID: String,   callback: (Boolean) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val busEarnDataReference = databaseReference.child("BusEarnData").child(ownerID).child(busID)
        val currentDateSnapshot = getCurrentDateSnapshot()

        busEarnDataReference.child(currentDateSnapshot).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {

                val currentEarnings = dataSnapshot.child("earnVal").value.toString()
                val currentPassCount = dataSnapshot.child("userCount").value.toString()

                if (currentEarnings.isNotEmpty() && currentPassCount.isNotEmpty()) {
                    val newValue =  parseInt(currentEarnings) + earnVal
                    val newPassCount =  parseInt(currentPassCount) + 1


                    busEarnDataReference.child(currentDateSnapshot)
                        .setValue(BusEarn(newValue.toDouble(), newPassCount,false,ownerID,busID,currentDateSnapshot))
                        .addOnSuccessListener {
                            callback(true)
                        }.addOnFailureListener {
                            callback(false)
                        }
                }
            }else{
                busEarnDataReference.child(currentDateSnapshot).setValue(BusEarn(earnVal.toDouble(),1,false,ownerID,busID, currentDateSnapshot))
                    .addOnSuccessListener {
                        callback(true)
                        Log.d("debug","bus earn updated")
                    }.addOnFailureListener {
                        callback(false)
                    }
            }
        }
    }

    private fun getUserData() {
        val userID = auth.currentUser!!.uid
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(userID)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    startJourney()
                    usersData = dataSnapshot
                    currentWalletBalance = Integer.parseInt(dataSnapshot.child("walletBalance").value.toString())
                }else{
                    Toast.makeText(this@MapsActivity, "Error Occurred. Please try Again.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MapsActivity, PassengerHomeActivity::class.java)
                        startActivity(intent)
                        finish()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MapsActivity, "Error Occurred ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSIONCODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            (LocationManager.NETWORK_PROVIDER)
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONCODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (!hasRestarted) {
              //  restartActivity();
                hasRestarted = true
            }
            Log.d("debug", "----------------------------------location Permissions granted----------------------------------")
        }
    }

}

//val busReference = FirebaseDatabase.getInstance().reference.child("Buses").child(ownerID).child(busID!!)
//
//busReference.addListenerForSingleValueEvent(object : ValueEventListener {
//    override fun onDataChange(dataSnapshot: DataSnapshot) {
//        if (dataSnapshot.exists()){
//            busData = dataSnapshot
//        }
//    }
//
//    override fun onCancelled(error: DatabaseError) {
//        Toast.makeText(
//            this@MapsActivity,
//            "Update Bus D",
//            Toast.LENGTH_LONG
//        ).show()
//    }
//
//
//})


// line 214
//                        val currentTime = Calendar.getInstance()
//
//                        currentLocationMarker?.remove()
//
//                        currentLocationMarker = mMap.addMarker(MarkerOptions()
//                            .position(LatLng(currentLocation.latitude,currentLocation.longitude))
//                            .title("Current Location"))

//                        if (startTime == 0L) {
//                            startTime = currentTime.timeInMillis
//                        } else {
//                            endTime = currentTime.timeInMillis
//                            val elapsedTimeInSeconds =
//                                (endTime - startTime) / 1000.0 // Convert to seconds
//                            val speedMps =
//                                distance / elapsedTimeInSeconds // Speed in meters per second (m/s)
//                            val speedKmph =
//                                (speedMps * 3.6).toFloat()  // Convert to kilometers per hour (km/h)
//                            val decimalFormat = DecimalFormat("#.#")
//                            val lastResult =  decimalFormat.format(speedKmph).toFloat()
//                            if (speedKmph > 0){
//                                textCurrentSpeed.text = "$lastResult km/h"
//                            }
//
//                            startTime = endTime
//                        }



//    private fun routeHandler() {
//
//        val startLocation = journeyLocations.first()
//        Log.d("Map", "START LOC LAT ${startLocation.latitude}----------------------------------------------------------")
//        Log.d("Map", "START LOC LNG ${startLocation.latitude}----------------------------------------------------------")
//        Log.d("Map", "END LOC LAT ${endLocationLat}----------------------------------------------------------")
//        Log.d("Map", "EMD LOC LNG ${endLocationLng}----------------------------------------------------------")
//
//        val context = GeoApiContext.Builder()
//            .apiKey(APIKEY)
//            .queryRateLimit(3)
//            .connectTimeout(10, TimeUnit.SECONDS)
//            .readTimeout(10, TimeUnit.SECONDS)
//            .writeTimeout(10, TimeUnit.SECONDS)
//            .build()
//
//        val directionsResult: DirectionsResult = DirectionsApi.newRequest(context)
//            .mode(TravelMode.DRIVING) // Choose travel mode
//            .origin("${startLocation.latitude},${startLocation.longitude}")
//            .destination("$endLocationLat,$endLocationLng")
//            .await()
//
//        val route = directionsResult.routes[0].overviewPolyline.decodePath()
//
//        if (route != null){
//            // Convert DirectionsLatLng to Google Maps LatLng
//            val googleMapsLatLngList = route.map { directionsLatLng ->
//                LatLng(directionsLatLng.lat, directionsLatLng.lng)
//            }
//
//            // Draw the route on the map
//            val polylineOptions = PolylineOptions().addAll(googleMapsLatLngList)
//            mMap.addPolyline(polylineOptions)
//        }else{
//            Log.d("debug", "---------------------------------List is Empty----------------------------")
//        }
//    }
//


//
//        endLocationFragment =
//            supportFragmentManager.findFragmentById(id.endLocationFragment) as AutocompleteSupportFragment
//
//        endLocationFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
//            .setHint("Select the Journey Destination")
//            .setCountry("LK")
//
//        endLocationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
//
//            override fun onPlaceSelected(place: Place) {
//                endLocationLat = place.latLng!!.latitude
//                endLocationLng = place.latLng!!.longitude
//
//                addMarker(LatLng(endLocationLat,endLocationLng), "Destination")
//
//                mMap.addMarker(
//                    MarkerOptions().position(LatLng(endLocationLat,endLocationLng))
//                        .title("End Location")
//                )
//
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(place.latLng!!.latitude,place.latLng!!.longitude)))
//              //  routeHandler()
//            }
//
//            override fun onError(status: Status) {
//                Log.i(TAG, "An error occurred: $status---------------------------------------------------------------------------------------------------------------")
//            }
//        })
//
//
//
//        conBtn.setOnClickListener {
//            if (!isJourneyStarted) {
//                startJourney()
//
//            } else {
//                stopJourney()
//            }
//        }
//
//        logOutBtn.setOnClickListener {
//            Firebase.auth.signOut()
//            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            this.finish()
//        }



//private fun showCurrentLocation() {
//    if (!isstartmarkerset) {
//        if (checkPermission()) {
//            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//                if (location != null) {
//                    val currentLatLng = LatLng(location.latitude, location.longitude)
//                    if (currentLocationMarker == null) {
//                        addMarker(currentLatLng, "Current Location")
//                        journeyLocations.add(location)
//                        val cityName: String = getCityName(location.latitude,location.longitude)
//                        textOrigin.text = cityName
//                    } else {
//                        currentLocationMarker?.position = currentLatLng
//                        addMarker(currentLatLng, "Current Location")
//                    }
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 8.0f))
//                    isstartmarkerset = true
//                } else {
//                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
//                    requestPermissions()
//                }
//            }
//        } else {
//            requestPermissions()
//        }
//    }
//}

//private fun addMarker(latLng: LatLng, type: String) {
//    // Remove existing markers if they exist
//    if (type == "Destination") {
//        destinationMarker?.remove()
//        destinationMarker = mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))
//    } else if (type == "Origin") {
//        originMarker?.remove()
//        originMarker = mMap.addMarker(MarkerOptions().position(latLng).title("Origin"))
//    }
//}
