package com.example.myapplication.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.dataclasses.Bus
import com.example.myapplication.dataclasses.Driver
import com.example.myapplication.dataclasses.Owner
import com.example.myapplication.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var userType : String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = Firebase.auth
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val registerBtn = view.findViewById<Button>(R.id.userRegBtn)
        val userEmail = view.findViewById<EditText>(R.id.editTextTextRegisterEmailAddress)
        val userName = view.findViewById<EditText>(R.id.editTextTextRegisterName)
        val userPassword = view.findViewById<EditText>(R.id.editTextTextRegisterPassword)
        val userPasswordRetype = view.findViewById<EditText>(R.id.editTextTextRegisterPasswordConfirm)
        val userAddress = view.findViewById<EditText>(R.id.editTextTextRegisterAddress)
        val userIDNum = view.findViewById<EditText>(R.id.editTextTextRegisterIDNumber)
        val userPhnNum = view.findViewById<EditText>(R.id.editTextTextRegisterPhoneNum)
        val progressBar = view.findViewById<ProgressBar>(R.id.registerProgressBar)
        val radioGroup: RadioGroup =  view.findViewById(R.id.radioGrp1)

        radioGroup.setOnCheckedChangeListener{ _, checkedId ->
            when (checkedId) {
                R.id.radioButtonOwner -> {
                  userType = "Owner"
                }
                R.id.radioButtonPassanger -> {
                 userType = "Passenger"
                }
            }
        }
        registerBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val email = userEmail.text.toString()
            val password = userPassword.text.toString()
            val passwordRetype = userPasswordRetype.text.toString()
            val name = userName.text.toString()
            val address = userAddress.text.toString()
            val userID = userIDNum.text.toString()
            val phoneNum = userPhnNum.text.toString()

            if (validation(name, email, userID, phoneNum, address, password, passwordRetype)) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener() { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { profileTask ->
                                    if (profileTask.isSuccessful) {
                                        Log.d(TAG, "User profile updated. ${user.displayName}")
                                    }
                                }

                            if (user != null) {
                                userType?.let { it1 ->
                                    addUserToDatabase(user.uid,name, email, userID, phoneNum, address,
                                        it1
                                    )
                                }
                            }

                            Log.d(TAG, "createUserWithEmail:success")

                            Toast.makeText(
                                activity, "Registered successfully...",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility = View.GONE

                            val transaction =  parentFragmentManager.beginTransaction()
                            transaction.replace(R.id.fragmentContainerView, LoginFragment())
                            transaction.addToBackStack(null)
                            transaction.commit()

                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                activity, "Registation failed...",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility = View.GONE
                        }
                    }

            }else  {
                progressBar.visibility = View.GONE
            }
        }

        return view
    }


    private fun addUserToDatabase (  uid: String,
                                    name: String,
                                    email: String,
                                    personID: String,
                                    phoneNum: String,
                                    address: String,
                                    type: String) : Boolean {
        return try{

            FirebaseDatabase.getInstance().reference.child("Users").child(uid).setValue(User(uid,name,email,phoneNum,address,personID,type,"","","0")).addOnCompleteListener {
                    Toast.makeText(activity, "Registered successfully.", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(activity, "Registered failed. ${it.message}", Toast.LENGTH_LONG).show()
            }

          false
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
        }else if(name.length < 4 || personID.length < 5||  address.length < 4){
            Toast.makeText(activity, "ID numbers and Names must have at least four characters...", Toast.LENGTH_LONG).show()
            false
        }else if(phoneNum.length < 10){
            Toast.makeText(activity, "Phone Number must have at least ten characters...", Toast.LENGTH_LONG).show()
            false
        }else if(userType == null){
            Toast.makeText(activity, "Select whether you are a passenger or a bus owner...", Toast.LENGTH_LONG).show()
            false
        }
        else  {
            true
        }
    }
}