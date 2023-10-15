package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

private lateinit var scanQRBtn : FloatingActionButton
private lateinit var viewMapBtn : FloatingActionButton
private lateinit var viewWalletBtn : FloatingActionButton
private lateinit var viewProfileBtn : FloatingActionButton
private lateinit var viewLogOutBtn : FloatingActionButton
private lateinit var auth: FirebaseAuth
private var usersData : DataSnapshot ?= null
private var currentWalletBalance : Int?=null
private var isCreditOkay : Boolean  = false

class PassengerHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pessanger_home)
        auth = Firebase.auth
        scanQRBtn = findViewById(R.id.fabPassengerQRScan)
        viewMapBtn = findViewById(R.id.fabPassengerMap)
        viewProfileBtn = findViewById(R.id.fabPassengerProfile)
        viewWalletBtn = findViewById(R.id.fabPassengerWallet)
        viewLogOutBtn = findViewById(R.id.fabPassengerLogout)

        viewWalletBtn.setOnClickListener {
            val intent = Intent(this, PessengerWalletActivity::class.java)
            startActivity(intent)
        }

        viewProfileBtn.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        getUserData(auth.currentUser!!.uid)

        viewLogOutBtn.setOnClickListener{
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        scanQRBtn.setOnClickListener {
            if (isCreditOkay){
                val intent = Intent(this, QRReadActivity::class.java)
                intent.putExtra("UserType", "Passenger")
                startActivity(intent)
            }else{
                Toast.makeText(this@PassengerHomeActivity, "Credit Limit Exceeded. Please TopUp", Toast.LENGTH_LONG).show()
            }
        }

        viewMapBtn.setOnClickListener {
            val intent = Intent(this,BusViewMap::class.java)
            intent.putExtra("UserType", "Passenger")
            startActivity(intent)
        }

    }

    private fun getUserData(uid: String) {

        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    usersData = dataSnapshot
                    currentWalletBalance = Integer.parseInt(dataSnapshot.child("walletBalance").value.toString())
                    if (currentWalletBalance!! < -50){
                        Toast.makeText(this@PassengerHomeActivity, "Credit Limit Exceeded. Please TopUp", Toast.LENGTH_LONG).show()
                    }else{
                        isCreditOkay = true
                    }
                }else{
                    Toast.makeText(this@PassengerHomeActivity, "Error Occurred. Please try Again.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@PassengerHomeActivity,LoginActivity::class.java)
                    Firebase.auth.signOut()
                    startActivity(intent)
                    finish()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@PassengerHomeActivity, "Error Occurred ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}