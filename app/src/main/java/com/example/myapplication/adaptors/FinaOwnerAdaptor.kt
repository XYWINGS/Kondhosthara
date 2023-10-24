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
import com.example.myapplication.dataclasses.Owner
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FinaOwnerAdaptor(private val owners:MutableList<Owner>): RecyclerView.Adapter<FinaOwnerAdaptor.ViewHolder>() {

    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val layout : LinearLayout = itemView.findViewById(R.id.finaViewOwnerLayout)
        val nameText: TextView = itemView.findViewById(R.id.finaViewOwnerName)
        val uIDText: TextView = itemView.findViewById(R.id.finaViewOwnerUID)
        val emailText: TextView = itemView.findViewById(R.id.finaViewOwnerEmail)
        val revokeAccessBtn : Button = itemView.findViewById(R.id.finaRevokeAccessOwnerBtn)
        val nicText : TextView = itemView.findViewById(R.id.finaViewOwnerNIC)
        val phoneText : TextView = itemView.findViewById(R.id.finaViewOwnerPhone)
        val permText : TextView = itemView.findViewById(R.id.finaViewOwnerPerm)
    }

    override fun getItemCount(): Int {
        return owners.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_owner_record,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val color = colors[position % colors.size]
        val record =owners[position]
        holder.layout.setBackgroundColor(Color.parseColor(color))
        var permGrated = true

        holder.nameText.text ="Owner Name : ${record.name}"
        holder.uIDText.text = "UID : ${record.uid} "
        holder.emailText.text = "Driving Hours : ${record.email}"
        holder.nicText.text = "NIC No : " + record.nic
        holder.phoneText.text = "Phone No : " + record.phone

        val permStat : String ?=  record.passID.toString()

        if (permStat.isNullOrEmpty()){
            holder.revokeAccessBtn.text = "Revoke Login Permissions"
            holder.permText.text = "Permission Status : Granted"
        }else{
            holder.revokeAccessBtn.text = "Grant Login Permissions"
            holder.permText.text = "Permission Status : Revoked"
            permGrated = false
        }



        holder.revokeAccessBtn.setOnClickListener {

            holder.revokeAccessBtn.text = "Are you sure?"
            holder.revokeAccessBtn.setOnClickListener {

                var updates = hashMapOf<String, String>()
                val userReference =
                    FirebaseDatabase.getInstance().reference.child("Users").child(record.uid!!)

                if (permGrated){
                    updates = hashMapOf(
                        "passID" to "Revoked",
                    )
                }else{
                    updates = hashMapOf(
                        "passID" to "",
                    )
                }

                userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {

                            userReference.updateChildren(updates as Map<String, String>)
                                .addOnSuccessListener {
                                    owners.remove(record)
                                    //notifyDataSetChanged()
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Owner Access Changed",
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

}