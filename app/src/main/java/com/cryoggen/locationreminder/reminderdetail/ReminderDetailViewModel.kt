package com.cryoggen.locationreminder.reminderdetail

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.cryoggen.locationreminder.Event
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.data.Result
import com.cryoggen.locationreminder.data.Result.Success
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Details screen.
 */
class ReminderDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _reminderIdRemoveGeofence = MutableLiveData<String>()
    val reminderIdForRemoveGeofence: LiveData<String> = _reminderIdRemoveGeofence

    private val _reminderForAddGeofence = MutableLiveData<Reminder>()
    val reminderForAddGeofence: LiveData<Reminder> = _reminderForAddGeofence

    // Note, for testing and architecture purposes, it's bad practice to construct the repository
    // here. We'll show you how to fix this during the codelab
    private val remindersRepository = RemindersRepository.getRepository(application)

    private val _reminderId = MutableLiveData<String>()

    private val _reminder = _reminderId.switchMap { reminderId ->
        remindersRepository.observeReminder(reminderId).map { computeResult(it) }
    }
    val reminder: LiveData<Reminder?> = _reminder

    val isDataAvailable: LiveData<Boolean> = _reminder.map { it != null }

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editReminderEvent = MutableLiveData<Event<Unit>>()
    val editReminderEvent: LiveData<Event<Unit>> = _editReminderEvent

    private val _deleteReminderEvent = MutableLiveData<Event<Unit>>()
    val deleteReminderEvent: LiveData<Event<Unit>> = _deleteReminderEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    // This LiveData depends on another so we can use a transformation.
    val completed: LiveData<Boolean> = _reminder.map { input: Reminder? ->
        input?.isCompleted ?: false
    }

    fun deleteReminder() {
        _reminderIdRemoveGeofence.value = _reminderId.value!!
        viewModelScope.launch {
            _reminderId.value?.let {
                remindersRepository.deleteReminder(it)
                _deleteReminderEvent.value = Event(Unit)
            }
        }
    }

    fun editReminder() {
        _editReminderEvent.value = Event(Unit)
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val reminder = _reminder.value ?: return@launch
        if (completed) {
            _reminderIdRemoveGeofence.value = _reminderId.value!!
            remindersRepository.completeReminder(reminder)
            showSnackbarMessage(R.string.reminder_marked_complete)
        } else {
            _reminderForAddGeofence.value = (reminder)
            remindersRepository.activateReminder(reminder)
            showSnackbarMessage(R.string.reminder_marked_active)
        }
    }

    fun start(reminderId: String) {
        // If we're already loading or already loaded, return (might be a config change)
        if (_dataLoading.value == true || reminderId == _reminderId.value) {
            return
        }
        // Trigger the load
        _reminderId.value = reminderId
    }

    private fun computeResult(reminderResult: Result<Reminder>): Reminder? {
        return if (reminderResult is Success) {
            reminderResult.data
        } else {
            showSnackbarMessage(R.string.loading_reminders_error)
            null
        }
    }


    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }


}
