package com.example.myapplication.dataclasses

data class Messages(
    val chatId:String? = null,
    val messageId:String? = null,
    val message: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val time: Long = 0L,
    var isRead: Boolean = false
)
