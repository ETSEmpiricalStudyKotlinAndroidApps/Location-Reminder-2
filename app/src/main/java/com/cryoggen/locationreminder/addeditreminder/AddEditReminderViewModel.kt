package com.cryoggen.locationreminder.addeditreminder

import android.app.Activity
import android.app.Application
import androidx.lifecycle.*
import com.cryoggen.locationreminder.Event
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.data.Result.Success
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel for the Add/Edit screen.
 */
class AddEditReminderViewModel(application: Application) : AndroidViewModel(application) {
    //current geofence coordinates
    private var latitude = 0.0
    private var longitude = 0.0

    // Note, for testing and architecture purposes, it's bad practice to construct the repository
    // here. We'll show you how to fix this during the codelab
    private val remindersRepository = RemindersRepository.getRepository(application)

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()

    private val _uploadReminderDataToMap = MutableLiveData<Reminder>()
    val uploadReminderDataToMap: LiveData<Reminder> = _uploadReminderDataToMap

    private val _savedReminder = MutableLiveData<Reminder>()
    val savedReminder: LiveData<Reminder> = _savedReminder

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _loadDataFromMap = MutableLiveData<Boolean>()
    val loadDataFromMap: LiveData<Boolean> = _loadDataFromMap

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _reminderUpdatedEvent = MutableLiveData<Event<Unit>>()
    val reminderUpdatedEvent: LiveData<Event<Unit>> = _reminderUpdatedEvent

    private var reminderIdForEdtiReminder: String? = null

    private var isNewReminder: Boolean = false

    private var isDataLoaded = false

    private var reminderCompleted = false

    fun start(reminderId: String?) {
        if (_dataLoading.value == true) {
            return
        }

        this.reminderIdForEdtiReminder = reminderId
        if (reminderId == null) {
            // No need to populate, it's a new reminder
            isNewReminder = true
            return
        }
        if (isDataLoaded) {
            // No need to populate, already have data.
            return
        }

        isNewReminder = false
        _dataLoading.value = true

        viewModelScope.launch {
            remindersRepository.getReminder(reminderId).let { result ->
                if (result is Success) {
                    onReminderLoaded(result.data)
                } else {
                    onDataNotAvailable()
                }
            }
        }
    }

    private fun onReminderLoaded(reminder: Reminder) {
        _uploadReminderDataToMap.value = reminder
        title.value = reminder.title
        description.value = reminder.description
        reminderCompleted = reminder.isCompleted
        _dataLoading.value = false
        isDataLoaded = true
    }


    private fun onDataNotAvailable() {
        _dataLoading.value = false
    }

    // Called when clicking on fab.
    fun loadGeofenceFromMap(){
        _loadDataFromMap.value=true
    }

    fun updateСoordinatesFromMap(latitude:Double,longitude:Double){
        _loadDataFromMap.value=false
        this.latitude = latitude
        this.longitude = longitude
        saveReminder()
    }


    fun saveReminder() {
        val currentTitle = title.value
        val currentDescription = description.value ?: ""
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.getUid().toString()
        if ((currentTitle == null) || (latitude == 0.0 && longitude == 0.0)) {
            _snackbarText.value = Event(R.string.empty_reminder_message)
            return
        }

        val currentReminderId = reminderIdForEdtiReminder
        if (isNewReminder || currentReminderId == null) {
            val newReminder = Reminder(
                currentTitle,
                currentDescription,
                currentUserUID,
                latitude,
                longitude
            )
            createReminder(
                newReminder
            )
        } else {
            val reminder = Reminder(
                currentTitle,
                currentDescription,
                currentUserUID,
                latitude,
                longitude,
                reminderCompleted,
                currentReminderId
            )
            updateReminder(reminder)
        }
    }

    private fun createReminder(newReminder: Reminder) {

        //we track the reminder that we will save in order to create geofence tracking
        _savedReminder.value = newReminder

        viewModelScope.launch {
            remindersRepository.saveReminder(newReminder)
            _reminderUpdatedEvent.value = Event(Unit)
        }
    }

    private fun updateReminder(reminder: Reminder) {
        if (isNewReminder) {
            throw RuntimeException("updateReminder() was called but reminder is new.")
        }

        //we track the reminder that we will update in order to create a geofencing tracking
        _savedReminder.value = reminder

        viewModelScope.launch {
            remindersRepository.saveReminder(reminder)
            _reminderUpdatedEvent.value = Event(Unit)
        }
    }



}
