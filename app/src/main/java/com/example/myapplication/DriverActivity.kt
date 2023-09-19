package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DriverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)


        val logOutBtn : FloatingActionButton = findViewById(R.id.fabDriverLogOut)
        val profileBtn :  FloatingActionButton = findViewById(R.id.fabDriverProfile)
        val busScanBtn :  FloatingActionButton = findViewById(R.id.fabDriverBusQR)

        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }



    }


}