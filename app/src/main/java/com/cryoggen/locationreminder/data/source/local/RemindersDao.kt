package com.cryoggen.locationreminder.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cryoggen.locationreminder.data.Reminder

/**
 * Data Access Object for the Reminders table.
 */
@Dao
interface RemindersDao {

    /**
     * Observes list of reminders.
     *
     * @return all reminders.
     */
    @Query("SELECT * FROM Reminders WHERE useruid = :authorizedUserUID")
    fun observeReminders(authorizedUserUID: String): LiveData<List<Reminder>>

    /**
     * Observes a single Reminder.
     *
     * @param reminderId the Reminder id.
     * @return the reminder with reminderId.
     */
    @Query("SELECT * FROM Reminders WHERE entryid = :reminderId")
    fun observeReminderById(reminderId: String): LiveData<Reminder>

    /**
     * Select all reminders from the Reminders table.
     *
     * @return all reminders.
     */
    @Query("SELECT * FROM Reminders WHERE useruid = :authorizedUserUID")
    fun getReminders(authorizedUserUID: String): List<Reminder>

    /**
     * Select a Reminder by id.
     *
     * @param reminderId the reminder id.
     * @return the reminder with reminderId.
     */
    @Query("SELECT * FROM Reminders WHERE entryid = :reminderId")
    fun getReminderById(reminderId: String): Reminder?

    /**
     * Insert a Reminder in the database. If the Reminder already exists, replace it.
     *
     * @param reminder the reminder to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    /**
     * Update a Reminder.
     *
     * @param reminder reminder to be updated
     * @return the number of reminders updated. This should always be 1.
     */
    @Update
    fun updateReminder(reminder: Reminder): Int

    /**
     * Update the complete status of a reminder
     *
     * @param ReminderId    id of the reminder
     * @param completed status to be updated
     */
    @Query("UPDATE Reminders SET completed = :completed WHERE entryid = :reminderId")
    fun updateCompleted(reminderId: String, completed: Boolean)

    /**
     * Delete a reminder by id.
     *
     * @return the number of reminders deleted. This should always be 1.
     */
    @Query("DELETE FROM Reminders WHERE entryid = :reminderId")
    fun deleteReminderById(reminderId: String): Int

    /**
     * Delete all reminders.
     */
    @Query("DELETE FROM reminders WHERE useruid = :authorizedUserUID")
    fun deleteReminders(authorizedUserUID: String): Int

    /**
     * Delete all completed reminders from the table.
     *
     * @return the number of reminders deleted.
     */
    @Query("DELETE FROM Reminders WHERE completed = 1 AND useruid = :authorizedUserUID ")
    fun deleteCompletedReminders(authorizedUserUID: String): Int
}
