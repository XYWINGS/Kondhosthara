package com.example.myapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adaptors.OwnerManageBusAdaptor
import com.example.myapplication.dataclasses.Bus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class OwnerManageBusFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private  var busList : MutableList<Bus> = mutableListOf()
    private lateinit var busAdaptor : OwnerManageBusAdaptor
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_owner_manage_bus, container, false)
        auth = Firebase.auth

        recyclerView = view.findViewById(R.id.recycleViewOwnerManageBus)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getBusStopNotification {it->
            if (it){
                busAdaptor = OwnerManageBusAdaptor(busList)
                recyclerView.adapter =busAdaptor
            }
        }
        return view
    }


    private fun getBusStopNotification(callback: (Boolean) -> Unit) {

        val userID = auth.currentUser?.uid
        val busReference = FirebaseDatabase.getInstance().reference.child("Buses").child(userID.toString())
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (busSnapshot in dataSnapshot.children) {
                        busSnapshot.getValue(Bus::class.java)?.let { busList.add(it) }
                    }
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
       busReference.addValueEventListener(valueEventListener)
    }
}