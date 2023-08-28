package com.example.myapplication.dataclasses

data class Owner(
    val uid:String? = null,
    val name:String? = null,
    val email:String? = null,
    val phone:String? = null,
    val address:String? = null,
    val nic:String? = null,
    val type:String? = null,
    var buses: List<Bus>? = null,
    var drivers : List<Driver> ? = null
)