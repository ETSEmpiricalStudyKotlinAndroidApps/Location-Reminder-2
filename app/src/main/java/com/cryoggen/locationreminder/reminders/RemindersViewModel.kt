package com.cryoggen.locationreminder.reminders

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.cryoggen.locationreminder.*
import com.cryoggen.locationreminder.data.Result
import com.cryoggen.locationreminder.data.Result.Success
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersDataSource
import com.cryoggen.locationreminder.data.source.RemindersRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Reminder list screen.
 */
class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    // Note, for testing and architecture purposes, it's bad practice to construct the repository
    // here. We'll show you how to fix this during the codelab
    private val remindersRepository = RemindersRepository.getRepository(application)

    private val _forceUpdate = MutableLiveData<Boolean>(false)

    private val _items: LiveData<List<Reminder>> = _forceUpdate.switchMap { forceUpdate ->
        if (forceUpdate) {
            _dataLoading.value = true
            viewModelScope.launch {
                remindersRepository.refreshReminders()
                _dataLoading.value = false
            }
        }
        remindersRepository.observeReminders().switchMap { filterReminders(it) }

    }

    val items: LiveData<List<Reminder>> = _items

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int> = _currentFilteringLabel

    private val _noRemindersLabel = MutableLiveData<Int>()
    val noRemindersLabel: LiveData<Int> = _noRemindersLabel

    private val _noReminderIconRes = MutableLiveData<Int>()
    val noReminderIconRes: LiveData<Int> = _noReminderIconRes

    private val _RemindersAddViewVisible = MutableLiveData<Boolean>()
    val RemindersAddViewVisible: LiveData<Boolean> = _RemindersAddViewVisible

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private var currentFiltering = RemindersFilterType.ALL_REMINDERS

    // Not used at the moment
    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _openReminderEvent = MutableLiveData<Event<String>>()
    val openReminderEvent: LiveData<Event<String>> = _openReminderEvent

    private val _newReminderEvent = MutableLiveData<Event<Unit>>()
    val newReminderEvent: LiveData<Event<Unit>> = _newReminderEvent

    private var resultMessageShown: Boolean = false

    // This LiveData depends on another so we can use a transformation.
    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        // Set initial state
        setFiltering(RemindersFilterType.ALL_REMINDERS)
        loadReminders(true)
    }

    /**
     * Sets the current Reminder filtering type.
     *
     * @param requestType Can be [RemindersFilterType.ALL_Reminders],
     * [RemindersFilterType.COMPLETED_Reminders], or
     * [RemindersFilterType.ACTIVE_Reminders]
     */
    fun setFiltering(requestType: RemindersFilterType) {
        currentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            RemindersFilterType.ALL_REMINDERS -> {
                setFilter(
                    R.string.label_all, R.string.no_reminders_all,
                    R.drawable.logo_no_fill, true
                )
            }
            RemindersFilterType.ACTIVE_REMINDERS -> {
                setFilter(
                    R.string.label_active, R.string.no_reminders_active,
                    R.drawable.ic_check_circle_96dp, false
                )
            }
            RemindersFilterType.COMPLETED_REMINDERS -> {
                setFilter(
                    R.string.label_completed, R.string.no_reminders_completed,
                    R.drawable.ic_verified_user_96dp, false
                )
            }
        }
        // Refresh list
        loadReminders(false)
    }

    private fun setFilter(
        @StringRes filteringLabelString: Int, @StringRes noRemindersLabelString: Int,
        @DrawableRes noReminderIconDrawable: Int, RemindersAddVisible: Boolean
    ) {
        _currentFilteringLabel.value = filteringLabelString
        _noRemindersLabel.value = noRemindersLabelString
        _noReminderIconRes.value = noReminderIconDrawable
        _RemindersAddViewVisible.value = RemindersAddVisible
    }

    fun clearCompletedReminders() {
        viewModelScope.launch {
            remindersRepository.clearCompletedReminders()
            showSnackbarMessage(R.string.completed_reminders_cleared)
        }
    }

    fun clearAllReminders() {
        viewModelScope.launch {
            remindersRepository.deleteAllReminders()
            showSnackbarMessage(R.string.completed_reminders_cleared)
        }
    }

    fun completeReminder(Reminder: Reminder, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            remindersRepository.completeReminder(Reminder)
            showSnackbarMessage(R.string.reminder_marked_complete)
        } else {
            remindersRepository.activateReminder(Reminder)
            showSnackbarMessage(R.string.reminder_marked_active)
        }
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewReminder() {
        _newReminderEvent.value = Event(Unit)
    }

    /**
     * Called by Data Binding.
     */
    fun openReminder(ReminderId: String) {
        _openReminderEvent.value = Event(ReminderId)
    }

    fun showEditResultMessage(result: Int) {
        if (resultMessageShown) return
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_reminder_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_reminder_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_reminder_message)
        }
        resultMessageShown = true
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    private fun filterReminders(RemindersResult: Result<List<Reminder>>): LiveData<List<Reminder>> {
        // TODO: This is a good case for liveData builder. Replace when stable.
        val result = MutableLiveData<List<Reminder>>()

        if (RemindersResult is Success) {
            isDataLoadingError.value = false
            viewModelScope.launch {
                result.value = filterItems(RemindersResult.data, currentFiltering)
            }
        } else {
            result.value = emptyList()
            showSnackbarMessage(R.string.loading_reminders_error)
            isDataLoadingError.value = true
        }

        return result
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the [RemindersDataSource]
     */
    fun loadReminders(forceUpdate: Boolean) {
        _forceUpdate.value = forceUpdate
    }

    private fun filterItems(Reminders: List<Reminder>, filteringType: RemindersFilterType): List<Reminder> {
        val RemindersToShow = ArrayList<Reminder>()
        // We filter the Reminders based on the requestType
        for (Reminder in Reminders) {
            when (filteringType) {
                RemindersFilterType.ALL_REMINDERS -> RemindersToShow.add(Reminder)
                RemindersFilterType.ACTIVE_REMINDERS -> if (Reminder.isActive) {
                    RemindersToShow.add(Reminder)
                }
                RemindersFilterType.COMPLETED_REMINDERS -> if (Reminder.isCompleted) {
                    RemindersToShow.add(Reminder)
                }
            }
        }
        return RemindersToShow
    }

    fun refresh() {
        _forceUpdate.value = true
    }

}
