package com.cryoggen.locationreminder.map

import android.annotation.SuppressLint
import android.content.Context
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.services.GEOFENCE_RADIUS_IN_METERS
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CircleOptions


class MapReminder(
    private val googleMap: GoogleMap,
    val context: Context,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    ) {

    private var zoomLevel: Float = 15f


    init {
        turnOnMyLocation()
    }

    //whether to listen for clicks on the map
    fun switchOnMapClick(switcher: Boolean) {
        if (switcher) {
            setMapClick()
        } else {
            clearSetMapClick()
        }
    }

    //disables listening for clicks on the map
    private fun clearSetMapClick() {
        googleMap.setOnMapClickListener {}
    }

    @SuppressLint("MissingPermission")
    fun turnOnMyLocation() {
        googleMap.isMyLocationEnabled = true
    }

    private fun setMapClick() {
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(context.resources?.getString(R.string.location_reminder))
            )
            latitude = latLng.latitude
            longitude = latLng.longitude
            addCircleOnMarker()
        }
    }

    fun addMarker() {
        val latLng = LatLng(latitude, longitude)
        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(context.resources?.getString(R.string.location_reminder))
        )
        addCircleOnMarker()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    private fun addCircleOnMarker() {
        googleMap.addCircle(
            CircleOptions()
                .center(LatLng(latitude, longitude))
                .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())
                .strokeColor(context.resources.getColor(R.color.primaryColor))
                .fillColor(
                    context.resources.getColor(R.color.fill_color_circle_geofence)
                )
        )
    }

}