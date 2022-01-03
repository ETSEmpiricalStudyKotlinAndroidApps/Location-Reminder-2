package com.cryoggen.locationreminder.data.source

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.cryoggen.locationreminder.data.Result
import com.cryoggen.locationreminder.data.Result.Success
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.local.RemindersLocalDataSource
import com.cryoggen.locationreminder.data.source.local.ToDoDatabase
import com.cryoggen.locationreminder.data.source.remote.RemindersRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Concrete implementation to load Reminders from the data sources into a cache.
 */
class RemindersRepository private constructor(application: Application) {

    private val remindersRemoteDataSource: RemindersDataSource
    private val RemindersLocalDataSource: RemindersDataSource
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    companion object {
        @Volatile
        private var INSTANCE: RemindersRepository? = null

        fun getRepository(app: Application): RemindersRepository {
            return INSTANCE ?: synchronized(this) {
                RemindersRepository(app).also {
                    INSTANCE = it
                }
            }
        }
    }

    init {
        val database = Room.databaseBuilder(
            application.applicationContext,
            ToDoDatabase::class.java, "Reminders.db"
        )
            .build()

        remindersRemoteDataSource = RemindersRemoteDataSource
        RemindersLocalDataSource = RemindersLocalDataSource(database.remindersDao())
    }

    suspend fun getReminders(forceUpdate: Boolean = false): Result<List<Reminder>> {
        if (forceUpdate) {
            try {
                updateRemindersFromRemoteDataSource()
            } catch (ex: Exception) {
                return Result.Error(ex)
            }
        }
        return RemindersLocalDataSource.getReminders()
    }

    suspend fun refreshReminders() {
        // updateRemindersFromRemoteDataSource()
    }

    fun observeReminders(): LiveData<Result<List<Reminder>>> {
        return RemindersLocalDataSource.observeReminders()
    }

    suspend fun refreshReminder(reminderId: String) {
        //   updateReminderFromRemoteDataSource(reminderId)
    }

    private suspend fun updateRemindersFromRemoteDataSource() {
        val remoteReminders = RemindersRemoteDataSource.getReminders()

        if (remoteReminders is Success) {
            // Real apps might want to do a proper sync.
            RemindersLocalDataSource.deleteAllReminders()
            remoteReminders.data.forEach { reminder ->
                RemindersLocalDataSource.saveReminder(reminder)
            }
        } else if (remoteReminders is Result.Error) {
            throw remoteReminders.exception
        }
    }

    fun observeReminder(ReminderId: String): LiveData<Result<Reminder>> {
        return RemindersLocalDataSource.observeReminder(ReminderId)
    }

    private suspend fun updateReminderFromRemoteDataSource(reminderId: String) {
        val remoteReminder = RemindersRemoteDataSource.getReminder(reminderId)

        if (remoteReminder is Success) {
            RemindersLocalDataSource.saveReminder(remoteReminder.data)
        }
    }

    /**
     * Relies on [getReminders] to fetch data and picks the Reminder with the same ID.
     */
    suspend fun getReminder(ReminderId: String, forceUpdate: Boolean = false): Result<Reminder> {
//        if (forceUpdate) {
//            updateReminderFromRemoteDataSource(ReminderId)
//        }
        return RemindersLocalDataSource.getReminder(ReminderId)
    }

    suspend fun saveReminder(reminder: Reminder) {
        coroutineScope {
            launch { RemindersRemoteDataSource.saveReminder(reminder) }
            launch { RemindersLocalDataSource.saveReminder(reminder) }
        }
    }

    suspend fun completeReminder(reminder: Reminder) {
        coroutineScope {
            launch { RemindersRemoteDataSource.completeReminder(reminder) }
            launch { RemindersLocalDataSource.completeReminder(reminder) }
        }
    }

    suspend fun completeReminder(ReminderId: String) {
        withContext(ioDispatcher) {
            (getReminderWithId(ReminderId) as? Success)?.let { it ->
                completeReminder(it.data)
            }
        }
    }

    suspend fun activateReminder(reminder: Reminder) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { RemindersRemoteDataSource.activateReminder(reminder) }
            launch { RemindersLocalDataSource.activateReminder(reminder) }
        }
    }

    suspend fun activateReminder(reminderId: String) {
        withContext(ioDispatcher) {
            (getReminderWithId(reminderId) as? Success)?.let { it ->
                activateReminder(it.data)
            }
        }
    }

    suspend fun clearCompletedReminders() {
        coroutineScope {
            launch { RemindersRemoteDataSource.clearCompletedReminders() }
            launch { RemindersLocalDataSource.clearCompletedReminders() }
        }
    }

    suspend fun deleteAllReminders() {
        coroutineScope {
            launch { RemindersRemoteDataSource.deleteAllReminders() }
            launch { RemindersLocalDataSource.deleteAllReminders() }
        }
    }

    suspend fun deleteReminder(reminderId: String) {
        coroutineScope {
            launch { RemindersRemoteDataSource.deleteReminder(reminderId) }
            launch { RemindersLocalDataSource.deleteReminder(reminderId) }
        }
    }

    private suspend fun getReminderWithId(id: String): Result<Reminder> {
        return RemindersLocalDataSource.getReminder(id)
    }

}
