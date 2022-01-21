package com.cryoggen.locationreminder.data.source

import androidx.lifecycle.LiveData
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.Result

/**
 * Main entry point for accessing Reminders data.
 */
interface RemindersDataSource {


    fun observeReminders(): LiveData<com.cryoggen.locationreminder.data.Result<List<Reminder>>>

    suspend fun getReminders(): Result<List<Reminder>>

    suspend fun refreshReminders()

    fun observeReminder(ReminderId: String): LiveData<Result<Reminder>>

    suspend fun getReminder(ReminderId: String): Result<Reminder>

    suspend fun refreshReminder(ReminderId: String)

    suspend fun saveReminder(Reminder: Reminder)

    suspend fun completeReminder(Reminder: Reminder)

    suspend fun completeReminder(ReminderId: String)

    suspend fun activateReminder(Reminder: Reminder)

    suspend fun activateReminder(ReminderId: String)

    suspend fun clearCompletedReminders()

    suspend fun deleteAllReminders()

    suspend fun deleteReminder(ReminderId: String)

}
