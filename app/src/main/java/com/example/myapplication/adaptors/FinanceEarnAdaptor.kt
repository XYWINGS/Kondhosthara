package com.example.myapplication.adaptors

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dataclasses.BusEarn

class FinanceEarnAdaptor(private val dataList: Map<String, List<BusEarn>>): RecyclerView.Adapter<FinanceEarnAdaptor.ViewHolder>() {
    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val layout : LinearLayout = itemView.findViewById(R.id.earnDataParentLinerLaayout)
        val ownerID : TextView = itemView.findViewById(R.id.ownerIDParentRecordEarn)
        val innerRecView : RecyclerView = itemView.findViewById(R.id.earnDataInnerRecView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position % colors.size]
        holder.layout.setBackgroundColor(Color.parseColor(color))

        val ownerIds = dataList.keys.toList()
        val ownerId = ownerIds[position]
        val busEarnList = dataList[ownerId]

        holder.ownerID.text = "Owner ID : $ownerId"


        val mutableBusEarnList = busEarnList!!.toMutableList()
        val innerAdapter = OwnerEarnAdaptor(mutableBusEarnList)
        holder.innerRecView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.innerRecView.adapter = innerAdapter

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_earndata_parent_record,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}