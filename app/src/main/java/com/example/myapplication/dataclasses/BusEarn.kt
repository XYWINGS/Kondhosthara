package com.example.myapplication.dataclasses

data class BusEarn(
    val earnVal : Double? = null,
    val userCount : Int?= null,
    val done : Boolean ?= null,
    val ownerID : String ?= null,
    val busID : String ?= null,
    val date : String ?=null,
    val commission : Int?=null
)
