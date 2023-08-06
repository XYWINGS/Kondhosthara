package com.example.myapplication.dataclasses

import com.google.android.gms.maps.model.LatLng

data class Route(
    val points: List<LatLng>,
    val distance: String,
    val duration: String
)