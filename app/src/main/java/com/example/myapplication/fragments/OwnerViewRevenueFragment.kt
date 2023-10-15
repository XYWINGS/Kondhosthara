package com.example.myapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adaptors.FinanceEarnAdaptor
import com.example.myapplication.adaptors.OwnerEarnAdaptor
import com.example.myapplication.dataclasses.BusEarn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class OwnerViewRevenueFragment : Fragment() {

    private  var earnList : MutableList<BusEarn> = mutableListOf()
    private lateinit var earnAdaptor : OwnerEarnAdaptor
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_owner_view_revenue, container, false)
        auth = Firebase.auth

        recyclerView = view.findViewById(R.id.ownerViewBusEarnRecView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        getEarnData { it1 ->
            if (it1){
                // Log.d("debug","$earnList")
                earnAdaptor = OwnerEarnAdaptor(earnList)
                recyclerView.adapter =earnAdaptor
                earnAdaptor.notifyDataSetChanged()

            }
        }

        return view
    }




    private fun getEarnData(callback: (Boolean) -> Unit) {

        val userID = auth.currentUser!!.uid
        val earnReference = FirebaseDatabase.getInstance().reference.child("BusEarnData").child(userID)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var updatedList = mutableListOf<BusEarn>()
                    Log.d("debug","$dataSnapshot")
                    earnList.clear()

                    for (busSnapshot in dataSnapshot.children) {
                        for (dateSnapshot in busSnapshot.children) {
                            dateSnapshot.getValue(BusEarn::class.java)?.let { updatedList.add(it) }
                        }
                    }

                    earnList.addAll(updatedList)
                    callback(true)
                } else {
                    Toast.makeText(context, "No Data Found", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }
        earnReference.addValueEventListener(valueEventListener)
    }

}