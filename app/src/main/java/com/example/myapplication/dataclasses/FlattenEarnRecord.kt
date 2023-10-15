package com.example.myapplication.dataclasses

data class FlattenEarnRecord(
    val ownerID: String ?= null,
    val busId: String?= null,
    val date: String?= null,
    val earnVal: Int?= null,
    val userCount: Int?= null,
    val isDone : Boolean ?= null,
)