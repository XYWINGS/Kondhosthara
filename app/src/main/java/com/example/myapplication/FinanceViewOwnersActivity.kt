package com.example.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptors.FinaOwnerAdaptor
import com.example.myapplication.dataclasses.Driver
import com.example.myapplication.dataclasses.Owner
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FinanceViewOwnersActivity : AppCompatActivity() {

    private  var ownerList : MutableList<Owner> = mutableListOf()
    private lateinit var ownerAdaptor : FinaOwnerAdaptor
    private lateinit var recyclerView: RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_view_owners)

        recyclerView = findViewById(R.id.finaViewOwnersRecView)
        recyclerView.layoutManager = LinearLayoutManager(this@FinanceViewOwnersActivity)

        getOwnerData {
            if (it){
                ownerAdaptor = FinaOwnerAdaptor(ownerList)
                recyclerView.adapter = ownerAdaptor
                ownerAdaptor.notifyDataSetChanged()

            }
        }
    }



    private fun getOwnerData(callback: (Boolean) -> Unit) {

        val userReference = FirebaseDatabase.getInstance().reference.child("Users")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val updatedOwnerList = mutableListOf<Owner>()
                    for (usersSnapshots in dataSnapshot.children) {
                        val userType = usersSnapshots.child("type").value.toString()
                        if (userType=="Owner"){
                                usersSnapshots.getValue(Owner::class.java)
                                    ?.let { updatedOwnerList.add(it) }
                        }
                    }

                    ownerList.clear()
                    ownerList.addAll(updatedOwnerList)
                    callback(true)

                } else {
                    Toast.makeText(
                       this@FinanceViewOwnersActivity,
                        "No Data Found",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FinanceViewOwnersActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }
        userReference.addValueEventListener(valueEventListener)
    }
}