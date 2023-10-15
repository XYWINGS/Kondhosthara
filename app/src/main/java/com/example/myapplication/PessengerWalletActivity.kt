package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.lang.Integer.parseInt

class PessengerWalletActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var usersData : DataSnapshot?= null
    private var currentWalletBalance : Int?=null
    private lateinit var walletBal : TextView
    private lateinit var topUpBtn : Button
    private lateinit var submitBtn : Button
    private lateinit var testDataBtn : Button
    private lateinit var linearLayout : LinearLayout

    private lateinit var cardNum : EditText
    private lateinit var cardName :EditText
    private lateinit var expDate :EditText
    private lateinit var cvv :EditText
    private lateinit var amountText :EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pessanger_wallet)

        walletBal = findViewById(R.id.passengerWalletBalacne)
        topUpBtn = findViewById(R.id.btnWalletTopUp)
        linearLayout = findViewById(R.id.linerLayoutWalletData)
        cardNum = findViewById(R.id.editTextCardNumber)
        cardName = findViewById(R.id.editTextCardHolder)
        expDate = findViewById(R.id.editTextExpiryDate)
        cvv = findViewById(R.id.editTextCVV)
        submitBtn = findViewById(R.id.btnSubmitWalletData)
        testDataBtn = findViewById(R.id.btnTestWalletData)
        amountText  = findViewById(R.id.editTextAmount)
        auth = Firebase.auth
        val userID = auth.currentUser!!.uid
        submitBtn.setOnClickListener{
            val cardNumber = cardNum.text.toString()
            val cardholderName = cardName.text.toString()
            val expiryDate = expDate.text.toString()
            val cvvText = cvv.text.toString()
            val amount = amountText.text.toString()

            if (isCardNumberValid(cardNumber) && isCardholderNameValid(cardholderName) &&
                isExpiryDateValid(expiryDate) && isCVVValid(cvvText) && isAmountValid(amount)) {
                updateWallet(userID,amount){
                    if (it){
                        linearLayout.visibility = View.INVISIBLE
                        Toast.makeText(this, "Wallet Refilled !", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Please Try Again. ", Toast.LENGTH_SHORT).show()
                    }
                }
                clearFields()
            } else {
                Toast.makeText(this, "Invalid data. Please check the fields.", Toast.LENGTH_SHORT).show()
            }
        }

        testDataBtn.setOnClickListener {
            cardNum.setText("1234567890123456")
            cardName.setText("Kaha Kurulla")
            expDate.setText("12/23")
            cvv.setText("123")
            amountText.setText("500")
        }

        getUserData(auth.currentUser!!.uid) {
            if (it){
                walletBal.text = "Credit Left :" + usersData!!.child("walletBalance").value.toString()
            }else{
                Toast.makeText(this@PessengerWalletActivity, "No Data Found", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@PessengerWalletActivity,PassengerHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        topUpBtn.setOnClickListener {
            linearLayout.visibility = View.VISIBLE
        }

    }

    private fun getUserData(uid: String, callback: (Boolean) -> Unit) {
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    usersData = dataSnapshot
                    currentWalletBalance = Integer.parseInt(dataSnapshot.child("walletBalance").value.toString())
                    walletBal.text = "Credit Left : ${currentWalletBalance}" // Update your TextView here
                    callback(true)
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
                Toast.makeText(this@PessengerWalletActivity, "Error Occurred ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateWallet(uid: String , amount : String, callback: (Boolean) -> Unit) {
        val userReference = FirebaseDatabase.getInstance().reference.child("Users").child(uid)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val money = currentWalletBalance?.plus(parseInt(amount))
                    val updates = hashMapOf(
                        "walletBalance" to money,
                    )
                    userReference.updateChildren(updates as Map<String, Any>).addOnSuccessListener {
                        callback(true)
                    }
                    callback(true)

                } else {
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
                Toast.makeText(
                    this@PessengerWalletActivity,
                    "Error Occurred ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }
    private fun  isAmountValid(amount: String): Boolean {
        if (!amount.isNullOrEmpty()) {
            val value = amount.toIntOrNull()
            return value != null && value >= 100
        }
        return false
    }
    
    private fun isCardNumberValid(cardNumber: String): Boolean {
        return cardNumber.length == 16 // Example: Validate that the card number has 16 digits
    }

    private fun isCardholderNameValid(cardholderName: String): Boolean {
        return cardholderName.isNotEmpty()
    }

    private fun isExpiryDateValid(expiryDate: String): Boolean {
        return expiryDate.matches(Regex("\\d{2}/\\d{2}")) // Example: Validate MM/YY format
    }

    private fun isCVVValid(cvv: String): Boolean {
        return cvv.length == 3 // Example: Validate that CVV has 3 digits
    }

    private fun clearFields() {
        cardNum.text.clear()
        cardName.text.clear()
        expDate.text.clear()
        cvv.text.clear()
        amountText.text.clear()
    }



}