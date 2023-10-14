package com.example.myapplication.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class OwnerProfileFragment : Fragment() {



    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_owner_profile, container, false)
        val dltAccBtn = view.findViewById<Button>(R.id.buttonDeleteAccountOwner)
        val logOutBtn =  view.findViewById<Button>(R.id.buttonLogoutOwnerMain)
        val viewFleetBtn = view.findViewById<Button>(R.id.viewFleetOwner)

        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(context, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        dltAccBtn.setOnClickListener {
            val user = Firebase.auth.currentUser
            val userID= user!!.uid

            val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userID)

                AlertDialog.Builder(requireContext())
                    .setMessage("Are you sure you want to delete your account?")
                    .setPositiveButton("Yes") { _, _ ->
                        user?.delete()
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if (userRef != null) {
                                        userRef.removeValue().addOnSuccessListener {
                                            Toast.makeText(activity, "Account has been deleted", Toast.LENGTH_LONG).show()
                                            activity?.let {
                                                val intent = Intent(it, LoginActivity::class.java)
                                                it.startActivity(intent)
                                                requireActivity().finish()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(activity, "Failed to delete account", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

        viewFleetBtn.setOnClickListener {
            activity?.let {
                val intent = Intent(it, LoginActivity::class.java)
                it.startActivity(intent)
                requireActivity().finish()
            }
        }

        return view
    }


}