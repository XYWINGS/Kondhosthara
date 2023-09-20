package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityBusViewMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private lateinit var fusedLocationClient : FusedLocationProviderClient
private lateinit var locationsRequest: com.google.android.gms.location.LocationRequest
private lateinit var radiusTextView : TextView
private lateinit var busDataTextView : TextView
private lateinit var sliderRadius : SeekBar
private lateinit var currentUserLocation : LatLng
private lateinit var logOutBtn : Button

class BusViewMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBusViewMapBinding
    private var arePermissionsGranted = false
    private var previousCircle: Circle? = null
    private val busDataList = mutableListOf<DataSnapshot>()
    private val busMarkersMap = mutableMapOf<String, Marker>()
    private var selectedRadius = 5.0
    private var shouldAddCurrentLocationMarker = false
    private val busMarkersList = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
        }

         binding = ActivityBusViewMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)


        sliderRadius = findViewById(R.id.sliderRadius)
        radiusTextView = findViewById(R.id.textViewRadius)
        logOutBtn = findViewById(R.id.viewBusLogout)
        busDataTextView = findViewById(R.id.textViewBusData)

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
                addCurrentLocationMarker(selectedRadius)

            }
        })

        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sriLankaLatLng = LatLng(7.8731, 80.7718)
        val zoomLevel = 8.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, zoomLevel))

        if (shouldAddCurrentLocationMarker) {
            addCurrentLocationMarker(5.0)
        }
    }

    private fun checkPermission(): Boolean { return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED }

    private fun requestPermissions() {
        val permissionCode = 111
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),  permissionCode
        )
    }

    private fun addCurrentLocationMarker(circleRadiusInKm: Double) {
        try {
            if (checkPermission()) {
               // Toast.makeText(this@BusViewMap, "Im here ", Toast.LENGTH_SHORT).show()
                Log.d("location","Location called ----------------------------------------------------------------------------",)
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        Log.d("location","Location recevied-------------------------------------------------------",)
                        if (location != null) {

                            Log.d("location","Location is $location -------------------------------------------------------",)
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            val markerOptions = MarkerOptions()
                                .position(currentLatLng)
                                .title("My Location") // Marker title
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Marker icon

                            mMap.addMarker(markerOptions)
                            currentUserLocation = currentLatLng
                    //        Toast.makeText(this@BusViewMap, "Im here ", Toast.LENGTH_SHORT).show()
                            circleHandler(circleRadiusInKm)


                            val zoomLevel = 10.0f
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    zoomLevel
                                )
                            )
                        }
                    }.addOnFailureListener { e ->
                        Log.e("LocationError", "Error getting location: ${e.message}")
                        // Handle location error
                    }
            }else{
                requestPermissions()
            }
        }catch (e : Exception){
            Log.d("Error Occoured ","${e.message}")
        }
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
            val  userLoc = currentUserLocation


            if (areLatLngsWithinRadius(busLocation,userLoc)){
                addMarker(busLocation , busID)
            }
        //    busDataTextView.text = destinationLong.toString()
        }
    }

    fun areLatLngsWithinRadius(latLngA: LatLng, currentUserLocation: LatLng ): Boolean {
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

        return distance <= (selectedRadius*1000)
    }


    private fun removeAllMarkers() {
        for (marker in busMarkersList) {
            marker?.remove()
        }
        busMarkersList.clear()
    }


    private fun addMarker(latLng: LatLng, busID: String) {

        Log.d("MArkers","Markers are $busMarkersList")
        val newMarker = mMap.addMarker(MarkerOptions().position(latLng).title("Bus ID : $busID"))
        if (newMarker != null) {
            busMarkersMap[busID] = newMarker
            busMarkersList.add(newMarker)
        }
    }

}