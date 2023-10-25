package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptors.FinanceEarnAdaptor
import com.example.myapplication.dataclasses.BusEarn
import com.example.myapplication.dataclasses.GroupedOwnerData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FinanceViewHistoryActivity : AppCompatActivity() {

    private  var earnList : MutableList<BusEarn> = mutableListOf()
    private lateinit var earnAdaptor : FinanceEarnAdaptor
    private lateinit var recyclerView: RecyclerView
    private lateinit var allBusEarnData : DataSnapshot
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_view_history)

        recyclerView = findViewById(R.id.financeViewHistoryRecView)
        recyclerView.layoutManager = LinearLayoutManager(this@FinanceViewHistoryActivity)

        getEarnData { it1 ->
            if (it1){

                if (!earnList.isNullOrEmpty()) {
                    val groupedData: Map<String, List<BusEarn>> = earnList.groupBy { it.ownerID!! }
                    val groupedOwnerDataList = mutableListOf<GroupedOwnerData>()
                    for ((ownerId, busDataList) in groupedData) {
                        val groupedOwnerData = GroupedOwnerData(ownerId, busDataList)
                        groupedOwnerDataList.add(groupedOwnerData)
                    }
                    earnAdaptor = FinanceEarnAdaptor(groupedData)
                    recyclerView.adapter = earnAdaptor
                    earnAdaptor.notifyDataSetChanged()
                }
            }
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
                                val isDone : Boolean = dateSnapshot.child("done").value as Boolean
                                if (isDone){
                                    dateSnapshot.getValue(BusEarn::class.java)?.let { updatedList.add(it) }
                                }
                            }
                        }
                    }
                    earnList.addAll(updatedList)
                    callback(true)
                } else {
                    Toast.makeText(this@FinanceViewHistoryActivity, "No Data Found", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FinanceViewHistoryActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }
        earnReference.addValueEventListener(valueEventListener)
    }
}