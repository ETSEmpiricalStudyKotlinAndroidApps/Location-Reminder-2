package com.cryoggen.locationreminder.reciver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cryoggen.locationreminder.notification.ACTION_CLOSE_NOTIFICATION_ENTER_IN_GEOFENCE_ID
import com.cryoggen.locationreminder.notification.ACTION_CLOSE_NOTIFICATION_GEOFENCE_STATUS
import com.cryoggen.locationreminder.notification.NOTIFICATION_ENTER_IN_GEOFENCE_ID
import com.cryoggen.locationreminder.services.*


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == ACTION_CLOSE_NOTIFICATION_GEOFENCE_STATUS) {
            val intentStopService = Intent(context, RemindersService::class.java)
            context.stopService(intentStopService)
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


