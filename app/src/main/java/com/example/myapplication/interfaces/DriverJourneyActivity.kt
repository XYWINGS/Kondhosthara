package com.example.myapplication.interfaces

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private  lateinit var  logOutBtn : Button
class DriverJourneyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_journey)

        logOutBtn = findViewById(R.id.driverJoruneyLogout)

        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}