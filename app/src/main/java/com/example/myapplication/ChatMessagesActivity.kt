package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.dataclasses.Messages
import com.example.myapplication.adaptors.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.w3c.dom.Text
import com.example.myapplication.fragments.ChatFragment


class ChatMessagesActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    private lateinit var chatId: String
    private lateinit var currentUserId: String
    private lateinit var otherUserId: String
    private lateinit var currentUserName: String
    private lateinit var otherUserName: String
    lateinit var messageEditText: EditText

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageList: ArrayList<Messages>
    private lateinit var adapter: MessageAdapter

    lateinit var btn: ImageView
    lateinit var msg: TextView
    val fragmentChat = ChatFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_messages)

        btn = findViewById(R.id.imgBackBtn)
        //msg = findViewById(R.id.sentMsg)
        //popupDelete()

        btn.setOnClickListener(){
            finish()
        }

        chatId = intent.getStringExtra("chatId") ?: ""
        currentUserName = intent.getStringExtra("currentUserName") ?: ""
        otherUserName = intent.getStringExtra("otherUserName") ?: ""
        otherUserId = intent.getStringExtra("otherUserId") ?: ""

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance("https://maad-bb9db-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        currentUserId = mAuth.currentUser?.uid ?: ""

        messageList = ArrayList()
        adapter = MessageAdapter(this, messageList, currentUserId)
        messageRecyclerView = findViewById(R.id.rvMessages)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = adapter

        val sendButton: ImageButton = findViewById(R.id.imgSendBtn)
        val chatName: TextView = findViewById(R.id.tvUserName)

        chatName.text = otherUserName

        messageEditText = findViewById(R.id.edtMessage)
        sendButton.isEnabled = false
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton.isEnabled = s.toString().trim().isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageEditText.setText("")
            }
        }

        //Update read status of messages when the chat is opened
        val query = mDbRef.child("messagesZ").orderByChild("chatId").equalTo(chatId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(Messages::class.java)
                    if (message != null && message.receiverId == currentUserId && !message.isRead) {
                        message.isRead = true
                        messageSnapshot.ref.setValue(message)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })

        // Set up the ValueEventListener to listen for changes in the messages node
        mDbRef.child("messagesZ").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Messages::class.java)
                    if (message?.chatId == chatId) {
                        messageList.add(message)
                        if(message.receiverId == currentUserId){
                            messageSnapshot.child("read").ref.setValue(true)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                messageRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

    }

    fun sendMessage(message: String) {
        val timestamp = System.currentTimeMillis().toString()
        //val messageId = mDbRef.child("messages/$chatId").push().key

        //if (messageId != null) {
        val senderId = mAuth.currentUser?.uid
        val receiverId = otherUserId
        val messageId = mDbRef.push().key
        val messageObj = Messages(chatId,messageId,message, senderId, receiverId, timestamp.toLong(), false)

        mDbRef.child("messagesZ/$messageId").setValue(messageObj)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Clear message input field
                    messageEditText.setText("")
                    // Update chat status to "unread" for receiver
                    //mDbRef.child("chats/$chatId").child("status").setValue("unread")
                } else {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }
        //}
    }
}