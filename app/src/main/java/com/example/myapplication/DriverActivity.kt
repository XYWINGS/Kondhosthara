package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.back)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        setContentView(imageView)

        auth = Firebase.auth
        val user = auth.currentUser
        val emailName = user!!.email!!.split("@")[0]

        val logOutBtn : FloatingActionButton = findViewById(R.id.fabDriverLogOut)
    //    val profileBtn :  FloatingActionButton = findViewById(R.id.fabDriverProfile)
        val busScanBtn :  FloatingActionButton = findViewById(R.id.fabDriverBusQR)

        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val driverReference = FirebaseDatabase.getInstance().reference.child("Drivers")
        val userID =user!!.uid

        driverReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ownerSnapshot in dataSnapshot.children) {
                    val ownerID = ownerSnapshot.key
                    for (driverSnapshot in ownerSnapshot.children) {
                        val driverEmailName = driverSnapshot.key
                        if (driverEmailName == emailName){
                            val driverData = driverSnapshot.value
                            driverSnapshot.ref.removeValue()
                            val newDriverReference = ownerID?.let { driverReference.child(it).child(userID) }
                            newDriverReference?.setValue(driverData)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DriverActivity, "Error Occurred ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })



    }


}