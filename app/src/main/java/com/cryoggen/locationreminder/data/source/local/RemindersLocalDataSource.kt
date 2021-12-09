package com.cryoggen.locationreminder.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersDataSource
import com.cryoggen.locationreminder.data.Result
import com.cryoggen.locationreminder.data.Result.Error
import com.cryoggen.locationreminder.data.Result.Success
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Concrete implementation of a data source as a db.
 */
class RemindersLocalDataSource internal constructor(
    private val RemindersDao: RemindersDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RemindersDataSource {

    override fun observeReminders(): LiveData<Result<List<Reminder>>> {
        return RemindersDao.observeReminders(FirebaseAuth.getInstance().currentUser?.getUid().toString()).map {
            Success(it)
        }
    }

    override fun observeReminder(ReminderId: String): LiveData<Result<Reminder>> {
        return RemindersDao.observeReminderById(ReminderId).map {
            Success(it)
        }
    }

    override suspend fun refreshReminder(ReminderId: String) {
        //NO-OP
    }

    override suspend fun refreshReminders() {
        //NO-OP
    }

    override suspend fun getReminders(): Result<List<Reminder>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(RemindersDao.getReminders(FirebaseAuth.getInstance().currentUser?.getUid().toString()))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getReminder(ReminderId: String): Result<Reminder> = withContext(ioDispatcher) {
        try {
            val Reminder = RemindersDao.getReminderById(ReminderId)
            if (Reminder != null) {
                return@withContext Success(Reminder)
            } else {
                return@withContext Error(Exception("Reminder not found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun saveReminder(Reminder: Reminder) = withContext(ioDispatcher) {
        RemindersDao.insertReminder(Reminder)
    }

    override suspend fun completeReminder(Reminder: Reminder) = withContext(ioDispatcher) {
        RemindersDao.updateCompleted(Reminder.id, true)
    }

    override suspend fun completeReminder(ReminderId: String) {
        RemindersDao.updateCompleted(ReminderId, true)
    }

    override suspend fun activateReminder(Reminder: Reminder) = withContext(ioDispatcher) {
        RemindersDao.updateCompleted(Reminder.id, false)
    }

    override suspend fun activateReminder(ReminderId: String) {
        RemindersDao.updateCompleted(ReminderId, false)
    }

    override suspend fun clearCompletedReminders() = withContext<Unit>(ioDispatcher) {
        RemindersDao.deleteCompletedReminders(FirebaseAuth.getInstance().currentUser?.getUid().toString())
    }

    override suspend fun deleteAllReminders() = withContext(ioDispatcher) {
        RemindersDao.deleteReminders(FirebaseAuth.getInstance().currentUser?.getUid().toString())
    }

    override suspend fun deleteReminder(ReminderId: String) = withContext<Unit>(ioDispatcher) {
        RemindersDao.deleteReminderById(ReminderId)
    }
}
