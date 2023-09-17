package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.myapplication.fragments.OwnerAddBusFragment
import com.example.myapplication.fragments.OwnerMainFragment

class OwnerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner)

        val addBusBtn : ImageButton = findViewById(R.id.imageButton2)
        val addDriverBtn : ImageButton = findViewById(R.id.imageButton)


        addBusBtn.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.ownerFragmentContainerView,OwnerAddBusFragment())
                commit()
            }
        }

        addDriverBtn.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.ownerFragmentContainerView,OwnerMainFragment())
                commit()
            }
        }

    }


}