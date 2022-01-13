package com.cryoggen.locationreminder.addeditreminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.addeditreminder.GeofencingConstants.ACTION_CLOSE_NOTIFICATION
import com.cryoggen.locationreminder.addeditreminder.GeofencingConstants.ACTION_GEOFENCE_EVENT
import com.cryoggen.locationreminder.sound.Sound
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent


/*
 * Triggered by the Geofence.  Since we only have one active Geofence at once, we pull the request
 * ID from the first Geofence, and locate it within the registered landmark data in our
 * GeofencingConstants within GeofenceUtils, which is a linear string search. If we had  very large
 * numbers of Geofence possibilities, it might make sense to use a different data structure.  We
 * then pass the Geofence index into the notification, which allows us to have a custom "found"
 * message associated with each Geofence.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val intentService = Intent(context, Sound::class.java)

        if (intent.action == ACTION_CLOSE_NOTIFICATION) {
            //stop work service for sound
            context.stopService(intentService)

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)

        }

        if (intent.action == ACTION_GEOFENCE_EVENT) {


            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, context.getString(R.string.geofence_entered))


                val GeofenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                val notificationManager = ContextCompat.getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager
                notificationManager.cancelNotifications()
                notificationManager.sendGeofenceEnteredNotification(
                    context, GeofenceId
                )
                //start work service for sound
                context.startService(intentService)
            }
        }
    }



}

const val TAG = "GeofenceReceiver"
