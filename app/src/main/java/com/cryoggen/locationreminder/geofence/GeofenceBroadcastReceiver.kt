package com.cryoggen.locationreminder.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.addeditreminder.*
import com.cryoggen.locationreminder.geofence.GeofencingConstants.ACTION_GEOFENCE_EVENT
import com.cryoggen.locationreminder.servises.*
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

        if (intent.action == ACTION_CLOSE_NOTIFICATION_GEOFENCE_STATUS) {
            val intent = Intent(context, RemindersService::class.java)
            context.stopService(intent)
        }


        if (intent.action == ACTION_CLOSE_NOTIFICATION_ENTER_IN_GEOFENCE_ID) {
            //stop work service for sound
            stopSound(context)

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ENTER_IN_GEOFENCE_ID)

        }

        if (intent.action == ACTION_GEOFENCE_EVENT) {


            val geofencingEvent = GeofencingEvent.fromIntent(intent)


            val geofenceHelper = GeofenceHelper(context)
            if (geofencingEvent.hasError()) {
                val errorMessage = geofenceHelper.errorMessage(context, geofencingEvent.errorCode)
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

                val intentDeactivateReminder = Intent(context, RemindersService::class.java)
                intentDeactivateReminder.putExtra(DEACIVATE_REMINDER_ID, GeofenceId)
                intentDeactivateReminder.action = ACTION_DEACTIVATE_REMINDER
                context.startService(intentDeactivateReminder)


                val notificationManager = ContextCompat.getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager
                notificationManager.cancelAllNotifications()
                val notification = sendGeofenceEnteredNotification(
                    context, GeofenceId
                )
                notificationManager.notify(NOTIFICATION_ENTER_IN_GEOFENCE_ID, notification)

                //start work service for sound
                startSound(context)

            }
        }
    }


}

const val TAG = "GeofenceReceiver"
