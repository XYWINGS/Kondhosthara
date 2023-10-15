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
import com.example.myapplication.dataclasses.BusEarn
import com.google.firebase.database.FirebaseDatabase

class OwnerEarnAdaptor(private val earnList:MutableList<BusEarn>): RecyclerView.Adapter<OwnerEarnAdaptor.ViewHolder>() {

    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        //val deleteRecordBtn : Button = itemView.findViewById(R.id.deleteBtnManageBusOwner)
        val pasCount : TextView = itemView.findViewById(R.id.passCountTextViewEarnRecord)
        val earnText: TextView = itemView.findViewById(R.id.earningsTextViewEarnRecord)
        val dateText : TextView = itemView.findViewById(R.id.dateTextViewEarnRecord)
        val busIDText: TextView = itemView.findViewById(R.id.busIDTextViewEarnRecord)
        val layout : LinearLayout = itemView.findViewById(R.id.aBusEarnRecordLayout)
        val payBtn : Button = itemView.findViewById(R.id.gotoPaymentFinBtn)
        val comisText : TextView = itemView.findViewById(R.id.comisViewEarnRecord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_busearn_record,parent,false)
        return  ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position % colors.size]
        val record = earnList[position]
        val newVal =  (record.earnVal!! *10/100)

        holder.busIDText.text = "Reg No : " + record.busID
        holder.layout.setBackgroundColor(Color.parseColor(color))
        holder.pasCount.text = "Passengers Traveled : " +record.userCount
        holder.dateText.text = "Date: " + record.date
        holder.earnText.text = "Earnings : Rs "+ record.earnVal
        holder.comisText.text = "Commission for App is : Rs $newVal"

      //  Log.d("debug","record is $record")
        val done = record.done as Boolean
        if (!done){
            holder.payBtn.text = "Go to Payment"
        }else{
            holder.payBtn.text = "Delete Record"
        }

        holder.payBtn.setOnClickListener {
            val reference =  FirebaseDatabase.getInstance().reference.child("BusEarnData").child(record.ownerID!!).child(record.busID!!).child(record.date!!)
            holder.payBtn.text = "Are You Sure?"

            holder.payBtn.setOnClickListener {

                if (!done){
                    reference.removeValue()
                        .addOnSuccessListener {
                            earnList.remove(record)
                            Toast.makeText(holder.itemView.context,"Data Deleted", Toast.LENGTH_SHORT).show()
                            // notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(holder.itemView.context,"Failed to delete: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }

                }else{
                   // val newVal =  (record.earnVal!! *10/100)
                    val updates = hashMapOf<String,Any>(
                        "done" to true,
                        "commission" to newVal
                    )
                    reference.updateChildren(updates as Map<String, Any>)
                        .addOnSuccessListener {
                            earnList.remove(record)
                            Toast.makeText(holder.itemView.context,"Payment Done", Toast.LENGTH_SHORT).show()
                            // notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(holder.itemView.context,"Failed to Pay ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return earnList.size
    }



}