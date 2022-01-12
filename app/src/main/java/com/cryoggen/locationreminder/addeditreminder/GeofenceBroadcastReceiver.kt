package com.cryoggen.locationreminder.addeditreminder

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.addeditreminder.GeofencingConstants.ACTION_CLOSE_NOTIFICATION
import com.cryoggen.locationreminder.addeditreminder.GeofencingConstants.ACTION_GEOFENCE_EVENT
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.cryoggen.locationreminder.main.MainActivity
import java.lang.Exception


/*
 * Triggered by the Geofence.  Since we only have one active Geofence at once, we pull the request
 * ID from the first Geofence, and locate it within the registered landmark data in our
 * GeofencingConstants within GeofenceUtils, which is a linear string search. If we had  very large
 * numbers of Geofence possibilities, it might make sense to use a different data structure.  We
 * then pass the Geofence index into the notification, which allows us to have a custom "found"
 * message associated with each Geofence.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    //    private var mediaPlayer = MediaPlayer.create(MainActivity.activity, R.raw.rington)
    private var mediaPlayer: MediaPlayer? = null
    private val vibrator = MainActivity.activity.getSystemService<Vibrator>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CLOSE_NOTIFICATION) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
            releaseMediaPlayer()
            stopVibratePhone()
        }
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            releaseMediaPlayer()
//            startMediaPlayer()
            vibratePhone()
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
                // Check geofence against the constants listed in GeofenceUtil.kt to see if the
                // user has entered any of the locations we track for geofences.
//                val foundIndex = GeofencingConstants.LANDMARK_DATA.indexOfFirst {
//                    it.id == fenceId
//                }
//
//                // Unknown Geofences aren't helpful to us
//                if ( -1 == foundIndex ) {
//                    Log.e(TAG, "Unknown Geofence: Abort Mission")
//                    return
//                }

                val notificationManager = ContextCompat.getSystemService(
                    context,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.sendGeofenceEnteredNotification(
                    context, GeofenceId
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun vibratePhone() {
        val pattern = longArrayOf(0, 200, 100, 300)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                //deprecated in API 26
                it.vibrate(pattern, 0)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopVibratePhone() {
        vibrator?.cancel()
    }

    private fun startMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }

        mediaPlayer = MediaPlayer.create(MainActivity.activity, R.raw.rington)
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener(MediaPlayer.OnCompletionListener() {
            it.release()
        })
    }


    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.run {
                if (isPlaying) stop()
                release()
            }
        } finally {
            mediaPlayer = null
        }
    }
}


const val TAG = "GeofenceReceiver"
