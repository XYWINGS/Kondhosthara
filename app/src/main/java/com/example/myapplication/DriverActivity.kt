package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.interfaces.DriverJourneyActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class DriverActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var DriverData : DataSnapshot
    private lateinit var welcomeView : TextView
    private lateinit var busNameView : TextView
    private lateinit var distanceView : TextView
    private lateinit var hoursView : TextView
    private lateinit var multiView : TextView
    private var isBusSelected = false
    private lateinit var logOutBtn : FloatingActionButton
    private lateinit var busScanBtn :  FloatingActionButton
    private lateinit var radioOrigin : RadioButton
    private lateinit var radioDestination : RadioButton
    private lateinit var selectlocation : TextView
    private lateinit var busLocRadioGroup : RadioGroup
    private var selectedJourneyLocation  : String? = null
    private var mainBusReference : DatabaseReference ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        auth = Firebase.auth

        selectlocation = findViewById(R.id.textViewSelectLocation)
        logOutBtn = findViewById(R.id.fabDriverLogOut)
        busScanBtn = findViewById(R.id.fabDriverBusQR)
        welcomeView = findViewById(R.id.textViewDriverWelcome)
        busNameView = findViewById(R.id.textViewDriverBusID)
        distanceView = findViewById(R.id.textViewDriverDisTravel)
        hoursView = findViewById(R.id.textViewDriverHrs)
        multiView = findViewById(R.id.textViewAddBus)
        radioOrigin = findViewById(R.id.radioButtonDriverOrigin)
        radioDestination = findViewById(R.id.radioButtonDriverDestination)
        busLocRadioGroup  = findViewById(R.id.selectLocRadioGroup)

        getDriverData()

        busScanBtn.setOnClickListener {
        runOnUiThread {
//            Log.d("Debug","origin selected  ${busLocRadioGroup.checkedRadioButtonId ==  R.id.radioButtonDriverDestination}")
//            Log.d("Debug","destination selected  ${busLocRadioGroup.checkedRadioButtonId ==  R.id.radioButtonDriverOrigin}")
//
//            Log.d("Debug","is bus elected  $isBusSelected")
//            Log.d("Debug","destination selected  condition data  $selectedJourneyLocation")




            if (isBusSelected) {

                if (checkRadioGroup()) {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm the Trip Start")
                        .setMessage("Are you ready to start the trip ? ")
                        .setPositiveButton("Yes") {_, _ ->
                            updateBusData { success ->
                                if (success) {
                                    val intent = Intent(this, DriverJourneyActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Operation Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .setNegativeButton("No") { _, _ ->
                            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                        }
                        .show()
                } else {
                    Toast.makeText(
                        it.context,
                        "Please Select the Journey Start Location ",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                val intent = Intent(this, QRReadActivity::class.java)
                intent.putExtra("UserType", "Driver")
                startActivity(intent)
            }
        }
        }

        busLocRadioGroup.setOnCheckedChangeListener{ _, checkedId ->
            when (checkedId) {
                R.id.radioButtonDriverOrigin -> {
                    selectedJourneyLocation   = radioOrigin.text.toString()
                }
                R.id.radioButtonDriverDestination -> {
                    selectedJourneyLocation  = radioDestination.text.toString()
                }
            }
        }

        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


    private fun checkRadioGroup() : Boolean{
        return if (busLocRadioGroup.checkedRadioButtonId ==  R.id.radioButtonDriverOrigin){
            true
        }else busLocRadioGroup.checkedRadioButtonId ==  R.id.radioButtonDriverDestination
    }

    private fun updateBusData(callback: (Boolean) -> Unit) {

        mainBusReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val updates = hashMapOf(
                        "journeyStatus" to selectedJourneyLocation
                    )
                    mainBusReference!!.updateChildren(updates as Map<String, Any>)
                        .addOnSuccessListener {
                            callback(true)
                        }.addOnFailureListener {
                            callback(false) // Signal failure
                        }
                } else {
                    Toast.makeText(
                        this@DriverActivity,
                        "Bus Data not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false) // Signal failure
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DriverActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(false) // Signal failure
            }
        })

    }

    private fun getDriverData(){
        val userID = auth.currentUser?.uid

        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(userID.toString())

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    DriverData = dataSnapshot


//                    updateDriverDetails(dataSnapshot)
                    updateDriverDetails(dataSnapshot)

                } else {
                    Toast.makeText(
                        this@DriverActivity,
                        "Error Occurred.Try Again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DriverActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateDriverDetails(driverData: DataSnapshot) {
        val driverName = driverData.child("name").value.toString()
        welcomeView.text = "Welcome Back $driverName"

        val busID = driverData.child("busID").value.toString()

        isBusSelected = busID.isNotEmpty()
        if (isBusSelected){
            busScanBtn.setImageResource(R.drawable.baseline_directions_run_24)
            multiView.text = "Start the Journey"
            busNameView.text ="Selected Bus : $busID "
            busScanBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CBA7F4"))

            val busReference =
                FirebaseDatabase.getInstance().reference.child("Buses").child(driverData.child("ownerUid").value.toString()).child(busID)
            mainBusReference = busReference

            busReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //Log.d("Debug","BusSnapshot is $dataSnapshot")
                        radioOrigin.visibility = View.VISIBLE
                        radioDestination.visibility = View.VISIBLE
                        radioOrigin.text = dataSnapshot.child("startLocation").child("name").value.toString()
                        radioDestination.text = dataSnapshot.child("endLocation").child("name").value.toString()

                    } else {
                        Toast.makeText(
                            this@DriverActivity,
                            "Error Occurred.Try Again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@DriverActivity,
                        "Error Occurred ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        }else{
            busScanBtn.setImageResource(R.drawable.sharp_directions_bus_24)
            busScanBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ECDDFD"))
            multiView.text = "Scan Bus's QR"
            busNameView.text = "Select a Bus"
            radioOrigin.visibility = View.GONE
            radioDestination.visibility = View.GONE
        }
        val distanceTraveled = driverData.child("distTraval").value.toString()
        if ( distanceTraveled== "" || distanceTraveled == "0"){
            distanceView.text = "No Travel Records Yet"
        }else{
            distanceView.text = "Distance Traveled : $distanceTraveled kms"
        }

        val hrsDrive = driverData.child("drvHrs").value.toString()
        if (hrsDrive == "" || hrsDrive == "0"){
            hoursView.text = "No Driving Records Yet"
        }else{
            hoursView.text = "Total Driving Time : $hrsDrive hrs"
        }


    }
}