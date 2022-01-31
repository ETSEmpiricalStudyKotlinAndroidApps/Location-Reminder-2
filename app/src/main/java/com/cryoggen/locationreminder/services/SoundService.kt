package com.cryoggen.locationreminder.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.main.MainActivity
import com.cryoggen.locationreminder.notification.NOTIFICATION_ENTER_IN_GEOFENCE_ID
import com.cryoggen.locationreminder.notification.sendGeofenceEnteredNotification


class SoundService : Service() {

    val wakeLock: PowerManager.WakeLock by lazy {     (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationReminder::MyWakelockTag").apply {
            acquire()
        }
    } }


    lateinit var mediaPlayer: MediaPlayer
    lateinit var vibrator: Vibrator
    val vibratePattern = longArrayOf(0, 400, 800, 600, 800, 800, 800, 1000)
    val amplitudesVibration = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)

    var soundStatus = false

    val notificationManager by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }

    private var reminderId = ""

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!soundStatus) {
            initializedSound()
            wakeLock.release()
            startSound()
        }
        reminderId = intent?.getStringExtra(REMINDER_NOTIFICATION_ID)!!
        createForegroundNotification()


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock.acquire()
        stopSound()
        stopSelf()
    }

    private fun createForegroundNotification() {
        val notification = sendGeofenceEnteredNotification(
            this, reminderId
        )
        startForeground(NOTIFICATION_ENTER_IN_GEOFENCE_ID, notification)

    }


    fun initializedSound() {

        mediaPlayer = MediaPlayer.create(this, R.raw.rington)

         vibrator  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =  this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        mediaPlayer.isLooping = true
    }

    @SuppressLint("MissingPermission")
    fun startSound() {
        soundStatus = true
        mediaPlayer.start()

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, amplitudesVibration, 0))
        } else {
            vibrator.vibrate(vibratePattern, 0)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopSound() {
        soundStatus = false
        mediaPlayer.stop()
        mediaPlayer.release()
        vibrator.cancel()
    }
}

fun startSoundAndNotification(context: Context, reminderId: String) {
    val intentService = Intent(context, SoundService::class.java)
    intentService.putExtra(REMINDER_NOTIFICATION_ID, reminderId)
    startForegroundService(context, intentService)
}

fun stopSoundAndNotification(context: Context) {
    val intentService = Intent(context, SoundService::class.java)
    context.stopService(intentService)
}

const val REMINDER_NOTIFICATION_ID = "com.cryoggen.REMINDER_NOTIFICATION_ID"

