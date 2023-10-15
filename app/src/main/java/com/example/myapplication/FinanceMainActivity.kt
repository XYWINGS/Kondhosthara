package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptors.FinanceEarnAdaptor
import com.example.myapplication.adaptors.OwnerManageBusAdaptor
import com.example.myapplication.dataclasses.Bus
import com.example.myapplication.dataclasses.BusEarn
import com.example.myapplication.dataclasses.FlattenEarnRecord
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.lang.Integer.parseInt
import kotlin.math.log


class FinanceMainActivity : AppCompatActivity() {
    private  var earnList : MutableList<BusEarn> = mutableListOf()
    private lateinit var earnAdaptor : FinanceEarnAdaptor
    private lateinit var recyclerView: RecyclerView
    private lateinit var logBtn :Button
    private lateinit var allBusEarnData : DataSnapshot
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_main)
        logBtn = findViewById(R.id.logoutBtnfinance)
        recyclerView = findViewById(R.id.financeDataViewRecView)
        recyclerView.layoutManager = LinearLayoutManager(this@FinanceMainActivity)

        getEarnData { it1 ->
            if (it1){
               // Log.d("debug","$earnList")

//                earnAdaptor = FinanceEarnAdaptor(earnList)
//                recyclerView.adapter =earnAdaptor
//                earnAdaptor.notifyDataSetChanged()
                val flattenedData = flattenDataStructure()
                val groupedData = flattenedData.groupBy { it.ownerID }
                Log.d("debig", "$groupedData")
//
//                earnAdaptor = FinanceEarnAdaptor(groupedData)
//                recyclerView.adapter =earnAdaptor
//                earnAdaptor.notifyDataSetChanged()





            }
        }

        logBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun getEarnData(callback: (Boolean) -> Unit) {


        val earnReference = FirebaseDatabase.getInstance().reference.child("BusEarnData")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    allBusEarnData = dataSnapshot
                    var updatedList = mutableListOf<BusEarn>()
                    Log.d("debug","$dataSnapshot")
                    earnList.clear()
                    for (ownerSnapshot in dataSnapshot.children) {
                        for (busSnapshot in ownerSnapshot.children) {
                            for (dateSnapshot in busSnapshot.children) {
                                dateSnapshot.getValue(BusEarn::class.java)?.let { updatedList.add(it) }
                            }
                        }
                    }
                    earnList.addAll(updatedList)
                    callback(true)
                } else {
                    Toast.makeText(this@FinanceMainActivity, "No Data Found", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FinanceMainActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }
        earnReference.addValueEventListener(valueEventListener)
    }

    private fun flattenDataStructure() : List<FlattenEarnRecord> {
        val flattenedList = mutableListOf<FlattenEarnRecord>()

        if (allBusEarnData.exists()){

            for (ownerData in allBusEarnData.children) {
                for (busData in ownerData.children) {
                    for (dateData in busData.children) {

                        Log.d("degub","$dateData")

                        val date = dateData.child("date").value.toString()
                        val earnVal = parseInt(dateData.child("earnVal").value.toString())
                        val userCount = parseInt(dateData.child("userCount").value.toString())
                        val ownerID =  dateData.child("ownerID").value.toString()
                        val busID =  dateData.child("busID").value.toString()
                        val isDone : Boolean = dateData.child("done").value as Boolean

                        flattenedList.add(
                           FlattenEarnRecord(
                                ownerID,
                                busID,
                                date,
                                earnVal,
                                userCount,
                                isDone
                            )
                        )
                    }
                }
            }
    }

        return flattenedList
    }

}