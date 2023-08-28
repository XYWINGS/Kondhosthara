package com.example.myapplication.dataclasses

data class Bus(
    val ownerUid:String? = null,
    val busName:String? = null,
    val routeID:String? = null,
    val driverID:String? = null,
    val driverName:String? = null,
    val ownerPhn:String? = null,
    val passngrCount : Int ? = null,
    val crntLang:String? = null,
    val crntLong:String? = null
)