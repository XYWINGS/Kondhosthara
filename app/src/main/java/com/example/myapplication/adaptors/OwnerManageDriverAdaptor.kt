package com.example.myapplication.adaptors

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dataclasses.Driver
import com.google.firebase.database.FirebaseDatabase


//driverRecordRecyclerViewManageDriver
class OwnerManageDriverAdaptor(private val driverList:MutableList<Driver>): RecyclerView.Adapter<OwnerManageDriverAdaptor.ViewHolder>() {

    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val busIDText: TextView = itemView.findViewById(R.id.busIDTextViewManageDriverOwner)
        val driverNameText: TextView = itemView.findViewById(R.id.nameDriverTextViewManageDriverOwner)
        val mileageText: TextView = itemView.findViewById(R.id.experienceTextViewManageDriverOwner)
        val deleteRecordBtn : Button = itemView.findViewById(R.id.deleteBtnManageDriverOwner)
        val layout : LinearLayout = itemView.findViewById(R.id.linearlayoutDriverRecord)
        val drvHrsText : TextView = itemView.findViewById(R.id.drvHoursTextViewManageDriverOwner)
        val nicText : TextView = itemView.findViewById(R.id.nicTextViewManageBusOwner)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_driver_record,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position % colors.size]
        val record =driverList[position]

        holder.driverNameText.text = "Driver Name : "+ record.name
        holder.busIDText.text = "Reg No : " + record.busID
        holder.mileageText.text = "Mileage : "+ record.distTravel
        holder.layout.setBackgroundColor(Color.parseColor(color))
        holder.drvHrsText.text = "Name of the bus : " +record.drvHrs
        holder.nicText.text = "Route No : " + record.nic



        holder.deleteRecordBtn.setOnClickListener {

            FirebaseDatabase.getInstance().reference
                .child("Users")
                .removeValue()
                .addOnSuccessListener {
                    driverList.remove(record)
                    notifyDataSetChanged()
                    Toast.makeText(holder.itemView.context,"Bus Removed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(holder.itemView.context,"Failed to delete: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }

        }
    }

    override fun getItemCount(): Int {
        return driverList.size
    }

}

