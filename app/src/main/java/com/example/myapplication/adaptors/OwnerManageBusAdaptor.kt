package com.example.myapplication.adaptors

import android.annotation.SuppressLint
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
import com.example.myapplication.dataclasses.Bus
import com.google.firebase.database.FirebaseDatabase


class OwnerManageBusAdaptor (private val busList:MutableList<Bus>): RecyclerView.Adapter<OwnerManageBusAdaptor.ViewHolder>() {

    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val busIDText: TextView = itemView.findViewById(R.id.busIDTextViewManageBusOwner)
        val busNameText : TextView = itemView.findViewById(R.id.busNameTextViewManageBusOwner)
        val routeIDText : TextView = itemView.findViewById(R.id.routeIDTextViewManageBusOwner)
        val driverNameText: TextView = itemView.findViewById(R.id.driverNameTextViewManageBusOwner)
        val mileageText: TextView = itemView.findViewById(R.id.runningTimeTextViewManageBusOwner)
        val deleteRecordBtn : Button= itemView.findViewById(R.id.deleteBtnManageBusOwner)
        val layout : LinearLayout = itemView.findViewById(R.id.busRecordLinearLayout)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_bus_record,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return busList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position % colors.size]
        val record =busList[position]

        holder.busNameText.text = "Name of the bus : " +record.busName
        holder.busIDText.text = "Reg No : " + record.busRegID
        holder.routeIDText.text = "Route No : " + record.routeID
        holder.driverNameText.text = "Driver Name : "+ record.driverName
        holder.mileageText.text = "Mileage : "+ record.disTravel + "km"
        holder.layout.setBackgroundColor(Color.parseColor(color))

        holder.deleteRecordBtn.setOnClickListener {

            if (record.driverID?.isEmpty() == true){
                record.ownerUid?.let { it1 ->
                    record.busRegID?.let { it2 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Buses")
                            .child(it1)
                            .child(it2)
                            .removeValue()
                            .addOnSuccessListener {
                                busList.remove(record)
                                Toast.makeText(holder.itemView.context,"Bus Removed",Toast.LENGTH_SHORT).show()
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(holder.itemView.context,"Failed to delete: ${e.message}",Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }else{
                Toast.makeText(holder.itemView.context,"Bus is currently Occupied. Please wait till return to yard.",Toast.LENGTH_LONG).show()
            }

        }

    }
}


