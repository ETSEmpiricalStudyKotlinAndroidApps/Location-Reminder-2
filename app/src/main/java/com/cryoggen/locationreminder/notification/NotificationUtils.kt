package com.cryoggen.locationreminder.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cryoggen.locationreminder.main.MainActivity
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.reciver.GeofenceBroadcastReceiver
import com.cryoggen.locationreminder.services.EXTRA_GEOFENCE_INDEX


//creates channel for notifications that are triggered when we enter the geofence
fun createChannelGeofenceEnterNotifications(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ENTER_IN_GEOFENCE_ID,
            context.getString(R.string.channel_name_enter_in_geofence),
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.apply {
            enableLights(true)
            lightColor = Color.RED
            enableVibration(false)
            description =
                context.getString(R.string.enter_in_geofence_notifications_description)
            setSound(null, null)

        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

//creates channel for notifications that fire if there is an active reminder
fun createChannelGeofenceStatusNotification(context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_GEOFENCE_STATUS_ID,
            context.getString(R.string.channel_name_geofence_status),
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.apply {
            enableLights(true)
            lightColor = Color.RED
            enableVibration(false)
            description =
                context.getString(R.string.geofence_status_notification_channel_description)
            setSound(null, null)

        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

//creates a notification when the device enters the geofence
fun sendGeofenceEnteredNotification(context: Context, reminderId: String): Notification {
    val contentIntent = Intent(context, MainActivity::class.java)
    contentIntent.action = ACTION_CLOSE_NOTIFICATION_ENTER_IN_GEOFENCE_ID
    contentIntent.putExtra(EXTRA_GEOFENCE_INDEX, reminderId)

    val pendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ENTER_IN_GEOFENCE_ID,
        contentIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )


    val closeNotificationIntent = Intent(context, GeofenceBroadcastReceiver::class.java)
    closeNotificationIntent.action = ACTION_CLOSE_NOTIFICATION_ENTER_IN_GEOFENCE_ID

    val closePendingIntent = PendingIntent.getBroadcast(
        context,
        NOTIFICATION_ENTER_IN_GEOFENCE_ID,
        closeNotificationIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )


    return NotificationCompat.Builder(context, CHANNEL_ENTER_IN_GEOFENCE_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(
            context.getString(
                R.string.content_text
            )
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .addAction(
            R.drawable.ic_notification_open_button_foreground,
            context.resources.getString(R.string.open), pendingIntent
        )
        .addAction(
            R.drawable.ic_notification_close_button_foreground,
            context.resources.getString(R.string.close), closePendingIntent
        )
        .setSmallIcon(R.drawable.ic_notification)
        .setAutoCancel(true)
        .setColor(context.resources.getColor(R.color.colorRed))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOngoing(true)
        .setDefaults(0)
        .setSound(null)
        .setVibrate(null)
        .build()

}

//sends a notification when there is an active reminder
fun sendNotificationStatus(context: Context, textTitle: String): Notification {
    val pendingIntent: PendingIntent =
        Intent(context, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                context,
                NOTIFICATION_GEOFENCE_STATUS_ID,
                notificationIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        }

    val disableButtonPendingIntent =
        Intent(context, GeofenceBroadcastReceiver::class.java).let { notificationIntent ->
            notificationIntent.action = ACTION_CLOSE_NOTIFICATION_GEOFENCE_STATUS
            PendingIntent.getBroadcast(
                context,
                NOTIFICATION_GEOFENCE_STATUS_ID,
                notificationIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        }


    return NotificationCompat.Builder(context, CHANNEL_GEOFENCE_STATUS_ID)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentTitle(textTitle)
        .setColor(context.resources.getColor(R.color.colorRed))
        .setSmallIcon(R.drawable.ic_notification)
        .setContentIntent(pendingIntent)
        .setTicker(textTitle)
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_notification_close_button_foreground,
            context.resources.getString(R.string.disable), disableButtonPendingIntent
        )
        .build()
}


fun NotificationManager.cancelAllNotifications() {
    cancelAll()
}

const val ACTION_CLOSE_NOTIFICATION_ENTER_IN_GEOFENCE_ID =
    "com.cryoggen.action.ACTION_CLOSE_NOTIFICATION_ENTER_IN_GEOFENCE_ID"

const val ACTION_CLOSE_NOTIFICATION_GEOFENCE_STATUS =
    "com.cryoggen.action.ACTION_CLOSE_NOTIFICATION_GEOFENCE_STATUS"

const val CHANNEL_ENTER_IN_GEOFENCE_ID = "CHANNEL_ENTER_IN_GEOFENCE_ID"
const val CHANNEL_GEOFENCE_STATUS_ID = "CHANNEL_GEOFENCE_STATUS_ID"

const val NOTIFICATION_ENTER_IN_GEOFENCE_ID = 77
const val NOTIFICATION_GEOFENCE_STATUS_ID = 33



