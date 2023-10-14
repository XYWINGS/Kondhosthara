package com.example.myapplication.dataclasses

data class Driver(
      val ownerUid:String? = null,
      val name:String? = null,
      val email:String? = null,
      val phone:String? = null,
      val address:String? = null,
      val nic:String? = null,
      val type:String? = null,
      val busID : String ?= null,
      val drvHrs : Double ?= null,
      val distTravel : Double?= null,
      val uid : String ?= null,
      val status : String ? =null,
      val isJourneyStarted : Boolean ?= false,
      val permission: String ?= null
    )