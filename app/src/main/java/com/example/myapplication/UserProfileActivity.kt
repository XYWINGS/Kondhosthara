package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptors.UserTripAdaptor
import com.example.myapplication.dataclasses.UserTripRecord
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var recordList:MutableList<UserTripRecord> = mutableListOf()
    private lateinit var recordAdaptor : UserTripAdaptor
    private lateinit var recordRecyclerView: RecyclerView
    private lateinit var delAccBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        recordRecyclerView = findViewById(R.id.tripRecordRecView)
        auth = Firebase.auth
        delAccBtn = findViewById(R.id.deleteProfilePassenger)

        delAccBtn.setOnClickListener {
            deleteAccount()
        }

        recordRecyclerView.layoutManager = LinearLayoutManager(this)

        recordAdaptor = UserTripAdaptor(recordList)
        recordRecyclerView.adapter = recordAdaptor
        Log.d("Debug","Record List $recordList")

        getUserTrips { success ->
            if (success) {
                recordAdaptor.notifyDataSetChanged()
            }
        }
    }

    private fun deleteAccount() {
        val user = Firebase.auth.currentUser
        val userID = auth.currentUser!!.uid
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userID)

        AlertDialog.Builder(this@UserProfileActivity)
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { _, _ ->
                user?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (userRef != null) {
                                userRef.removeValue().addOnSuccessListener {
                                    Toast.makeText(this@UserProfileActivity, "Account has been deleted", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@UserProfileActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else {
                            Toast.makeText(this@UserProfileActivity, "Failed to delete account", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getUserTrips(callback: (Boolean) -> Unit) {
        val userID = auth.currentUser?.uid

            val reference = FirebaseDatabase.getInstance().reference.child("UserTrips").child(userID.toString())

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                      for (records in dataSnapshot.children) {
                          val record = records.getValue(UserTripRecord::class.java)
                          if (record != null) {
                              recordList.add(record)
                          }
                      }
                        callback(true)
                    } else {
                        Toast.makeText(
                            this@UserProfileActivity,
                            "Error Occurred. Try Again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(false)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@UserProfileActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            }

           reference.addValueEventListener(valueEventListener)
        }
    }