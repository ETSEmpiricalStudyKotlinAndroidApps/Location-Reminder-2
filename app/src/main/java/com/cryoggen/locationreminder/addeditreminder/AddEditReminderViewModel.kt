
package com.cryoggen.locationreminder.addeditreminder

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

    // Note, for testing and architecture purposes, it's bad practice to construct the repository
    // here. We'll show you how to fix this during the codelab
    private val remindersRepository = RemindersRepository.getRepository(application)

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _reminderUpdatedEvent = MutableLiveData<Event<Unit>>()
    val reminderUpdatedEvent: LiveData<Event<Unit>> = _reminderUpdatedEvent

    private var reminderId: String? = null

    private var isNewReminder: Boolean = false

    private var isDataLoaded = false

    private var reminderCompleted = false

    fun start(reminderId: String?) {
        if (_dataLoading.value == true) {
            return
        }

        this.reminderId = reminderId
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
    fun saveReminder() {
        val currentTitle = title.value
        val currentDescription = description.value?:""
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.getUid().toString()

        if (currentTitle == null ) {
            _snackbarText.value = Event(R.string.empty_reminder_message)
            return
        }

        val currentReminderId = reminderId
        if (isNewReminder || currentReminderId == null) {
            createReminder(Reminder(currentTitle, currentDescription,currentUserUID))
        } else {
            val reminder = Reminder(currentTitle, currentDescription, currentUserUID, reminderCompleted, currentReminderId)
            updateReminder(reminder)
        }
    }

    private fun createReminder(newReminder: Reminder) = viewModelScope.launch {
        remindersRepository.saveReminder(newReminder)
        _reminderUpdatedEvent.value = Event(Unit)
    }

    private fun updateReminder(reminder: Reminder) {
        if (isNewReminder) {
            throw RuntimeException("updateReminder() was called but reminder is new.")
        }
        viewModelScope.launch {
            remindersRepository.saveReminder(reminder)
            _reminderUpdatedEvent.value = Event(Unit)
        }
    }
}
