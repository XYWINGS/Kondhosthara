package com.example.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.DriverActivity
import com.example.myapplication.LoginActivity
import com.example.myapplication.MapsActivity
import com.example.myapplication.OwnerActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton = view.findViewById<Button>(R.id.loginBtn)
        val loginEmail = view.findViewById<EditText>(R.id.editTextTextEmailAddress)
        val loginPassword = view.findViewById<EditText>(R.id.editTextTextPassword)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        auth = Firebase.auth
        val signupButton = view.findViewById<Button>(R.id.regBtn)
        // val passrestButton = view.findViewById<TextView>(R.id.frogot_password_link)

        val user = Firebase.auth.currentUser

        if (user != null){
            Firebase.auth.currentUser?.let { it1 ->  redirection(it1.uid) }
        }

        loginButton.setOnClickListener {

            progressBar.visibility = View.VISIBLE
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()

            if(email == "" || password =="" ){
                Toast.makeText(activity, "Please Enter Email and Password ", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }else{

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                    if (task.isSuccessful) {

                        Firebase.auth.currentUser?.let { it1 ->  redirection(it1.uid) }
                        progressBar.visibility = View.GONE

                    } else {
                        Toast.makeText(activity, "No account found for the login details", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }

        signupButton.setOnClickListener {
            val transaction =  parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, RegisterFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun redirection(uid: String) {

        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    val userType = dataSnapshot.child("type").getValue(String::class.java)

                    if(userType == "Owner"){
                        activity?.let {
                            val intent = Intent(it, OwnerActivity::class.java)
                            it.startActivity(intent)
                            it.finish()
                        }
                    }else if (userType=="Passenger"){
                        activity?.let {
                            val intent = Intent(it, MapsActivity::class.java)
                            it.startActivity(intent)
                            it.finish()
                        }
                    }
                }else{
                    Toast.makeText(activity, "Logging as a driver", Toast.LENGTH_LONG).show()
                    activity?.let {
                        val intent = Intent(it, DriverActivity::class.java)
                        it.startActivity(intent)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(activity, "Error Occurred ${databaseError.message}", Toast.LENGTH_SHORT).show()

            }
        }
        )
    }



}