package com.cryoggen.locationreminder.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cryoggen.locationreminder.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapReminder(
    val googleMap: GoogleMap,
    val context: Context?,
    latitude: Double,
    longitude: Double
) {

    private val REQUEST_LOCATION_PERMISSION = 1

    var latitude = latitude
        get() = field
        set(value) {
            field = value
        }
    var longitude = longitude
        get() = field
        set(value) {
            field = value
        }
    var zoomLevel: Float = 15f
        set(value) {
            field = value
        }

    init {
        val homeLatLng = LatLng(latitude, longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        googleMap.addMarker(MarkerOptions().position(homeLatLng))
        setMapLongClick()
    }

    @SuppressLint("MissingPermission")
    fun turnOnMyLocation() {
        googleMap.isMyLocationEnabled = true
    }

    private fun setMapLongClick() {
        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(context?.getResources()?.getString(R.string.location_reminder))
            )
        }
    }


}