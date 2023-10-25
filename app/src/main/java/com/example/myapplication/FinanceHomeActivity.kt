package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.example.myapplication.fragments.ChatFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FinanceHomeActivity : AppCompatActivity() {
    private lateinit var logBtn : Button
    private lateinit var payementBtn : Button
    private lateinit var historyBtn : Button
    private lateinit var ownersBtn : Button
    private lateinit var chatBtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_home)

        logBtn = findViewById(R.id.logoutBtnfinance)
        payementBtn = findViewById(R.id.goToFinanceMainBtnFina)
        historyBtn = findViewById(R.id.goToHistoryBtnFina)
        ownersBtn = findViewById(R.id.goToOwnerDataBtnFina)
        chatBtn = findViewById(R.id.chatbtn)

        logBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        payementBtn.setOnClickListener {
            val intent = Intent(this, FinanceMainActivity::class.java)
            startActivity(intent)
        }

        historyBtn.setOnClickListener {
            val intent = Intent(this, FinanceViewHistoryActivity::class.java)
            startActivity(intent)
        }

        ownersBtn.setOnClickListener {
            val intent = Intent(this, FinanceViewOwnersActivity::class.java)
            startActivity(intent)
        }

        chatBtn.setOnClickListener {
            val fragment = ChatFragment()

            // Dynamically create a FrameLayout to act as the container for the fragment
            val frameLayout = FrameLayout(this)
            frameLayout.id = View.generateViewId() // Generate a unique ID for the FrameLayout

            // Add the FrameLayout to the parent view
            val parentLayout = findViewById<LinearLayout>(R.id.financeLayout) // Replace with the ID of the parent layout in your XML
            parentLayout.addView(frameLayout)

            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(frameLayout.id, fragment) // Use the ID of the dynamically created FrameLayout
            transaction.addToBackStack(null) // Optional: Allows the user to navigate back to the previous fragment.
            transaction.commit()
        }

    }
}