package com.cryoggen.locationreminder.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.addeditreminder.GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
import com.cryoggen.locationreminder.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CircleOptions

import com.google.android.gms.maps.model.Circle
import java.util.*


class MapReminder(
    val googleMap: GoogleMap,
    val context: Context?,
    latitude: Double = 0.0,
    longitude: Double = 0.0

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
        val client = FusedLocationProviderClient(MainActivity.activity)
        val location = client.lastLocation
        location.addOnCompleteListener {
            homeLatLng = LatLng( it.result.latitude, it.result.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        }
    }

    fun switchMapLongClick(switcher: Boolean) {
        if (switcher) {
            setMapLongClick()
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

    private fun setMapLongClick() {
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