package com.cryoggen.locationreminder.geofence

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.*
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersRepository
import com.cryoggen.locationreminder.geofence.GeofencingConstants.ACTION_STOP_GEOFENCE_SERVICE


class GeofenceService : LifecycleService() {

    private val geofenceHelper = GeofenceHelper(this)

    private val remindersRepository = RemindersRepository.getRepository(application)

    private var items = MutableLiveData<List<Reminder>>()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        observeReminderIdRemoveGeofence()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        items.switchMap { remindersRepository.observeReminders() }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun observeReminderIdRemoveGeofence() {
        items.observe(this, Observer {

        })
    }
}