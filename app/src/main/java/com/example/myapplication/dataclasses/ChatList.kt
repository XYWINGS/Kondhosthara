package com.example.myapplication.dataclasses

data class ChatList(
    val chatId: String,
    val otherUserId: String,
    val chatName:String,
    val lastMessage: String?,
    val lastMessageTime: Long?,
    val unreadCount: Int
)
