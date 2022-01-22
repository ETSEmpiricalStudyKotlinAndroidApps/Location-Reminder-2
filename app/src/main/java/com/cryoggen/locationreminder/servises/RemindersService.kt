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

    var stopWork = false

    var sumActiveReminders = -1

    private val geofenceHelper by lazy { GeofenceHelper(this) }

    private var listReminders = listOf<Reminder>()

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

            try {
                for (i in 1..500) {
                    when (sumActiveReminders) {
                        in 1..100 -> {
                            sendNotification()
                            println("11111 " + i)
                        }
                        0 -> {
                            stopForeground(true)
                            break
                        }
                    }
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            stopSelf(msg.arg1)

        }


        private fun sendNotification() {
            val textNotification: String =
                if (sumActiveReminders == 1) getString(R.string.reminder) else "$sumActiveReminders " + getString(
                    R.string.reminders_active
                )

            notificationManager.notify(
                NOTIFICATION_GEOFENCE_STATUS_ID,
                sendNotificationStatus(context, textNotification)
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        startNotifictionForegraund()
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        observeUpdateReminder()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()

    }

    private fun startNotifictionForegraund() {
        startForeground(
            NOTIFICATION_GEOFENCE_STATUS_ID,
            sendNotificationStatus(context, resources.getString(R.string.loading_notification_text))
        )
    }

    private fun deactivateAllReminders() {
        for (reminder in listReminders) {
            if (reminder.isActive) {
                viewModel.completeReminder(reminder, true)
            }
        }
        geofenceHelper.removeAllGeofences()
    }

    private fun observeUpdateReminder() {
        viewModel.items.observe(this, Observer {
            listReminders = it
            serviceManagement()
        })
    }

    private fun serviceManagement() {
        updateSumActiveReminders()
      //  refreshGeofenseReminders()
    }


    fun updateSumActiveReminders() {
        var sumReminders = 0
        for (reminder in listReminders) {
            if (reminder.isActive) {
                sumReminders++
            }
        }
        sumActiveReminders = sumReminders
    }

    fun refreshGeofenseReminders() {
        geofenceHelper.removeAllGeofences()
        if (sumActiveReminders > 0) {
            for (reminder in listReminders) {
                if (reminder.isActive) {
                    geofenceHelper.addGeofenceForReminder(
                        reminder.id,
                        reminder.latitude,
                        reminder.longitude
                    )
                }
            }
        }
    }

}
