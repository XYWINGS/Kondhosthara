package com.example.myapplication.interfaces

import com.example.myapplication.dataclasses.Route
import com.google.android.gms.maps.model.LatLng

import org.json.JSONException
import org.json.JSONObject

class DirectionsParser {

    fun parse(response: String?): Route? {
        if (response == null || response.isEmpty()) {
            return null
        }

        try {
            val jsonResponse = JSONObject(response)
            val routesArray = jsonResponse.getJSONArray("routes")
            if (routesArray.length() > 0) {
                val routeObject = routesArray.getJSONObject(0)
                val legsArray = routeObject.getJSONArray("legs")
                if (legsArray.length() > 0) {
                    val legObject = legsArray.getJSONObject(0)
                    val distanceObject = legObject.getJSONObject("distance")
                    val durationObject = legObject.getJSONObject("duration")

                    val distance = distanceObject.getString("text")
                    val duration = durationObject.getString("text")

                    val points = decodePolyline(routeObject.getJSONObject("overview_polyline").getString("points"))

                    return Route(points, distance, duration)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly: MutableList<LatLng> = mutableListOf()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(
                lat.toDouble() / 1e5,
                lng.toDouble() / 1e5
            )
            poly.add(latLng)
        }
        return poly
    }
}