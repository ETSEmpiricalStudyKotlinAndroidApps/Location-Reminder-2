package com.cryoggen.locationreminder.addeditreminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cryoggen.locationreminder.main.MainActivity
import com.cryoggen.locationreminder.R

/*
 * We need to create a NotificationChannel associated with our CHANNEL_ID before sending a
 * notification.
 */
fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),

            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description =
            context.getString(R.string.notification_channel_description)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

/*
 * A Kotlin extension function for AndroidX's NotificationCompat that sends our Geofence
 * entered notification.  It sends a custom notification based on the name string associated
 * with the LANDMARK_DATA from GeofencingConstatns in the GeofenceUtils file.
 */
fun NotificationManager.sendGeofenceEnteredNotification(context: Context, GeofenceId: String) {
    val contentIntent = Intent(context, MainActivity::class.java)
    contentIntent.putExtra(GeofencingConstants.EXTRA_GEOFENCE_INDEX, GeofenceId)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val closeNotificationIntent = Intent(context, GeofenceBroadcastReceiver::class.java)
    closeNotificationIntent.action = GeofencingConstants.ACTION_CLOSE_NOTIFICATION
    val closePendingIntent = PendingIntent.getBroadcast(
        context,
        111,
        closeNotificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )


    // We use the name resource ID from the LANDMARK_DATA along with content_text to create
    // a custom message when a Geofence triggers.
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(
            context.getString(
                R.string.content_text
            )
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .addAction(
            R.drawable.ic_notification_open_button_foreground,
            context.resources.getString(R.string.open), contentPendingIntent
        )
        .addAction(
            R.drawable.ic_notification_close_button_foreground,
            context.resources.getString(R.string.close), closePendingIntent
        )
        .setSmallIcon(R.drawable.ic_notification)
        .setAutoCancel(true)
        .setColor(context.resources.getColor(R.color.secondaryDarkColor))
    val tst = builder.build()
//    tst.flags = Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
    notify(NOTIFICATION_ID, tst)
}

const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "GeofenceChannel"


