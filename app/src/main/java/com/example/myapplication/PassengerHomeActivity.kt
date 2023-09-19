package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton

private lateinit var scanQRBtn : FloatingActionButton
private lateinit var viewMapBtn : FloatingActionButton
private lateinit var viewWalletBtn : FloatingActionButton
private lateinit var viewProfileBtn : FloatingActionButton

class PassengerHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pessanger_home)

        scanQRBtn = findViewById(R.id.fabPassengerQRScan)
        viewMapBtn = findViewById(R.id.fabPassengerMap)
        viewProfileBtn = findViewById(R.id.fabPassengerProfile)
        viewWalletBtn = findViewById(R.id.fabPassengerWallet)

        scanQRBtn.setOnClickListener {
            val intent = Intent(this, QRReadActivity::class.java)
            intent.putExtra("UserType", "Passenger")
            startActivity(intent)
            finish()
        }

        viewMapBtn.setOnClickListener {
            val intent = Intent(this,BusViewMap::class.java)
            intent.putExtra("UserType", "Passenger")
            startActivity(intent)
            finish()
        }


    }
}