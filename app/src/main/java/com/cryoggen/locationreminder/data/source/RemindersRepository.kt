package com.cryoggen.locationreminder.data.source

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.cryoggen.locationreminder.data.Result
import com.cryoggen.locationreminder.data.Result.Success
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.local.RemindersLocalDataSource
import com.cryoggen.locationreminder.data.source.local.ToDoDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Concrete implementation to load Reminders from the data sources into a cache.
 */
class RemindersRepository private constructor(application: Application) {

    private val remindersLocalDataSource: RemindersDataSource
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

        remindersLocalDataSource = RemindersLocalDataSource(database.remindersDao())
    }

    suspend fun getReminders(forceUpdate: Boolean = false): Result<List<Reminder>> {
        return remindersLocalDataSource.getReminders()
    }


    fun observeReminders(): LiveData<Result<List<Reminder>>> {
        return remindersLocalDataSource.observeReminders()
    }


    fun observeReminder(ReminderId: String): LiveData<Result<Reminder>> {
        return remindersLocalDataSource.observeReminder(ReminderId)
    }


    /**
     * Relies on [getReminders] to fetch data and picks the Reminder with the same ID.
     */
    suspend fun getReminder(ReminderId: String, forceUpdate: Boolean = false): Result<Reminder> {

        return remindersLocalDataSource.getReminder(ReminderId)
    }

    suspend fun saveReminder(reminder: Reminder) {
        coroutineScope {
            launch { remindersLocalDataSource.saveReminder(reminder) }
        }
    }

    suspend fun completeReminder(reminder: Reminder) {
        coroutineScope {
            launch { remindersLocalDataSource.completeReminder(reminder) }
        }
    }


    suspend fun activateReminder(reminder: Reminder) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { remindersLocalDataSource.activateReminder(reminder) }
        }
    }

    suspend fun clearCompletedReminders() {
        coroutineScope {
            launch { remindersLocalDataSource.clearCompletedReminders() }
        }
    }

    suspend fun deleteAllReminders() {
        coroutineScope {
            launch { remindersLocalDataSource.deleteAllReminders() }
        }
    }

    suspend fun deleteReminder(reminderId: String) {
        coroutineScope {
            launch { remindersLocalDataSource.deleteReminder(reminderId) }
        }
    }


}
