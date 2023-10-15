package com.example.myapplication.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.DriverActivity
import com.example.myapplication.MapsActivity
import com.example.myapplication.OwnerActivity
import com.example.myapplication.PassengerHomeActivity
import com.example.myapplication.R
import com.example.myapplication.DriverMapsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlin.math.log

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailName : String
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

        val user = auth.currentUser


        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (!arePermissionsGranted(permissions)) {
            requestPermissions(permissions)
        }else{

            if (user != null){
                Firebase.auth.currentUser?.let { it1 ->
                    emailName = user.email?.split("@")?.get(0).toString()
                    redirection(it1.uid,emailName)
                }
            }
        }

        loginButton.setOnClickListener {

            if (arePermissionsGranted(permissions))
            {
                progressBar.visibility = View.VISIBLE
                val email = loginEmail.text.toString()
                val password = loginPassword.text.toString()

                if (email == "" || password == "") {
                    Toast.makeText(activity, "Please Enter Email and Password ", Toast.LENGTH_LONG)
                        .show()
                    progressBar.visibility = View.GONE
                } else {

                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            Firebase.auth.currentUser?.let { it1 ->
                                val emailName = it1.email?.split("@")?.get(0)
                                if (emailName != null) {
                                    redirection(it1.uid, emailName)
                                }
                            }
                            progressBar.visibility = View.GONE

                        } else {
                            Toast.makeText(
                                activity,
                                "No account found for the login details",
                                Toast.LENGTH_LONG
                            ).show()
                            progressBar.visibility = View.GONE
                        }
                    }
                }
            }else{
                Toast.makeText(activity, "App needs all the permissions. Try again after Clearing App Data ", Toast.LENGTH_LONG).show()
                //     Log.d("Permission","Permission Check not granted")
                exitApp()
            }
        }

        signupButton.setOnClickListener {
            val transaction =  parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, RegisterFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun redirection(uid: String, emailName: String) {

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
                        val busId = dataSnapshot.child("busID").value.toString()
                        if (busId.length > 5){
                            activity?.let {
                                val intent = Intent(it, MapsActivity::class.java)
                                it.startActivity(intent)
                                it.finish()
                            }
                        }else{
                            activity?.let {
                                val intent = Intent(it, PassengerHomeActivity::class.java)
                                it.startActivity(intent)
                                it.finish()
                            }
                        }

                    }else if (userType=="Driver"){
                        val status = dataSnapshot.child("status").value.toString()
                        if(status=="driving"){
                            activity?.let {
                                    val intent = Intent(it, DriverMapsActivity::class.java)
                                    it.startActivity(intent)
                                    it.finish()
                                }
                            } else if(status =="idle" || status=="registered"){
                            activity?.let {
                                val intent = Intent(it, DriverActivity::class.java)
                                it.startActivity(intent)
                                it.finish()
                            }
                        }
                    }
                }else{
                    handleDriverLogin(uid,emailName)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(activity, "Error Occurred ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    @SuppressLint("SuspiciousIndentation")
    private fun handleDriverLogin(userID: String, emailName: String) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("Users")

        Log.d("debug", "im called  $userID  $emailName")

        // Get the data from emailRef
        usersRef.child(emailName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Copy data to newRef
                    usersRef.child(userID).setValue(dataSnapshot.value).addOnCompleteListener { copyTask ->
                        if (copyTask.isSuccessful) {
                            // Delete the data from emailRef
                            usersRef.child(emailName).removeValue().addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    // Update newRef with additional data
                                    val updates = hashMapOf<String,Any>(
                                        "uid" to userID,
                                        "status" to "idle"
                                    )
                                    usersRef.child(userID).updateChildren(updates).addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            // Data copied, deleted, and updated successfully
                                            Toast.makeText(context, "Logging in", Toast.LENGTH_SHORT).show()
                                            activity?.let {
                                                val intent = Intent(it, DriverActivity::class.java)
                                                it.startActivity(intent)
                                            }
                                        } else {
                                            // Handle the update failure
                                        }
                                    }
                                } else {
                                    // Handle the delete failure
                                }
                            }
                        } else {
                            // Handle the copy failure
                        }
                    }
                } else {
                    Toast.makeText(context, "Data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Error Occurred ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


















//    private fun handleDriverLogin(userID: String, emailName: String) {
//        val newRef = FirebaseDatabase.getInstance().reference.child("Users").child(userID)
//        val emailRef = FirebaseDatabase.getInstance().reference.child("Users").child(emailName)
//
//        emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//
//                    val data : DataSnapshot = dataSnapshot
//                        newRef.setValue(data).addOnSuccessListener {
//
//                            emailRef.removeValue().addOnSuccessListener {
//
//                                val updates = hashMapOf(
//                                    "uid" to userID,
//                                    "status" to "idle"
//                                )
//                                newRef.updateChildren(updates as Map<String, Any>)
//                                Toast.makeText(context, "Logging in ", Toast.LENGTH_SHORT).show()
//                                activity?.let {
//                                    val intent = Intent(it, DriverActivity::class.java)
//                                    it.startActivity(intent)
//                                }
//                            }
//                        }
//
//                } else {
//                    Toast.makeText(context, "Data not found", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Toast.makeText(
//                    context,
//                    "Error Occurred ${databaseError.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//    }



    private fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            val permissionStatus = ContextCompat.checkSelfPermission(requireActivity(), permission)
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
    }

    private fun exitApp() {
        Toast.makeText(context, "Permissions not granted. Exiting the app.", Toast.LENGTH_SHORT)
            .show()
        requireActivity().finish()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {

            var allPermissionsGranted = true
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (!allPermissionsGranted) {
                exitApp()
            }
        }
    }
    companion object {
        private const val REQUEST_CODE = 123
    }

}