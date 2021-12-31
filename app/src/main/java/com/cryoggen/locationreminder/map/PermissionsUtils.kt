package com.cryoggen.locationreminder.map

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.main.MainActivity
import com.cryoggen.locationreminder.map.ConstantsPermissions.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.cryoggen.locationreminder.map.ConstantsPermissions.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar

object ConstantsPermissions {
    const val TAG = "MainActivity"
    const val SIGN_IN_RESULT_CODE = 1001
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    const val LOCATION_PERMISSION_INDEX = 0
    const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
}

private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.Q

private var chekStatusLocationSettingsAndStartGeofence = MutableLiveData<Boolean>()
val _chekStatusLocationSettingsAndStartGeofence: LiveData<Boolean> = chekStatusLocationSettingsAndStartGeofence

fun checkPermissionsAndStartGeofencing() {
    if (foregroundAndBackgroundLocationPermissionApproved()) {
        checkDeviceLocationSettingsAndStartGeofence()
    } else {
        requestForegroundAndBackgroundLocationPermissions()
    }
}


private fun requestForegroundAndBackgroundLocationPermissions() {
    if (foregroundAndBackgroundLocationPermissionApproved())
        return
    var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = when {
        runningQOrLater -> {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        }
        else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
    }
    Log.d(ConstantsPermissions.TAG, "Request foreground only location permission")
    ActivityCompat.requestPermissions(
        MainActivity.activity,
        permissionsArray,
        resultCode
    )
}

 fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        MainActivity.activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
    val backgroundPermissionApproved =
        if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        MainActivity.activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    return foregroundLocationApproved && backgroundPermissionApproved
}

 fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(MainActivity.activity)
    val locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build())
    locationSettingsResponseTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException && resolve) {
            try {
                exception.startResolutionForResult(
                    MainActivity.activity,
                    ConstantsPermissions.REQUEST_TURN_DEVICE_LOCATION_ON
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(
                    ConstantsPermissions.TAG,
                    "Error getting location settings resolution: " + sendEx.message
                )
            }
        } else {
            Snackbar.make(
                MainActivity.activity.findViewById(R.id.drawer_layout),
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                checkDeviceLocationSettingsAndStartGeofence()
            }.show()
        }
    }
    locationSettingsResponseTask.addOnCompleteListener {
        if (it.isSuccessful) {
            chekStatusLocationSettingsAndStartGeofence.value = true
        }
    }
}