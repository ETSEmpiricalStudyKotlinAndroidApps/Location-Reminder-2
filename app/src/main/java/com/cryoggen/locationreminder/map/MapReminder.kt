package com.cryoggen.locationreminder.map

import android.annotation.SuppressLint
import android.content.Context
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.geofence.GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
import com.cryoggen.locationreminder.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CircleOptions
import java.util.*


class MapReminder(
    val googleMap: GoogleMap,
    val context: Context,
    latitude: Double = 0.0,
    longitude: Double = 0.0,

    ) {
    private var homeLatLng = LatLng(55.75253338241553, 37.617544731021034)
    private val REQUEST_LOCATION_PERMISSION = 1

    var latitude = latitude
    var longitude = longitude
    var zoomLevel: Float = 15f


    init {
        turnOnMyLocation()
        moveCameraСurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraСurrentLocation() {
        try {

           val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener {
                homeLatLng = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
            }

        } catch (e: Exception) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        }
    }

    fun switchMapLongClick(switcher: Boolean) {
        if (switcher) {
            setMapClick()
        } else {
            clearSetMapLongClick()
        }
    }

    private fun clearSetMapLongClick() {
        googleMap.setOnMapLongClickListener {}
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
                    .title(context?.getResources()?.getString(R.string.location_reminder))
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
                .title(context?.getResources()?.getString(R.string.location_reminder))
        )
        addCircleOnMarker()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    fun addCircleOnMarker() {
        googleMap.addCircle(
            CircleOptions()
                .center(LatLng(latitude, longitude))
                .radius(GEOFENCE_RADIUS_IN_METERS.toDouble())
                .strokeColor(context!!.resources.getColor(R.color.primaryColor))
                .fillColor(
                    context.resources.getColor(R.color.fill_color_circle_geofence)
                )
        )
    }

}