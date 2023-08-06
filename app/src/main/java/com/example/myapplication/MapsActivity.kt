package com.example.myapplication
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityMapsBinding
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.myapplication.R.*
import com.example.myapplication.R.id.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import java.util.*
import android.graphics.Color
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity :AppCompatActivity(),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locText: TextView
    private lateinit var locBtn: Button
    private var isJourneyStarted = false
    private var totalDistance = 0.0
    private var previousUpdateTime = 0L
    private var previousLocation: Location? = null

    private var PERMISSION_CODE = 111
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationsRequest: com.google.android.gms.location.LocationRequest
    private val journeyLocations: MutableList<Location> = mutableListOf()
    private var startLocationMarker: Marker? = null
    private var currentLocationMarker: Marker? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locText =  findViewById(R.id.locText)
        locBtn =  findViewById(R.id.locBtn)

        locBtn.setOnClickListener {
            if (!isJourneyStarted) {
                startJourney()
            } else {
                stopJourney()
            }


        }

    }

    private fun startJourney(){
        if (CheckPermission()) {
            if (isLocationEnabled()) {
                isJourneyStarted = true
                totalDistance = 0.0
                previousUpdateTime = 0L
                previousLocation = null
                journeyLocations.clear()
                getNewLocation()

            } else {
                Toast.makeText(this, "Please turn on the Location Service", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            RequestPermissions()
        }
    }

    private fun removeMarkers() {
        startLocationMarker?.remove()
        startLocationMarker = null
        currentLocationMarker?.remove()
        currentLocationMarker = null
    }
    private fun stopJourney() {
        isJourneyStarted = false
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        // Calculate total distance traveled
        if (journeyLocations.size >= 2) {
            for (i in 1 until journeyLocations.size) {
                val previousLocation = journeyLocations[i - 1]
                val currentLocation = journeyLocations[i]
                val distance = previousLocation.distanceTo(currentLocation) // in meters
                totalDistance += distance
            }
        }

        // Display the total distance
        locText.text = "Total Distance Traveled: ${String.format("%.2f", totalDistance)} meters"
    }




    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val currentLocation = p0.lastLocation
            if (currentLocation != null) {
                val cityName: String? = getCityName(currentLocation.latitude, currentLocation.longitude)
                locText.text =
                    "Lat : ${currentLocation.latitude} long : ${currentLocation.longitude} The city is $cityName"

                // Calculate distance between consecutive locations and update total distance
                if (isJourneyStarted && journeyLocations.isNotEmpty()) {
                    val previousLocation = journeyLocations.last()
                    val distance = previousLocation.distanceTo(currentLocation)
                    // EDIT HERE TO CHANGE THE SENSITIVITY OF THE TRAVELED DISTANCE USING GPS SERVICES

                    if (distance > 1000) {
                        totalDistance += distance
                    }
                }

                // Add current location to the journeyLocations list
                journeyLocations.add(currentLocation)
            }

            removeMarkers()
            val startLocation = journeyLocations.first()
            val stLat = startLocation.latitude
            val stLong = startLocation.longitude
            startLocationMarker = mMap.addMarker(
                MarkerOptions().position(LatLng(stLat, stLong))
                    .title("Start Location")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(stLat, stLong)))
        }

    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sriLankaLatLng = LatLng(7.8731, 80.7718)
        val zoomLevel = 8.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, zoomLevel))

    }

    private fun getCityName(lat: Double, long: Double): String {
        var cityName = "Not Found"
        val geoCoder = Geocoder(this, Locale.getDefault())
        val addressList = geoCoder.getFromLocation(lat, long, 1)
        if (addressList != null && addressList.isNotEmpty()) {
            val address = addressList[0]
            cityName = address.locality ?: "Not Found"
        }
        return cityName
    }


//    @SuppressLint("MissingPermission")
//    private fun locationDisplayer() {
//        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
//            var location: Location? = task.result
//            if (location == null) {
//                getNewLocation()
//                Toast.makeText(this, "Waiting for the location", Toast.LENGTH_LONG).show()
//            } else {
//                var cityName = getCityName(location.latitude,location.longitude)
//                locText.text = "Lat : ${location.latitude} long : ${location.longitude} The city is $cityName"
//            }
//        }
//    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        Toast.makeText(this, "Waiting for the location", Toast.LENGTH_LONG).show()
        locationsRequest = com.google.android.gms.location.LocationRequest()
        locationsRequest.priority =
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        locationsRequest.interval = 0
        locationsRequest.fastestInterval = 0
        locationsRequest.numUpdates = 200
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationsRequest, locationCallback, Looper.myLooper()
        )
    }


    private fun CheckPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {

        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_CODE
        )
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            (LocationManager.NETWORK_PROVIDER)
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug", "WE have the permissions----------------------------")
            }
        }
    }



}