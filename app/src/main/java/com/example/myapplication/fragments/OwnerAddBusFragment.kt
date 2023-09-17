package com.example.myapplication.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.dataclasses.Bus
import com.example.myapplication.dataclasses.Driver
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class OwnerAddBusFragment : Fragment() {
    private val APIKEY = "AIzaSyBtydB5hJ7sw4uFbMQOINK9N-5SCObh524"
    private lateinit var auth: FirebaseAuth
    private var startLocation  :Place? = null
    private var endLocation  :Place? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        val user = auth.currentUser
            // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_owner_add_bus, container, false)
        this.context?.let { Places.initialize(it, APIKEY) }

        val busNme = view.findViewById<EditText>(R.id.editTextBusName)
        val busRegID = view.findViewById<EditText>(R.id.editTextBusRegistrationID)
        val busPermID = view.findViewById<EditText>(R.id.editTextBusRoutePermitID)
        val busRouteNum = view.findViewById<EditText>(R.id.editTextBusRouteNumber)

        val busRegBtn = view.findViewById<Button>(R.id.busRegBtn)
        val testValBtn = view.findViewById<Button>(R.id.buttonTestValueAddBus)

        val startLocationFragment = childFragmentManager.findFragmentById(R.id.busRegStartLocation) as AutocompleteSupportFragment
        startLocationFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
            .setHint("Select the Journey Origin")
            .setCountry("LK")


        val endLocationFragment = childFragmentManager.findFragmentById(R.id.busRegEndLocation) as AutocompleteSupportFragment
        endLocationFragment.setHint("Select the Journey Destination")
            .setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
            .setCountry("LK")



    busRegBtn.setOnClickListener{
        val busOwnerID =user!!.uid
        val busRouteNumber = busRouteNum.text.toString()
        val busPermitID = busPermID.text.toString()
        val busRegNumber =  busRegID.text.toString()
        val busName = busNme.text.toString()

        if(validation(busName,busRegNumber,busPermitID,busRouteNumber)){
          addBusToSystem(busOwnerID,busName,busRegNumber,busPermitID,busRouteNumber)
            busNme.setText("")
            busRegID.setText("")
            busPermID.setText("")
            busRouteNum.setText("")
            startLocationFragment.setText("")
            endLocationFragment.setText("")

        }
    }

        testValBtn.setOnClickListener {
            busNme.setText("Running Bus")
            busRegID.setText("LG-8950")
            busPermID.setText("78950")
            busRouteNum.setText("76")
            startLocationFragment.setText("Kandy")
            endLocationFragment.setText("Malabe")
        }




    startLocationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

        override fun onPlaceSelected(place: Place) {
            startLocation = place
            // endLocationLat = place.latLng!!.latitude
            // endLocationLng = place.latLng!!.longitude
            //Toast.makeText(context,"Place is ${place.name} ", Toast.LENGTH_SHORT).show()
        }
        override fun onError(status: Status) {
            Log.i(ContentValues.TAG, "An error occurred: $status---------------------------------------------------------------------------------------------------------------")
        }
    })

    endLocationFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

        override fun onPlaceSelected(place: Place) {
            endLocation = place
           // Toast.makeText(context,"Place is ${place.name} ", Toast.LENGTH_SHORT).show()
        }
        override fun onError(status: Status) {
            Log.i(ContentValues.TAG, "An error occurred: $status---------------------------------------------------------------------------------------------------------------")
        }
    })

    return view

    }

     private fun addBusToSystem(busOwnerID: String, busName: String, busRegNumber: String, busPermitID: String, busRouteNumber: String) {
        FirebaseDatabase.getInstance().reference
            .child("Buses")
            .child(busOwnerID)
            .child(busRegNumber)
            .setValue(Bus(busOwnerID,busName,busRegNumber,busPermitID,busRouteNumber,
                startLocation,endLocation,"","",
                "",0,"","","Home"))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Bus Details added to the system", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(activity, "Registration failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }



    private fun validation(
        busName: String,
        busRegNumber: String,
        busPermitID: String,
        busRouteNumber: String
    ): Boolean {
        return when {
            busName.isEmpty() || busRegNumber.isEmpty() || busPermitID.isEmpty() || busRouteNumber.isEmpty() -> {
                Toast.makeText(context, "Please enter all the Information", Toast.LENGTH_LONG).show()
                false
            }
            busName.length > 20 || busRegNumber.length > 12 || busPermitID.length > 20 || busRouteNumber.length > 5 -> {
                Toast.makeText(context, "Please do not exceed word limits", Toast.LENGTH_LONG).show()
                false
            }
            !isValidBusRegNumber(busRegNumber) -> {
                Toast.makeText(context, "Invalid Bus Registration Number", Toast.LENGTH_LONG).show()
                false
            }startLocation == null && endLocation == null ->{
                Toast.makeText(context, "Please Select the Journey Origin and Destination ", Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    // Function to validate Bus Registration Number (e.g., AA-12345)
    private fun isValidBusRegNumber(busRegNumber: String): Boolean {
        val regex = """^[A-Z]+-\d+$""".toRegex()
        return regex.matches(busRegNumber)
    }

    // Function to validate Bus Permit ID (e.g., BP-987654321)




}