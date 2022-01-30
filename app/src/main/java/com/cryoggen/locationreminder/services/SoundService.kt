package com.cryoggen.locationreminder.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import com.cryoggen.locationreminder.R


class SoundService : Service() {
    lateinit var mediaPlayer: MediaPlayer
    lateinit var vibrator: Vibrator
    val vibratePattern = longArrayOf(0, 400, 800, 600, 800, 800, 800, 1000)
    val amplitudesVibration = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.rington)
        vibrator = ContextCompat.getSystemService(
            this,
            Vibrator::class.java
        ) as Vibrator
    }
        @SuppressLint("MissingPermission")
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

            mediaPlayer.isLooping = true
            mediaPlayer.start()

            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, amplitudesVibration, 0))
            } else {
                vibrator.vibrate(vibratePattern, 0)
            }
            return START_STICKY
        }

        @SuppressLint("MissingPermission")
        override fun onDestroy() {
            super.onDestroy()
            mediaPlayer.release()
            vibrator.cancel()

    }
}

fun startSound(context: Context){
    val intentService = Intent(context, SoundService::class.java)
    context.startService(intentService)
}
fun stopSound(context: Context){
    val intentService = Intent(context, SoundService::class.java)
    context.stopService(intentService)
}