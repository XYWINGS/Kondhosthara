package com.example.myapplication.dataclasses

data class BusEarn(
    val earnVal : Double? = null,
    val userCount : Int?= null,
    val isDone : Boolean ?= null,
    val ownerID : String ?= null,
    val busID : String ?= null,
    val date : String ?=null
)
