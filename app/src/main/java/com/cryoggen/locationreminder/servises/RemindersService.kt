package com.cryoggen.locationreminder.servises

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.addeditreminder.*
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersRepository
import com.cryoggen.locationreminder.geofence.GeofenceHelper
import com.cryoggen.locationreminder.main.MainActivity
import com.cryoggen.locationreminder.reminders.RemindersViewModel
import com.cryoggen.locationreminder.statistics.getActiveAndCompletedStats
import kotlinx.coroutines.launch


class RemindersService : LifecycleService() {

    private val geofenceHelper by lazy { GeofenceHelper(this)}

    private var activeReminders = 0

    private var listReminders = listOf<Reminder>()

    private var stopWork = false

    private val viewModel by lazy { RemindersViewModel(application) }

    private val context = this

    val notificationManager by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {

            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                for (i in 1..500) {
                    if (stopWork == true) {
                        stopForeground(true)
                        break
                    }

                    sendNotification()
                    println("11111 " + i)
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }


        private fun sendNotification() {
            val textNotification: String = if (activeReminders == 1) getString(R.string.reminder) else "$activeReminders " + getString(
                        R.string.reminders_active
                    )

            notificationManager.notify(
                NOTIFICATION_ID,
                sendNotificationStatus(context, textNotification)
            )

        }


    }

    override fun onCreate() {
        super.onCreate()
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
        observeReminderIdRemoveGeofence()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        startForeground(
            NOTIFICATION_ID,
            sendNotificationStatus(this, resources.getString(R.string.loading_notification_text))
        )
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        // If we get killed, after returning from here, restart

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWork = true
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()

    }

    private fun observeReminderIdRemoveGeofence() {
        viewModel.items.observe(this, Observer {
            listReminders = it
            refreshGeofenseReminders()
        })
    }

    fun refreshGeofenseReminders() {
        var sumReminders = 0
        for (reminder in listReminders) {
            if (reminder.isActive) {
                sumReminders++
                geofenceHelper.removeAllGeofences()
                geofenceHelper.addGeofenceForReminder(reminder.id,reminder.latitude,reminder.longitude)
            }
        }
        if (sumReminders == 0) {
            geofenceHelper.removeAllGeofences()
            stopWork = true
        } else {
            activeReminders = sumReminders
        }
    }

}
