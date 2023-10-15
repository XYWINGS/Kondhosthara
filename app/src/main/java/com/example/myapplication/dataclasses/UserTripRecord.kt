package com.example.myapplication.dataclasses

data class UserTripRecord(
    val origin:String? = null,
    val destination:String? = null,
    val distance: String ?= null,
    val cost: String ?= null,
    val startTime: HashMap<String, Any>? = null,
    val endTime: HashMap<String, Any>? =  hashMapOf(
        "hours" to "",
        "minutes" to ""
    ),
    val busID:String? = null,
)

