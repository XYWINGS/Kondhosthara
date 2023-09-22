package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var scanQRBtn : FloatingActionButton
private lateinit var viewMapBtn : FloatingActionButton
private lateinit var viewWalletBtn : FloatingActionButton
private lateinit var viewProfileBtn : FloatingActionButton
private lateinit var viewLogOutBtn : FloatingActionButton

class PassengerHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pessanger_home)

        scanQRBtn = findViewById(R.id.fabPassengerQRScan)
        viewMapBtn = findViewById(R.id.fabPassengerMap)
        viewProfileBtn = findViewById(R.id.fabPassengerProfile)
        viewWalletBtn = findViewById(R.id.fabPassengerWallet)
        viewLogOutBtn = findViewById(R.id.fabPassengerLogout)

        viewLogOutBtn.setOnClickListener{
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        scanQRBtn.setOnClickListener {
            val intent = Intent(this, QRReadActivity::class.java)
            intent.putExtra("UserType", "Passenger")
            startActivity(intent)
        }

        viewMapBtn.setOnClickListener {
            val intent = Intent(this,BusViewMap::class.java)
            intent.putExtra("UserType", "Passenger")
            startActivity(intent)
        }


    }
}