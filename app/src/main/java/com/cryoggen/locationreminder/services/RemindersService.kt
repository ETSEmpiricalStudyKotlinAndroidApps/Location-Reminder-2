package com.cryoggen.locationreminder.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.os.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.notification.NOTIFICATION_ENTER_IN_GEOFENCE_ID
import com.cryoggen.locationreminder.notification.NOTIFICATION_GEOFENCE_STATUS_ID
import com.cryoggen.locationreminder.notification.cancelAllNotifications
import com.cryoggen.locationreminder.notification.sendGeofenceEnteredNotification
import com.cryoggen.locationreminder.reminders.RemindersViewModel
import com.google.android.gms.location.*


class RemindersService : LifecycleService() {

    var reminderMinDistance: Reminder? = null
    var minDistanceLocation = -1.0F

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    var sumActiveReminders = -1

    private var listReminders = listOf<Reminder>()

    private val viewModel by lazy { RemindersViewModel(application) }

    private val context = this

    val notificationManager by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_DEACTIVATE_ALL_REMINDERS -> deactivateAllReminders()
            else -> {
                startNotifictionForegraund()

                createLocationRequest()
                observeUpdateReminder()
                subscribeToLocationUpdates()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        unsubscribeToLocationUpdates()
        stopSelf()
    }

    private fun startNotifictionForegraund() {
        startForeground(
            NOTIFICATION_GEOFENCE_STATUS_ID,
            com.cryoggen.locationreminder.notification.sendNotificationStatus(
                context,
                resources.getString(R.string.loading_notification_text)
            )
        )
    }

    private fun sendNotificationStatus() {
        val textNotification: String =
            if (reminderMinDistance == null) getString(R.string.loading_notification_text) else "${reminderMinDistance!!.title} · " + getString(
                R.string.after
            ) + " · $minDistanceLocation" + "km."

        notificationManager.notify(
            NOTIFICATION_GEOFENCE_STATUS_ID,
            com.cryoggen.locationreminder.notification.sendNotificationStatus(
                context,
                textNotification
            )
        )
    }

    private fun sendNotificationEnterGeofence() {
        if (sumActiveReminders > 0) sumActiveReminders--
        viewModel.completeReminder(reminderMinDistance!!, true)
        startSoundAndNotification(this, reminderMinDistance!!.id)
    }

    private fun deactivateAllReminders() {
        sumActiveReminders = 0
        for (reminder in listReminders) {
            if (reminder.isActive) {
                viewModel.completeReminder(reminder, true)
            }
        }

    }

    private fun observeUpdateReminder() {
        viewModel.items.observe(this, Observer {
            listReminders = it
            updateSumActiveReminders()
        })
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

    fun createLocationRequest() {

        createLocationSetting()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                createLocationSetting()

                currentLocation = locationResult.lastLocation
                determinesNearestGeofence()
                checkSumActiveReminders()
                if (minDistanceLocation == 0.0F) {
                    sendNotificationEnterGeofence()

                    //it is necessary that notifications are not generated until the distance to the next geofence is updated.
                    minDistanceLocation = -0.0F
                }
            }
        }

    }

    private fun createLocationSetting() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
        }
        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
    }

    private fun checkSumActiveReminders() {
        when (sumActiveReminders) {
            in 1..100 -> {
                sendNotificationStatus()
            }
            0 -> {
                stopSelf()
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun subscribeToLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            //checks if there are permissions to access geolocation

        }
    }

    fun unsubscribeToLocationUpdates() {

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    stopSelf()
                } else {

                }
            }

        } catch (unlikely: SecurityException) {

        }

    }

    fun determinesNearestGeofence() {
        reminderMinDistance = null
        minDistanceLocation = Float.MAX_VALUE
        var reminderLocation = Location("location")
        var reminderDistanceLocation = 0.0F
        if (sumActiveReminders > 0) {

            for (reminder in listReminders) {

                if (reminder.isActive) {
                    reminderLocation.latitude = reminder.latitude
                    reminderLocation.longitude = reminder.longitude

                    if (currentLocation != null) {
                        reminderDistanceLocation = currentLocation!!.distanceTo(reminderLocation)
                        if (minDistanceLocation > reminderDistanceLocation) {
                            minDistanceLocation = reminderDistanceLocation
                            reminderMinDistance = reminder
                        }

                    }

                }

            }
            minDistanceLocation = metersToKilometers(minDistanceLocation)
        }
    }

    private fun metersToKilometers(minDistanceLocation: Float): Float {

        return if ((minDistanceLocation - GEOFENCE_RADIUS_IN_METERS) >= 0) ((((minDistanceLocation - GEOFENCE_RADIUS_IN_METERS) * 0.1).toInt()) / 100.0f) else 0.0F
    }
}


const val ACTION_DEACTIVATE_ALL_REMINDERS = "com.cryoggen.action.ACTION_DEACTIVATE_ALL_REMINDERS"


const val GEOFENCE_RADIUS_IN_METERS = 200f
const val EXTRA_GEOFENCE_INDEX = "com.cryoggen.GEOFENCE_INDEX"
