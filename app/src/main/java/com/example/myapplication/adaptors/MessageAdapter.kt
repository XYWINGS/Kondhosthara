package com.example.myapplication.adaptors

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.dataclasses.Messages
import com.example.myapplication.R
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat
import java.util.*

class MessageAdapter(
    private val context: Context,
    private var messageList: List<Messages>,
    private val currentUserId: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    // private val VIEW_TYPE_EDITABLE = 3


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return if (viewType == VIEW_TYPE_SENT) {
            SentMessageViewHolder(inflater.inflate(R.layout.message_sent, parent, false))
        } else {
            ReceivedMessageViewHolder(inflater.inflate(R.layout.message_received, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(messageList[position])
        } else {
            (holder as ReceivedMessageViewHolder).bind(messageList[position])
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sentMsg)
        private val timeTextView: TextView = itemView.findViewById(R.id.sentTime)

        fun bind(message: Messages) {
            messageTextView.text = message.message
            timeTextView.text = getTimeText(message.time)

            // Add OnClickListener to messageTextView
            messageTextView.setOnClickListener {
                val popup = PopupMenu(context, messageTextView)
                popup.menuInflater.inflate(R.menu.popup_message, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.edit -> {
                            // Create a dialog box with an EditText view to get the new message from the user
                            val dialog = Dialog(context)
                            dialog.setContentView(R.layout.edit_dialog)
                            val editText = dialog.findViewById<EditText>(R.id.etMessage)
                            editText.setText(message.message)

                            // Set the onClickListener for the "Save" button
                            val saveButton = dialog.findViewById<ImageButton>(R.id.btnUpdateMsg)
                            saveButton.setOnClickListener {
                                val newMessage = editText.text.toString().trim()
                                if (newMessage.isNotEmpty()) {
                                    // Update the message in the database
                                    val messageId = message.messageId
                                    val messageRef =
                                        FirebaseDatabase.getInstance("https://maad-bb9db-default-rtdb.asia-southeast1.firebasedatabase.app")
                                            .getReference("messagesZ/$messageId")
                                    messageRef.child("message").setValue(newMessage)


                                    dialog.dismiss()
                                } else {
                                    editText.error = "Message cannot be empty"
                                }
                            }

                            // Set the onClickListener for the "Cancel" button
                            val cancelButton =
                                dialog.findViewById<ImageButton>(R.id.btnUpdateMsgCancel)
                            cancelButton.setOnClickListener {
                                dialog.dismiss()
                            }

                            dialog.show()
                        }
                        R.id.delete -> {
                            // Remove message from database
                            val messageId = message.messageId
                            val messageRef =
                                FirebaseDatabase.getInstance("https://maad-bb9db-default-rtdb.asia-southeast1.firebasedatabase.app")
                                    .getReference("messagesZ/$messageId")
                            messageRef.removeValue()

                        }


                    }
                    true

                }
                popup.show()
            }
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.recMsg)
        private val timeTextView: TextView = itemView.findViewById(R.id.recTime)

        fun bind(message: Messages) {
            messageTextView.text = message.message
            timeTextView.text = getTimeText(message.time)
        }
    }

    private fun getTimeText(timeSent: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeSent
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time)
    }


}