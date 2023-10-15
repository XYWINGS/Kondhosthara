package com.example.myapplication.dataclasses

data class GroupedOwnerData(
    val ownerId: String,
    val busDataList: List<BusEarn>
)