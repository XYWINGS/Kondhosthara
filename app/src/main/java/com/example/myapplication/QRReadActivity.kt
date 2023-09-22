package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class QRReadActivity : AppCompatActivity() {

    private lateinit var cameraSource: CameraSource
    private lateinit var cameraView: SurfaceView
    private val CAMERA_PERMISSION_REQUEST_CODE = 222
    private lateinit var userType: String
    private lateinit var auth: FirebaseAuth
    var qrCodeCaptured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrread)
        cameraView = findViewById(R.id.cameraView)
        auth = Firebase.auth


        val receivedIntent = intent
        if (receivedIntent != null && receivedIntent.hasExtra("UserType")) {
            userType = receivedIntent.getStringExtra("UserType").toString()
            intent.removeExtra("UserType")
        }

        if (checkPermission()) {
            initializeCameraSource()
        } else {
            requestCameraPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun initializeCameraSource() {
        val barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(holder)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                if (!qrCodeCaptured) { // Check if a QR code hasn't been captured yet
                    val barcodes = detections.detectedItems
                    if (barcodes.size() > 0) {
                        val qrContent = barcodes.valueAt(0).displayValue
                        handleRedirection(caesarDecrypt(qrContent))
                    }
                }
            }
        })
    }

    private fun handleRedirection(result: String) {

        runOnUiThread {
            val busData = result.split(",")
            val busID = busData[1]
            val ownerID = busData[0]
            if (ownerID.length == 28 && busID.length > 6) {
                qrCodeCaptured = true

                if (userType == "Driver") {

                    AlertDialog.Builder(this@QRReadActivity)
                        .setTitle("Confirmation of the Bus")
                        .setMessage("Confirm your registration as the driver of the bus with ID $busID ")
                        .setPositiveButton("Confirm") { dialog, which ->

                            updateUserData(busID,ownerID) { success ->

                                val intent = Intent(this@QRReadActivity, DriverActivity::class.java)
                                if (success) {
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@QRReadActivity,
                                        "Operation Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                        .setNegativeButton("Cancel") { dialog, which ->
                            Toast.makeText(this@QRReadActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                        }
                        .show()
                } else if (userType == "Passenger") {

                    updateUserData(busID,ownerID) { success ->

                        val intent = Intent(this, MapsActivity::class.java)

                        if (success) {
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@QRReadActivity,
                                "Operation Failed",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                            startActivity(intent)
                            finish()
                        }
                    }
                }

            } else {
                Toast.makeText(this@QRReadActivity, "Invalid QR Code", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

        private fun updateUserData(busID: String, ownerID : String, callback: (Boolean) -> Unit) {
            runOnUiThread {
                val userID = auth.currentUser?.uid

                val userReference =
                    FirebaseDatabase.getInstance().reference.child("Users").child(userID.toString())

                val busReference =
                    FirebaseDatabase.getInstance().reference.child("Buses").child(ownerID).child(busID)

                busReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val updates = hashMapOf(
                                "driverID" to userID
                            )
                            busReference.updateChildren(updates as Map<String, Any>)
                                .addOnSuccessListener {
                                callback(true)
                            }.addOnFailureListener {
                                callback(false) // Signal failure
                            }
                        } else {
                            Toast.makeText(
                                this@QRReadActivity,
                                "Bus Data not Found",
                                Toast.LENGTH_SHORT
                            ).show()
                            callback(false) // Signal failure
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@QRReadActivity,
                            "Error Occurred ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(false) // Signal failure
                    }
                })

                userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val updates = hashMapOf(
                                "busID" to busID
                            )
                            userReference.updateChildren(updates as Map<String, Any>)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@QRReadActivity,
                                        "Bus Registered Successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    callback(true) // Signal success
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        this@QRReadActivity,
                                        "Error Occurred ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    callback(false) // Signal failure
                                }
                        } else {
                            Toast.makeText(
                                this@QRReadActivity,
                                "User data not Found",
                                Toast.LENGTH_SHORT
                            ).show()
                            callback(false) // Signal failure
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@QRReadActivity,
                            "Error Occurred ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(false) // Signal failure
                    }
                })
            }
        }

        fun caesarDecrypt(input: String): String {
            val result = StringBuilder()
            val shift = -5
            for (char in input) {
                if (char.isLetter()) {
                    val isUpperCase = char.isUpperCase()
                    val base = if (isUpperCase) 'A' else 'a'
                    val shiftedChar =
                        ((char.toInt() - base.toInt() + shift) % 26 + 26) % 26 + base.toInt()
                    result.append(shiftedChar.toChar())
                } else {
                    result.append(char)
                }
            }

            return result.toString()
        }

        private fun restartActivity() {
            val intent = Intent(this@QRReadActivity, QRReadActivity::class.java)
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
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeCameraSource()
                    restartActivity()
                    Log.d("debug", "Camera Permissions granted")
                }
            }
        }
    }

