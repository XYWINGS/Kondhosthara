package com.example.myapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adaptors.OwnerManageBusAdaptor
import com.example.myapplication.adaptors.OwnerManageDriverAdaptor
import com.example.myapplication.dataclasses.Bus
import com.example.myapplication.dataclasses.Driver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class OwnerManageDriverFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private  var driverList : MutableList<Driver> = mutableListOf()
    private lateinit var driverAdaptor : OwnerManageDriverAdaptor
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_owner_manage_driver, container, false)

        auth = Firebase.auth

        recyclerView = view.findViewById(R.id.driverRecordRecyclerViewManageDriver)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getDriverData {
            if (it){
                driverAdaptor = OwnerManageDriverAdaptor(driverList)
                recyclerView.adapter = driverAdaptor
                driverAdaptor.notifyDataSetChanged()

            }
        }
        return view
    }


    private fun getDriverData(callback: (Boolean) -> Unit) {
        val userID = auth.currentUser?.uid
        val userReference = FirebaseDatabase.getInstance().reference.child("Users")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val updatedDriverList = mutableListOf<Driver>() // Create a new list

                    for (driverSnapshot in dataSnapshot.children) {
                        val userType = driverSnapshot.child("type").value.toString()

                        if (userType=="Driver"){
                            val drvOwnerID = driverSnapshot.child("ownerUid").value.toString()
                            if (drvOwnerID == userID){
                                driverSnapshot.getValue(Driver::class.java)
                                    ?.let { updatedDriverList.add(it) }
                            }
                        }
                    }

                    driverList.clear()
                    driverList.addAll(updatedDriverList)

                    callback(true)
                } else {
                    Toast.makeText(
                        context,
                        "No Data Found",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }
        userReference.addValueEventListener(valueEventListener)
    }


//    private fun getDriverData(callback: (Boolean) -> Unit) {
//
//        val userID = auth.currentUser?.uid
//        val userReference = FirebaseDatabase.getInstance().reference.child("Users")
//        val valueEventListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists())
//                {
//                    for (driverSnapshot in dataSnapshot.children) {
//                        driverSnapshot.getValue(Driver::class.java)?.let {
//                            if (it.ownerUid == userID){
//                                driverList.add(it)
//                            }
//                        }
//                    }
//                    callback(true)
//                } else {
//                    Toast.makeText(
//                        context,
//                        "No Data Found",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    callback(false)
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) {
//                Toast.makeText(context, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
//                callback(false)
//            }
//        }
//        userReference.addValueEventListener(valueEventListener)
//    }

}