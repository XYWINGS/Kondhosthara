package com.example.myapplication.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.text.set
import com.example.myapplication.LoginActivity
import com.example.myapplication.R
import com.example.myapplication.dataclasses.Driver
import com.example.myapplication.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class OwnerMainFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        val user = auth.currentUser

        val view = inflater.inflate(R.layout.fragment_owner_main, container, false)
        val userEmail = view.findViewById<EditText>(R.id.editTextTextRegisterEmailAddressDriver)
        val userName = view.findViewById<EditText>(R.id.editTextTextRegisterNameDriver)
        val userPassword = view.findViewById<EditText>(R.id.editTextTextRegisterPasswordDriver)
        val userPasswordRetype = view.findViewById<EditText>(R.id.editTextTextRegisterPasswordConfirmDriver)
        val userAddress = view.findViewById<EditText>(R.id.editTextTextRegisterAddressDriver)
        val userIDNum = view.findViewById<EditText>(R.id.editTextTextRegisterIDNumberDriver)
        val userPhnNum = view.findViewById<EditText>(R.id.editTextTextRegisterPhoneNumDriver)

        val submitBtn = view.findViewById<Button>(R.id.userRegBtnDriver)
        val logouttBtn = view.findViewById<Button>(R.id.logoutBtnDriver)

        val testValBtn = view.findViewById<Button>(R.id.buttonTestValue)

        testValBtn.setOnClickListener {
             userEmail.setText("testriver@gmail.com")
             userPassword.setText("123456")
             userPasswordRetype.setText("123456")
             userName.setText("achintha")
             userAddress.setText("kandy")
             userIDNum.setText("1234567890")
             userPhnNum.setText("1234567890")
        }

        logouttBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(context, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        submitBtn.setOnClickListener {
            val email = userEmail.text.toString()
            val password = userPassword.text.toString()
            val passwordRetype = userPasswordRetype.text.toString()
            val name = userName.text.toString()
            val address = userAddress.text.toString()
            val userID = userIDNum.text.toString()
            val phoneNum = userPhnNum.text.toString()
            val ownerID =user!!.uid

            if (validation(name,email,userID,phoneNum,address,password,passwordRetype)){
                addDriverToDatabase(ownerID,name,email,userID,phoneNum,address,password)
                userEmail.setText("")
                userPassword.setText("")
                userPasswordRetype.setText("")
                userName.setText("")
                userAddress.setText("")
                userIDNum.setText("")
                userPhnNum.setText("")
            }
        }
        return view
    }

    private fun addDriverToDatabase (ownerUid:String,
                                     name: String,
                                     email: String,
                                     personID: String,
                                     phoneNum: String,
                                     address: String,
                                     password: String
                                     ) : Boolean {
        return try{
          //  val emailData =

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { registrationTask ->
                    if (registrationTask.isSuccessful) {
                        FirebaseDatabase.getInstance().reference
                            .child("Drivers")
                            .child(ownerUid)
                            .setValue(Driver(ownerUid, name, email, phoneNum, address, personID, "Driver", "","0","0","","idle"))
                            .addOnCompleteListener { registrationCompleteTask ->
                                if (registrationCompleteTask.isSuccessful) {
                                    Toast.makeText(activity, "Driver added to the system...", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(activity, "Registration failed.", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(activity, "Registration failed. ${registrationTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }


            true
        }catch (e : java.lang.Exception){
            false
        }
    }

    private fun validation(
        name: String,
        email: String,
        personID: String,
        phoneNum: String,
        address: String,
        password: String,
        passRetype: String,
    ): Boolean {
        val progressBar = view?.findViewById<ProgressBar>(R.id.registerProgressBar)
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")

        return if (name == "" || email =="" || personID =="" || phoneNum =="" || address =="" || personID=="" || password =="" || passRetype == ""){
            Toast.makeText(activity, "Please fill all the fields...", Toast.LENGTH_LONG).show()
            if (progressBar != null) {
                progressBar.visibility = View.GONE
            }
            false
        }else if(password.length < 6 ){
            Toast.makeText(activity, "Password must be at least six characters long...", Toast.LENGTH_LONG).show()
            if (progressBar != null) {
                progressBar.visibility = View.GONE
            }
            false
        }else if(password != passRetype){
            Toast.makeText(activity, "Passwords doesn't match...", Toast.LENGTH_LONG).show()
            if (progressBar != null) {
                progressBar.visibility = View.GONE
            }
            false
        }else if(!email.matches(emailRegex)){
            Toast.makeText(activity, "Email is not in a valid format...", Toast.LENGTH_LONG).show()
            false
        }else if(name.length < 4 || personID.length < 10||  address.length < 4){
            Toast.makeText(activity, "ID numbers and Names must have at least four characters...", Toast.LENGTH_LONG).show()
            false
        }else if(phoneNum.length < 10){
            Toast.makeText(activity, "Phone Number must have at least ten characters...", Toast.LENGTH_LONG).show()
            false
        }
        else  {
            true
        }
    }
}

