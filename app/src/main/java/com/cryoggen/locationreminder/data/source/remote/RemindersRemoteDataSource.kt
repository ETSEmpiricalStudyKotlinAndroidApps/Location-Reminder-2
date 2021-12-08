package com.cryoggen.locationreminder.data.source.remote

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersDataSource
import com.cryoggen.locationreminder.data.Result
import com.cryoggen.locationreminder.data.Result.Error
import com.cryoggen.locationreminder.data.Result.Success
import kotlinx.coroutines.delay

/**
 * Implementation of the data source that adds a latency simulating network.
 */
object RemindersRemoteDataSource : RemindersDataSource {

    private const val SERVICE_LATENCY_IN_MILLIS = 2000L

    private var Reminders_SERVICE_DATA = LinkedHashMap<String, Reminder>(2)

    init {
        addReminder("Build tower in Pisa", "Ground looks good, no foundation work required.")
        addReminder("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
    }

    private val observableReminders = MutableLiveData<Result<List<Reminder>>>()

    @SuppressLint("NullSafeMutableLiveData")
    override suspend fun refreshReminders() {
        observableReminders.value = getReminders()
    }

    override suspend fun refreshReminder(ReminderId: String) {
        refreshReminders()
    }

    override fun observeReminders(): LiveData<Result<List<Reminder>>> {
        return observableReminders
    }

    override fun observeReminder(ReminderId: String): LiveData<Result<Reminder>> {
        return observableReminders.map { Reminders ->
            when (Reminders) {
                is Result.Loading -> Result.Loading
                is Error -> Error(Reminders.exception)
                is Success -> {
                    val Reminder = Reminders.data.firstOrNull() { it.id == ReminderId }
                        ?: return@map Error(Exception("Not found"))
                    Success(Reminder)
                }
            }
        }
    }

    override suspend fun getReminders(): Result<List<Reminder>> {
        // Simulate network by delaying the execution.
        val Reminders = Reminders_SERVICE_DATA.values.toList()
        delay(SERVICE_LATENCY_IN_MILLIS)
        return Success(Reminders)
    }

    override suspend fun getReminder(ReminderId: String): Result<Reminder> {
        // Simulate network by delaying the execution.
        delay(SERVICE_LATENCY_IN_MILLIS)
        Reminders_SERVICE_DATA[ReminderId]?.let {
            return Success(it)
        }
        return Error(Exception("Reminder not found"))
    }

    private fun addReminder(title: String, description: String) {
        val newReminder = Reminder(title, description)
        Reminders_SERVICE_DATA[newReminder.id] = newReminder
    }

    override suspend fun saveReminder(Reminder: Reminder) {
        Reminders_SERVICE_DATA[Reminder.id] = Reminder
    }

    override suspend fun completeReminder(Reminder: Reminder) {
        val completedReminder = Reminder(Reminder.title, Reminder.description, Reminder.userUID, true, Reminder.id)
        Reminders_SERVICE_DATA[Reminder.id] = completedReminder
    }

    override suspend fun completeReminder(ReminderId: String) {
        // Not required for the remote data source
    }

    override suspend fun activateReminder(Reminder: Reminder) {
        val activeReminder = Reminder(Reminder.title, Reminder.description, Reminder.userUID, false, Reminder.id)
        Reminders_SERVICE_DATA[Reminder.id] = activeReminder
    }

    override suspend fun activateReminder(ReminderId: String) {
        // Not required for the remote data source
    }

    override suspend fun clearCompletedReminders() {
        Reminders_SERVICE_DATA = Reminders_SERVICE_DATA.filterValues {
            !it.isCompleted
        } as LinkedHashMap<String, Reminder>
    }

    override suspend fun deleteAllReminders() {
        Reminders_SERVICE_DATA.clear()
    }

    override suspend fun deleteReminder(ReminderId: String) {
        Reminders_SERVICE_DATA.remove(ReminderId)
    }
}
