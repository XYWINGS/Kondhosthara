package com.example.myapplication.fragments

import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.nio.charset.Charset
import java.util.EnumMap
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.log

class OwnerAddBusFragment : Fragment() {
    private val APIKEY = "AIzaSyBtydB5hJ7sw4uFbMQOINK9N-5SCObh524"
    private lateinit var auth: FirebaseAuth
    private var startLocation  :Place? = null
    private var endLocation  :Place? = null
    private var qrCodeImageView: ImageView ? = null
    private var PERMISSIONCODE = 222
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        val user = auth.currentUser
        val view = inflater.inflate(R.layout.fragment_owner_add_bus, container, false)
        this.context?.let { Places.initialize(it, APIKEY) }
        val busNme = view.findViewById<EditText>(R.id.editTextBusName)
        val busRegID = view.findViewById<EditText>(R.id.editTextBusRegistrationID)
        val busPermID = view.findViewById<EditText>(R.id.editTextBusRoutePermitID)
        val busRouteNum = view.findViewById<EditText>(R.id.editTextBusRouteNumber)
        val busRegBtn = view.findViewById<Button>(R.id.busRegBtn)
        val testValBtn = view.findViewById<Button>(R.id.buttonTestValueAddBus)

        qrCodeImageView = view.findViewById(R.id.ownerBusQRView)

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

        if (checkPermission()){
            if(validation(busName,busRegNumber,busPermitID,busRouteNumber)){
                addBusToSystem(busOwnerID,busName,busRegNumber,busPermitID,busRouteNumber)
                busNme.setText("")
                busRegID.setText("")
                busPermID.setText("")
                busRouteNum.setText("")
                startLocationFragment.setText("")
                endLocationFragment.setText("")
            }
        }else{
            requestPermissions()
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
    private fun checkPermission(): Boolean {
        return context?.let { ActivityCompat.checkSelfPermission(it, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED || context?.let {
            ActivityCompat.checkSelfPermission(
                it, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        } == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            context as Activity, arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), PERMISSIONCODE
        )
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
                    qrCodeCaller(busOwnerID,busRegNumber)
                } else {
                    Toast.makeText(activity, "Registration failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun encrypt(text: String, key: String): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val keyBytes = hexStringToByteArray(key)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(text.toByteArray())
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4)
                    + Character.digit(hexString[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun qrCodeCaller(ownerID: String, busRegNum: String) {
        val combinedData = "$ownerID|$busRegNum"
        val encryptionKey = "5D6A7D723EAD6F586DCCDEA0847CA6565D6A7D723EAD6F586DCCDEA0847CA656"
        val encryptedData = encrypt(combinedData, encryptionKey)
        val qrCodeBitmap = generateQRCodeBitmap(encryptedData, busRegNum ,800, 800)
        if (qrCodeBitmap != null) {
            saveQRCodeToGallery(qrCodeBitmap,busRegNum)
        }
        qrCodeImageView?.setImageBitmap(qrCodeBitmap)
    }

    private fun generateQRCodeBitmap(data: ByteArray, heading: String, width: Int, height: Int): Bitmap? {
        try {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                String(data, Charset.forName("ISO-8859-1")),
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
            )

            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) -0x1000000 else -0x1
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(
                pixels,
                0,
                width,
                0,
                0,
                width,
                height
            )

            if (heading.isNotEmpty()) {
                val canvas = Canvas(bitmap)
                val headingPaint = Paint()
                headingPaint.textSize = 40f
                headingPaint.color = -0x1000000 // Black color
                val textBounds = Rect()
                headingPaint.getTextBounds(heading, 0, heading.length, textBounds)
                val x = (width - textBounds.width()) / 2
                val y = 25  + textBounds.height() // Position the heading at the top
                canvas.drawText(heading, x.toFloat(), y.toFloat(), headingPaint)
            }

            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }
    }

    private fun validation(busName: String, busRegNumber: String, busPermitID: String, busRouteNumber: String
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
    private fun isValidBusRegNumber(busRegNumber: String): Boolean {
        val regex = """^[A-Z]+-\d+$""".toRegex()
        return regex.matches(busRegNumber)
    }
    private fun saveQRCodeToGallery(qrCodeBitmap: Bitmap, busRegNumber: String) {
        val displayName = "QRCode_${busRegNumber}.png"
        val mimeType = "image/png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { imageUri ->
            val outputStream = resolver.openOutputStream(imageUri)
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream?.close()

            // Notify the MediaScanner about the new image
            MediaScannerConnection.scanFile(context, arrayOf(imageUri.path), arrayOf(mimeType)) { _, _ ->
                Toast.makeText(activity, "QR Code has been saved to the Gallery", Toast.LENGTH_LONG).show()
            }
        }
    }

}