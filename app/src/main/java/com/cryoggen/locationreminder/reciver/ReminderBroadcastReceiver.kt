package com.cryoggen.locationreminder.reciver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cryoggen.locationreminder.addeditreminder.*
import com.cryoggen.locationreminder.servises.*



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

    }


}

const val TAG = "GeofenceReceiver"
