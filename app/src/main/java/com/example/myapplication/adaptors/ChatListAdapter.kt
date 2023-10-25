package com.example.myapplication.adaptors

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.fragments.ChatFragment
import com.example.myapplication.ChatMessagesActivity
import com.example.myapplication.dataclasses.ChatList
import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.util.*


class ChatListAdapter(
    private val context: Context,
    //private var chatList: List<ChatList>,
    //private val currentUserId: String
):RecyclerView.Adapter<ChatListAdapter.ViewHolder>(){

    var chatList: List<ChatList> = mutableListOf()


    fun updateData(newList: MutableList<ChatList>) {
        chatList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){

        val userNameTextView: TextView = itemView.findViewById(R.id.tvChatName)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.tvRecMsg)
        val timeTextView: TextView = itemView.findViewById(R.id.tvRecTime)
        val unreadCountTextView: TextView = itemView.findViewById(R.id.tvMsgCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder{
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.chat_list,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.userNameTextView.text = chat.chatName
        holder.lastMessageTextView.text = chat.lastMessage
        holder.timeTextView.text = getFormattedTime(chat.lastMessageTime!!)

        if (chat.unreadCount > 0) {
            holder.unreadCountTextView.visibility = View.VISIBLE
            holder.unreadCountTextView.text = chat.unreadCount.toString()
        } else {
            holder.unreadCountTextView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatMessagesActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            intent.putExtra("otherUserName", chat.chatName)
            intent.putExtra("otherUserId", chat.otherUserId)

            context.startActivity(intent)
        }

    }

    fun updateChatList(newList: List<ChatList>) {
        chatList = newList
        notifyDataSetChanged()
    }

    fun updateUnreadCount(chatId: String, unreadCount: Int) {
        val index = chatList.indexOfFirst { it.chatId == chatId }
        if (index != -1) {
            val updatedChat = chatList[index].copy(unreadCount = unreadCount)
            val updatedList = chatList.toMutableList()
            updatedList[index] = updatedChat
            updateChatList(updatedList)
        }
    }

    private fun getFormattedTime(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(calendar.time)
    }

}