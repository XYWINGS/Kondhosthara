package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.myapplication.fragments.OwnerAddBusFragment
import com.example.myapplication.fragments.OwnerMainFragment
import com.example.myapplication.fragments.OwnerManageDriverFragment
import com.example.myapplication.fragments.OwnerManageBusFragment
import com.example.myapplication.fragments.OwnerProfileFragment

class OwnerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner)

        val addBusBtn : ImageButton = findViewById(R.id.imageButton2)
        val addDriverBtn : ImageButton = findViewById(R.id.imageButton)
        val manageBusBtn : ImageButton = findViewById(R.id.imageButton3)
        val manageDriverBtn : ImageButton = findViewById(R.id.imageButton4)
        val profileBtn : ImageButton = findViewById(R.id.imageButton5)


        manageBusBtn.setOnClickListener{
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.ownerFragmentContainerView,OwnerManageBusFragment())
                commit()
            }
        }

        manageDriverBtn.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.ownerFragmentContainerView,OwnerManageDriverFragment())
                commit()
            }
        }

        profileBtn.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.ownerFragmentContainerView,OwnerProfileFragment())
                commit()
            }

        }



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