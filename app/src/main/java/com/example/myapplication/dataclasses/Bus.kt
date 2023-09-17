package com.example.myapplication.dataclasses

import android.location.Location
import com.google.android.libraries.places.api.model.Place
import com.google.maps.model.LatLng

data class Bus(
    val ownerUid:String? = null,
    val busName:String? = null,
    val busRegID:String? = null,
    val busPermitID:String? = null,
    val routeID:String? = null,

    val startLocation :Place? = null,
    val endLocation : Place? = null,

    val driverID:String? = null,
    val driverName:String? = null,

    val ownerPhn:String? = null,

    val passngrCount : Int ? = null,

    val crntLang:String? = null,
    val crntLong:String? = null,

    val journeyStatus: String?= null
)