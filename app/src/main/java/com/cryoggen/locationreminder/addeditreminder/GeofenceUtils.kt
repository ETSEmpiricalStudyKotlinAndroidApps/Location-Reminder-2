package com.cryoggen.locationreminder.addeditreminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import com.cryoggen.locationreminder.MainActivity
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.addeditreminder.GeofencingConstants.ACTION_GEOFENCE_EVENT
import com.cryoggen.locationreminder.map.MapReminder
import com.cryoggen.locationreminder.map.foregroundAndBackgroundLocationPermissionApproved
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}

internal object GeofencingConstants {

    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(24)
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
    const val ACTION_GEOFENCE_EVENT =
        "ACTION_GEOFENCE_EVENT"
}

private val geofencePendingIntent: PendingIntent by lazy {
    val intent = Intent(MainActivity.activity, GeofenceBroadcastReceiver::class.java)
    intent.action = ACTION_GEOFENCE_EVENT
    PendingIntent.getBroadcast(MainActivity.activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

private val geofencingClient = LocationServices.getGeofencingClient(MainActivity.activity)

@SuppressLint("MissingPermission")
fun addGeofenceForReminder(reminderId: String?, mapReminder: MapReminder) {
    val geofence = Geofence.Builder()

        // Set the request ID of the geofence. This is a string to identify this
        // geofence.
        .setRequestId(reminderId!!)

        // Set the circular region of this geofence.
        .setCircularRegion(
            mapReminder.latitude,
            mapReminder.longitude,
            GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
        )

        // Set the expiration duration of the geofence. This geofence gets automatically
        // removed after this period of time.
        .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

        // Set the transition types of interest. Alerts are only generated for these
        // transition. We track entry and exit transitions in this sample.
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

        // Create the geofence.
        .build()

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()

    geofencingClient.removeGeofences(geofencePendingIntent)?.run {
        addOnCompleteListener {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Toast.makeText(
                        MainActivity.activity, R.string.geofences_added,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                addOnFailureListener {
                    Toast.makeText(
                        MainActivity.activity, R.string.geofences_not_added,
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }
}

/**
 * Removes geofences. This method should be called after the user has granted the location
 * permission.
 */
fun removeGeofences() {
    if (!foregroundAndBackgroundLocationPermissionApproved()) {
        return
    }
    geofencingClient.removeGeofences(geofencePendingIntent)?.run {
        addOnSuccessListener {
            Log.d(TAG, MainActivity.activity.resources.getString(R.string.geofences_removed))
            Toast.makeText(MainActivity.activity, R.string.geofences_removed, Toast.LENGTH_SHORT)
                .show()
        }
        addOnFailureListener {
            Log.d(TAG, MainActivity.activity.resources.getString(R.string.geofences_not_removed))
        }
    }
}
