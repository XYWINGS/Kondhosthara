package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions

private lateinit var fusedLocationClient : FusedLocationProviderClient
private lateinit var locationsRequest: com.google.android.gms.location.LocationRequest
private lateinit var radiusTextView : TextView
private lateinit var sliderRadius : SeekBar
private lateinit var currentUserLocation : LatLng


class BusViewMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBusViewMapBinding
    private var arePermissionsGranted = false
    private var previousCircle: Circle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusViewMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sliderRadius = findViewById(R.id.sliderRadius)
        radiusTextView = findViewById(R.id.textViewRadius)
        val radiusValues = arrayOf(1.0, 5.0, 10.0, 25.0, 50.0)
        var selectedRadius =  radiusValues[2]

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
                circleHandler(selectedRadius)
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sriLankaLatLng = LatLng(7.8731, 80.7718)
        val zoomLevel = 8.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, zoomLevel))
        addCurrentLocationMarker()
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun addCurrentLocationMarker() {
        try {
            if (checkPermission()) {

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->

                        if (location != null) {

                            val currentLatLng = LatLng(location.latitude, location.longitude)

                            val markerOptions = MarkerOptions()
                                .position(currentLatLng)
                                .title("My Location") // Marker title
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Marker icon

                            mMap.addMarker(markerOptions)
                            currentUserLocation = currentLatLng

                            val zoomLevel = 10.0f
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    zoomLevel
                                )
                            )
                        }
                    }
            }else{
                requestPermissions()
            }
        }catch (e : Exception){
           Log.d("Error Occoured ","${e.message}")
        }
    }


    private fun circleHandler(circleRadiusInKm: Double) {
        if (currentUserLocation != null) {
            // Remove the previous circle if it exists
            previousCircle?.remove()

            val circleOptions = CircleOptions()
                .center(currentUserLocation)
                .radius(circleRadiusInKm * 1000) // Convert km to meters
                .strokeColor(Color.BLUE) // Circle border color
                .fillColor(Color.parseColor("#200000FF")) // Circle fill color (with transparency)

            // Add the new circle to the map
            val newCircle = mMap.addCircle(circleOptions)

            // Store a reference to the new circle as the previous circle
            previousCircle = newCircle
        }
    }
    private fun requestPermissions() {
        val permissionCode = 111
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),  permissionCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                arePermissionsGranted = true
                addCurrentLocationMarker()
            }
        }
    }
}