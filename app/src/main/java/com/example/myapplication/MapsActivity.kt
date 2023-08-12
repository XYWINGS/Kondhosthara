package com.example.myapplication
import android.Manifest
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
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.myapplication.R.*
import com.example.myapplication.R.id.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import java.util.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import java.util.concurrent.TimeUnit
import com.google.maps.model.LatLng as DirectionsLatLng


class MapsActivity :AppCompatActivity(),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locText: TextView
    //private lateinit var locBtn: Button
    private lateinit var conBtn: Button
    private lateinit var qrGenBtn: Button
    private lateinit var qrReadtn: Button
    private var isJourneyStarted = false
    private var isstartmarkerset = false
    private var totalDistance = 0.0
    private var previousUpdateTime = 0L
    private var previousLocation: Location? = null

    private var PERMISSIONCODE = 111
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationsRequest: com.google.android.gms.location.LocationRequest
    private val journeyLocations: MutableList<Location> = mutableListOf()
    private var startLocationMarker: Marker? = null
    private var currentLocationMarker: Marker? = null

   // private lateinit var startLocationFragment: AutocompleteSupportFragment
    private lateinit var endLocationFragment: AutocompleteSupportFragment

    private var startLocationLatLng  :LatLng? = null
    private var endLocationLat  :Double = 0.0
    private var endLocationLng  :Double = 0.0

    private val APIKEY = "AIzaSyBtydB5hJ7sw4uFbMQOINK9N-5SCObh524"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locText =  findViewById(id.locText)
        qrGenBtn = findViewById(id.qrGenBtn)
        qrReadtn = findViewById(id.qrReadBtn)
        conBtn =  findViewById(id.conBtn)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        Places.initialize(applicationContext, APIKEY)

//        startLocationFragment =
//            supportFragmentManager.findFragmentById(id.startLocationFragment) as AutocompleteSupportFragment

        endLocationFragment =
            supportFragmentManager.findFragmentById(id.endLocationFragment) as AutocompleteSupportFragment



        // Set up PlaceSelectionListener for end location fragment
        endLocationFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        endLocationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onPlaceSelected(place: Place) {
                endLocationLat = place.latLng!!.latitude
                endLocationLng = place.latLng!!.longitude

                mMap.addMarker(
                    MarkerOptions().position(LatLng(endLocationLat,endLocationLng))
                        .title("End Location")
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(place.latLng!!.latitude,place.latLng!!.longitude)))
                routeHandler()
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status---------------------------------------------------------------------------------------------------------------")
            }
        })


        conBtn.setOnClickListener {
            if (!isJourneyStarted) {
                startJourney()
                Log.i(TAG, "An error occurred: ---------------------------------------------------------------------------------------------------------------")
            } else {
                stopJourney()
            }
        }

        qrGenBtn.setOnClickListener {
            val intent = Intent(this, QRGenerateActivity::class.java)
            startActivity(intent)
        }

        qrReadtn.setOnClickListener {
            val intent = Intent(this, QRReadActivity::class.java)
            startActivity(intent)
        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sriLankaLatLng = LatLng(7.8731, 80.7718)
        val zoomLevel = 8.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, zoomLevel))
        showCurrentLocation()
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

        }

    }

    private fun routeHandler() {
        val startLocation = journeyLocations.first()
        Log.d("Map", "START LOC LAT ${startLocation.latitude}----------------------------------------------------------")
        Log.d("Map", "START LOC LNG ${startLocation.latitude}----------------------------------------------------------")
        Log.d("Map", "END LOC LAT ${endLocationLat}----------------------------------------------------------")
        Log.d("Map", "EMD LOC LNG ${endLocationLng}----------------------------------------------------------")


        val context = GeoApiContext.Builder()
            .apiKey(APIKEY)
            .queryRateLimit(3)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val directionsResult: DirectionsResult = DirectionsApi.newRequest(context)
            .mode(TravelMode.DRIVING) // Choose travel mode
            .origin("${startLocation.latitude},${startLocation.longitude}")
            .destination("$endLocationLat,$endLocationLng")
            .await()

        val route = directionsResult.routes[0].overviewPolyline.decodePath()

        // Convert DirectionsLatLng to Google Maps LatLng
        val googleMapsLatLngList = route.map { directionsLatLng ->
            com.google.android.gms.maps.model.LatLng(directionsLatLng.lat, directionsLatLng.lng)
        }

        // Draw the route on the map
        val polylineOptions = PolylineOptions().addAll(googleMapsLatLngList)
        mMap.addPolyline(polylineOptions)
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

            } else {
                Toast.makeText(this, "Please turn on the Location Service", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            requestPermissions()
        }
    }


    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        Toast.makeText(this, "Waiting for the location", Toast.LENGTH_LONG).show()
        locationsRequest = com.google.android.gms.location.LocationRequest()
        locationsRequest.priority =
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        locationsRequest.interval = 3000
        locationsRequest.fastestInterval = 2000
        locationsRequest.numUpdates = 200
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationsRequest, locationCallback, Looper.myLooper()
        )
    }

    private fun getCityName(lat:Double,long:Double):String{
        var cityName ="Not Found"
        var geoCoder = Geocoder(this, Locale.getDefault())
        var address = geoCoder.getFromLocation(lat,long,1)
        if (address != null) {
            cityName = address.get(0)?.locality.toString()
        }
        return  cityName
    }

    private fun showCurrentLocation() {
        if (!isstartmarkerset) {
            if (checkPermission()) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        if (currentLocationMarker == null) {
                            currentLocationMarker = mMap.addMarker(
                                MarkerOptions().position(currentLatLng).title("Current Location")
                            )
                        } else {
                            currentLocationMarker?.position = currentLatLng
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 8.0f))
                        isstartmarkerset = true
                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                        requestPermissions()
                    }
                }
            } else {
                requestPermissions()
            }
        }
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

    private fun restartActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONCODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            restartActivity()
            Log.d("debug", "----------------------------------location Permissions granted----------------------------------")
        }
    }


}