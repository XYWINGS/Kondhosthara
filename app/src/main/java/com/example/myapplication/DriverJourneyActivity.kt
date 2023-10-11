package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import kotlin.math.sqrt


class DriverJourneyActivity : AppCompatActivity() , SensorEventListener {

    private  lateinit var  logOutBtn : Button
    private lateinit var driverData : DataSnapshot
    private lateinit var busData : DataSnapshot
    private lateinit var notifyData : DataSnapshot
    private lateinit var auth: FirebaseAuth
    private var isDataReady : Boolean = false
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var isRecording = false
    private val recordingDuration = 2 * 60 * 1000
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_journey)
        auth = Firebase.auth
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        getDriverData{ success ->
            if (success) {
             //   Log.d("Debug","Get Driver Data OK---------------------------------------------------")
                getBusData{ busSuccess ->
                    if (busSuccess) {
                       // Log.d("Debug","Get Bus Data OK ---------------------------------------------------")
                        isDataReady = true
                        getBusStopNotification{
                            notification->
                            if (notification){
                                startRecording()
                              //  Log.d("Debug","Driver Notified Called Successfully ---------------------------------------------------")
                            }
                        }

                    } else {
                        Toast.makeText(
                            this,
                            "Operation Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } else {
                Toast.makeText(
                    this,
                    "Operation Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        logOutBtn = findViewById(R.id.driverJoruneyLogout)
        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    override fun onResume() {
        super.onResume()
        // Register the sensor listener when the activity is resumed
        sensorManager.registerListener(this@DriverJourneyActivity, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the sensor listener when the activity is paused
        sensorManager.unregisterListener(this@DriverJourneyActivity, accelerometer)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Access accelerometer data
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val thresholdValue = (5.0 / 3.6).toFloat()

            val magnitude = sqrt((x * x + y * y + z * z).toDouble())
            if (magnitude >= thresholdValue) {
                Log.d("Debug", "Moving Very Slowly or Stopped-----------------------------------------")
            } else {
                Log.d("Debug", "Moving Very Fast Indeed-----------------------------------------")
            }

        }
    }

    private fun startRecording() {
        isRecording = true
        handler.postDelayed({
            stopRecording()
        }, recordingDuration.toLong())
    }

    private fun stopRecording() {
        if (isRecording) {
            sensorManager.unregisterListener(this)
            isRecording = false
        }
    }
    private fun getDriverData(callback: (Boolean) -> Unit){
        val userID = auth.currentUser?.uid
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(userID.toString())

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    driverData = dataSnapshot
                    callback(true)
                } else {
                    Toast.makeText(
                        this@DriverJourneyActivity,
                        "Error Occurred.Try Again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DriverJourneyActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(false)
            }
        })
    }
    private fun getBusData(callback: (Boolean) -> Unit){

        val busReference = FirebaseDatabase.getInstance().reference.child("Buses").child(driverData.child("ownerUid").value.toString()).child(driverData.child("busID").value.toString())
        busReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    busData = dataSnapshot
                    callback(true)
                } else {
                    Toast.makeText(
                        this@DriverJourneyActivity,
                        "Error Occurred.Try Again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DriverJourneyActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }
    private fun getBusStopNotification(callback: (Boolean) -> Unit) {
        val busNotiReference =
            FirebaseDatabase.getInstance().reference.child("BusNotify")
                .child(driverData.child("busID").value.toString())

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    notifyData = dataSnapshot

                    val newValue: String = dataSnapshot.value.toString()
                    Log.d("Debug", "Driver Notified Data Received $newValue")
                    callback(true)

                } else {
                    Toast.makeText(
                        this@DriverJourneyActivity,
                        "Error Occurred. Try Again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DriverJourneyActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }

        busNotiReference.addValueEventListener(valueEventListener)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //nothing
    }

}

