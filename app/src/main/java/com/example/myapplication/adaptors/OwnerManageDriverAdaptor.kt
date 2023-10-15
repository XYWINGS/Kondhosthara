package com.example.myapplication.adaptors

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dataclasses.Driver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


//driverRecordRecyclerViewManageDriver
class OwnerManageDriverAdaptor(private val driverList:MutableList<Driver>): RecyclerView.Adapter<OwnerManageDriverAdaptor.ViewHolder>() {

    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val busIDText: TextView = itemView.findViewById(R.id.busIDTextViewManageDriverOwner)
        val driverNameText: TextView = itemView.findViewById(R.id.nameDriverTextViewManageDriverOwner)
        val mileageText: TextView = itemView.findViewById(R.id.experienceTextViewManageDriverOwner)
        val deleteRecordBtn : Button = itemView.findViewById(R.id.deleteBtnManageDriverOwner)
        val phnNumText : TextView = itemView.findViewById(R.id.phoneTextViewManageDriverOwner)
        val layout : LinearLayout = itemView.findViewById(R.id.linearlayoutDriverRecord)
        val drvHrsText : TextView = itemView.findViewById(R.id.drvHoursTextViewManageDriverOwner)
        val nicText : TextView = itemView.findViewById(R.id.nicTextViewManageBusOwner)
        val permText : TextView = itemView.findViewById(R.id.drvPermTextViewManageDriverOwner)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_driver_record,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position % colors.size]
        val record =driverList[position]
        var isDelete = false

        holder.driverNameText.text = "Driver Name : "+ record.name
        if (record.busID == null || record.busID==""){
            holder.busIDText.text = "Current Bus : Not Occupied"
        }else{
            holder.busIDText.text = "Current Bus : " + record.busID
        }

        holder.mileageText.text = "Distance Driven : "+ record.distTravel+" km"
        holder.layout.setBackgroundColor(Color.parseColor(color))
        holder.drvHrsText.text = "Driving Hours : " +record.drvHrs+" h"
        holder.nicText.text = "NIC No : " + record.nic
        holder.phnNumText.text = "Phone No : " + record.phone

        val permStat =  record.permission.toString()
        if (permStat=="Granted"){
            holder.deleteRecordBtn.text = "Revoke Login Permissions"
        }else if (permStat=="Revoked"){
            holder.deleteRecordBtn.text = "Grant Login Permissions"
        }
        holder.permText.text = "Permission Status :  $permStat"

        holder.deleteRecordBtn.setOnClickListener {

            holder.deleteRecordBtn.text = "Are you sure?"
            holder.deleteRecordBtn.setOnClickListener {

                var updates = hashMapOf<String, String>()
                val userReference =
                    FirebaseDatabase.getInstance().reference.child("Users").child(record.uid!!)

                if (permStat == "Granted") {
                    updates = hashMapOf(
                        "permission" to "Revoked",
                    )

                } else if (permStat == "Revoked") {
                    updates = hashMapOf(
                        "permission" to "Granted",
                    )
                }

                userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {

                            userReference.updateChildren(updates as Map<String, String>)
                                .addOnSuccessListener {
                                    driverList.remove(record)
                                    //notifyDataSetChanged()
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Driver Access Changed",
                                        Toast.LENGTH_LONG
                                    ).show()

                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            holder.itemView.context, "Failed to delete: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            }
        }
    }

    override fun getItemCount(): Int {
        return driverList.size
    }

}

