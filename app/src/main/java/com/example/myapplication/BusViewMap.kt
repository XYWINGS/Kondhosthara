package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.databinding.ActivityBusViewMapBinding
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
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class BusViewMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var currentUserLocation : LatLng
    private lateinit var radiusTextView : TextView
    private lateinit var sliderRadius : SeekBar
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBusViewMapBinding
    private var previousCircle: Circle? = null
    private val busDataList = mutableListOf<DataSnapshot>()
    private val busMarkersMap = mutableMapOf<String, Marker>()
    private var selectedRadius = 5.0
    private val busMarkersList = mutableListOf<Marker>()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationsRequest: LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
        }

         binding = ActivityBusViewMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        sliderRadius = findViewById(R.id.sliderRadius)
        radiusTextView = findViewById(R.id.textViewRadius)


        val radiusValues = arrayOf(1.0, 5.0, 10.0, 25.0, 50.0)
        selectedRadius =  radiusValues[1]

        sliderRadius.min = 1
        sliderRadius.max = radiusValues.size - 1

        sliderRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedRadius = radiusValues[progress]
                radiusTextView.text = "Search Radius: ${selectedRadius.toInt()} km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                radiusTextView.text = "Search Radius: ${selectedRadius.toInt()} km"

                if (currentUserLocation != null){
                    circleHandler(selectedRadius)
                }else{
                   startThings()
                }

            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sriLankaLatLng = LatLng(7.8731, 80.7718)
        val zoomLevel = 8.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, zoomLevel))

        startThings()

    }



    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val currentLocation = p0.lastLocation
            if (currentLocation != null) {
            val currentLLoc = LatLng(currentLocation.latitude,currentLocation.longitude)
                currentUserLocation =currentLLoc

                val markerOptions = MarkerOptions()
                    .position( currentLLoc)
                    .title("My Location")
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                mMap.addMarker(markerOptions)

                val zoomLevel = 8.0f
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLLoc,
                        zoomLevel
                    )
                )

            }
        }
    }


    private fun startThings(){
        if (checkPermission()) {
            if (isLocationEnabled()) {
                getNewLocation()
            } else {
                Toast.makeText(this, "Please turn on the Location Service", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            (LocationManager.NETWORK_PROVIDER)
        )
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        Toast.makeText(this, "Please wait for the location", Toast.LENGTH_LONG).show()
        locationsRequest = LocationRequest()
        locationsRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY
        locationsRequest.interval = 10000
        locationsRequest.fastestInterval = 5000
        locationsRequest.numUpdates = 200
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationsRequest, locationCallback, Looper.myLooper()
        )
    }

    private fun checkPermission(): Boolean { return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED }

    private fun requestPermissions() {
        val permissionCode = 111
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),  permissionCode
        )
    }


    private fun circleHandler(circleRadiusInKm: Double) {

        // Remove the previous circle if it exists
        previousCircle?.remove()
        val circleOptions = CircleOptions()
            .center(currentUserLocation)
            .radius(circleRadiusInKm * 1000) // Convert km to meters
            .strokeColor(Color.BLUE) // Circle border color
            .fillColor(Color.parseColor("#200000FF")) // Circle fill color (with transparency)

        val newCircle = mMap.addCircle(circleOptions)
        previousCircle = newCircle
        getBusDetails()

    }

    private fun getBusDetails() {
        val busReference = FirebaseDatabase.getInstance().reference.child("Buses")
        busReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                busDataList.clear()
                for (ownerSnapshot in dataSnapshot.children) {
                    for (busSnapshot in ownerSnapshot.children) {
                        val destinationLat = busSnapshot.child("endLocation").child("latLng").child("latitude").value
                        val destinationLong = busSnapshot.child("endLocation").child("latLng").child("longitude").value

                        if (destinationLat != null && destinationLong != null) {
                            busDataList.add(busSnapshot)
                        }
                    }
                }
                passDataToFilter()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BusViewMap, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun passDataToFilter() {

        removeAllMarkers()

        busDataList.forEach { busSnapshot ->
            val destinationLat = busSnapshot.child("endLocation").child("latLng").child("latitude").value
            val destinationLong = busSnapshot.child("endLocation").child("latLng").child("longitude").value
            val busLocation = LatLng(destinationLat as Double, destinationLong as Double)
            val busID = busSnapshot.key.toString()
            val destination = busSnapshot.child("journeyStatus").value.toString()
            val userLoc = currentUserLocation
            val distance = areLatLngsWithinRadius(busLocation,userLoc)
            val seatsLeft  =   Integer.parseInt(busSnapshot.child("seatCount").value.toString()) -  Integer.parseInt(
                busSnapshot.child("passngrCount").value.toString()
            )

            if (distance  <= (selectedRadius*1000)){
                addMarker(busLocation , busID, distance , destination ,seatsLeft)
            }
        //    busDataTextView.text = destinationLong.toString()
        }
    }

    private fun areLatLngsWithinRadius(latLngA: LatLng, currentUserLocation: LatLng ): Double {
        val earthRadius = 6371000.0

        val lat1 = Math.toRadians(latLngA.latitude)
        val lon1 = Math.toRadians(latLngA.longitude)

        val lat2 = Math.toRadians(currentUserLocation.latitude)
        val lon2 = Math.toRadians(currentUserLocation.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = earthRadius * c

        return distance
    }


    private fun removeAllMarkers() {
        for (marker in busMarkersList) {
            marker.remove()
        }
        busMarkersList.clear()
    }

    private fun addMarker(
        latLng: LatLng,
        busID: String,
        distance: Double,
        destination: String,
        seatsLeft: Int
    ) {

        val decimalFormat = DecimalFormat("#.#")

        val lastResult =  decimalFormat.format(distance).toFloat() /1000

        val finalResult =  decimalFormat.format(lastResult).toFloat()

        val newMarker = mMap.addMarker(MarkerOptions()
            .position(latLng)
            .title("Bus ID : $busID")
            .snippet("GoingTo:$destination Seats Left:$seatsLeft Distance:$finalResult km"))
        if (newMarker != null) {
            busMarkersMap[busID] = newMarker
            busMarkersList.add(newMarker)
        }
    }

}



