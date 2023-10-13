package com.example.myapplication.adaptors

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dataclasses.UserTripRecord


class UserTripAdaptor (private val userTripRecord:MutableList<UserTripRecord>): RecyclerView.Adapter<UserTripAdaptor.ViewHolder>() {

    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val originText: TextView = itemView.findViewById(R.id.originTextViewProfile)
        val destinationTest : TextView = itemView.findViewById(R.id.destinationTextViewProfile)
        val busIDText : TextView = itemView.findViewById(R.id.busIDTextViewProfile)
        val costText: TextView = itemView.findViewById(R.id.costTextViewProfile)
        val endTimeText: TextView = itemView.findViewById(R.id.endTimeTextViewProfile)
        val startTimeText: TextView = itemView.findViewById(R.id.startTimeTextViewProfile)
        val distanceText : TextView = itemView.findViewById(R.id.distanceTextViewProfile)
        val recordLinearLayout : LinearLayout =itemView.findViewById(R.id.recordLinearLayout)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_user_trip_record,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val color = colors[position % colors.size]
        val record = userTripRecord[position]


        if (record.origin == null || record.origin == ""){
            holder.originText.text= "No Data"
        }else{
            holder.originText.text = "Origin : " +record.origin
        }


        if (record.destination == null || record.destination=="" ){
            holder.destinationTest.text = "No Data"
        }else{
            holder.destinationTest.text = "Destination : " + record.destination
        }


        holder.busIDText.text = "Bus Tag : " + record.busID
        holder.costText.text = "Trip Cost : " + record.cost


        val dis = record.distance.toString().toDouble() / 1000.0
        holder.distanceText.text =  "Travel Distance :" + String.format("%.2f", dis) + "Km"

        holder.startTimeText.text = "Time of Enter :"+record.startTime?.getValue("hours")+" hours "+record.startTime?.getValue("minutes") + " minutes "

        holder.endTimeText.text =  "Time of Exit :"+record.endTime?.getValue("hours") +" hours "+record.endTime?.getValue("minutes") + " minutes "

        holder.recordLinearLayout.setBackgroundColor(Color.parseColor(color))
    }




    override fun getItemCount(): Int {
        return userTripRecord.size
    }
}