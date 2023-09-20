package com.example.myapplication

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.interfaces.DriverJourneyActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class DriverActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var busID : String
    private lateinit var DriverData : DataSnapshot
    private lateinit var welcomeView : TextView
    private lateinit var busNameView : TextView
    private lateinit var distanceView : TextView
    private lateinit var hoursView : TextView
    private lateinit var multiView : TextView
    private var isBusSelected = false
    private lateinit var logOutBtn : FloatingActionButton
    private lateinit var busScanBtn :  FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        auth = Firebase.auth
        val user = auth.currentUser

        logOutBtn = findViewById(R.id.fabDriverLogOut)
        busScanBtn = findViewById(R.id.fabDriverBusQR)
        welcomeView = findViewById(R.id.textViewDriverWelcome)
        busNameView = findViewById(R.id.textViewDriverBusID)
        distanceView = findViewById(R.id.textViewDriverDisTravel)
        hoursView = findViewById(R.id.textViewDriverHrs)
        multiView = findViewById(R.id.textViewAddBus)

        getDriverData()

        busScanBtn.setOnClickListener {
            if (isBusSelected){
                val intent = Intent(this, DriverJourneyActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, QRReadActivity::class.java)
                intent.putExtra("UserType", "Driver")
                startActivity(intent)
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

        }else{
            busScanBtn.setImageResource(R.drawable.sharp_directions_bus_24)
            busScanBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ECDDFD"))
            multiView.text = "Scan Bus's QR"
            busNameView.text = "Select a Bus"
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