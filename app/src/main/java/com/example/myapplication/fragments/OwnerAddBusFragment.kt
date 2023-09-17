package com.example.myapplication.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.myapplication.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class OwnerAddBusFragment : Fragment() {
    private val APIKEY = "AIzaSyBtydB5hJ7sw4uFbMQOINK9N-5SCObh524"
//    val ownerUid:String? = null,
//    val busName:String? = null,
//    val routeID:String? = null,
//    val driverID:String? = null,
//    val driverName:String? = null,
//    val ownerPhn:String? = null,
//    val passngrCount : Int ? = null,
//    val crntLang:String? = null,
//    val crntLong:String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_owner_add_bus, container, false)
        this.context?.let { Places.initialize(it, APIKEY) }

        var startLocationFragment =
            childFragmentManager.findFragmentById(R.id.busRegStartLocation) as AutocompleteSupportFragment

        startLocationFragment.setHint("Select the Journey Origin")

        startLocationFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        var endLocationFragment =
            childFragmentManager.findFragmentById(R.id.busRegEndLocation) as AutocompleteSupportFragment
        endLocationFragment.setHint("Select the Journey Destination")

        endLocationFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

    endLocationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

        override fun onPlaceSelected(place: Place) {
           // endLocationLat = place.latLng!!.latitude
           // endLocationLng = place.latLng!!.longitude
            Toast.makeText(context,"Place is ${place.name} ", Toast.LENGTH_SHORT).show()
        }
        override fun onError(status: Status) {
            Log.i(ContentValues.TAG, "An error occurred: $status---------------------------------------------------------------------------------------------------------------")
        }
    })

    startLocationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

        override fun onPlaceSelected(place: Place) {
            // endLocationLat = place.latLng!!.latitude
            // endLocationLng = place.latLng!!.longitude
            Toast.makeText(context,"Place is ${place.name} ", Toast.LENGTH_SHORT).show()
        }
        override fun onError(status: Status) {
            Log.i(ContentValues.TAG, "An error occurred: $status---------------------------------------------------------------------------------------------------------------")
        }
    })











    return view

    }

}