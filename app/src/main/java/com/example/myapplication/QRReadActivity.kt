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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class QRReadActivity : AppCompatActivity() {

    private lateinit var cameraSource: CameraSource
    private lateinit var cameraView: SurfaceView
    private val CAMERA_PERMISSION_REQUEST_CODE = 222
    private lateinit var qrResultView :  TextView
    private lateinit var userType : String
    var qrCodeCaptured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrread)

        cameraView = findViewById(R.id.cameraView)
        qrResultView = findViewById(R.id.qrResultView)

        val receivedIntent = intent
        if (receivedIntent != null && receivedIntent.hasExtra("UserType")) {
            userType = receivedIntent.getStringExtra("UserType").toString()
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
                       // Set the flag to true after capturing a valid QR code
                    }
                }
            }
        })
    }

    private fun handleRedirection(result: String) {

        val busData = result.split(",")
        val busID = busData[1]
        val ownerID = busData[0]
        if (ownerID.length==28 && busID.length >6){
            qrCodeCaptured = true

            if (userType == "Driver"){
                AlertDialog.Builder(this)
                    .setTitle("Confirmation of the Bus")
                    .setMessage("Confirm the registration as this bus's driver?")
                    .setPositiveButton("Confirm") { dialog, which ->
                        val intent = Intent(this, DriverActivity::class.java)
                        intent.putExtra("BusID", "$busID")
                        intent.putExtra("OwnerID", "$ownerID")
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }else if(userType=="Passenger"){
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("BusID", "$busID")
                intent.putExtra("OwnerID", "$ownerID")
                startActivity(intent)
                finish()
            }
        }else{
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
        }

    // qrResultView.text ="$busID"
    }

    fun caesarDecrypt(input: String): String {
        val result = StringBuilder()
        val shift = -5
        for (char in input) {
            if (char.isLetter()) {
                val isUpperCase = char.isUpperCase()
                val base = if (isUpperCase) 'A' else 'a'
                val shiftedChar = ((char.toInt() - base.toInt() + shift) % 26 + 26) % 26 + base.toInt()
                result.append(shiftedChar.toChar())
            } else {
                result.append(char)
            }
        }

        return result.toString()
    }

    private fun  startNextActivity(qrContent : String){
        Toast.makeText(this, "QR DATA IS $qrContent", Toast.LENGTH_LONG).show()
    }

    private fun restartActivity() {
        val intent = Intent(this, QRReadActivity::class.java)
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