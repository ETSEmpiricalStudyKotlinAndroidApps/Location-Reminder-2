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
     * Observes list of Reminders.
     *
     * @return all Reminders.
     */
    @Query("SELECT * FROM Reminders WHERE useruid = :authorizedUserUID")
    fun observeReminders(authorizedUserUID: String): LiveData<List<Reminder>>

    /**
     * Observes a single Reminder.
     *
     * @param ReminderId the Reminder id.
     * @return the Reminder with ReminderId.
     */
    @Query("SELECT * FROM Reminders WHERE entryid = :reminderId")
     fun observeReminderById(reminderId: String): LiveData<Reminder>

    /**
     * Select all Reminders from the Reminders table.
     *
     * @return all Reminders.
     */
    @Query("SELECT * FROM Reminders WHERE useruid = :authorizedUserUID")
     fun getReminders(authorizedUserUID: String): List<Reminder>

    /**
     * Select a Reminder by id.
     *
     * @param ReminderId the Reminder id.
     * @return the Reminder with ReminderId.
     */
    @Query("SELECT * FROM Reminders WHERE entryid = :reminderId")
    fun getReminderById(reminderId: String): Reminder?

    /**
     * Insert a Reminder in the database. If the Reminder already exists, replace it.
     *
     * @param Reminder the Reminder to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    /**
     * Update a Reminder.
     *
     * @param Reminder Reminder to be updated
     * @return the number of Reminders updated. This should always be 1.
     */
    @Update
    fun updateReminder(reminder: Reminder): Int

    /**
     * Update the complete status of a Reminder
     *
     * @param ReminderId    id of the Reminder
     * @param completed status to be updated
     */
    @Query("UPDATE Reminders SET completed = :completed WHERE entryid = :reminderId")
     fun updateCompleted(reminderId: String, completed: Boolean)

    /**
     * Delete a Reminder by id.
     *
     * @return the number of Reminders deleted. This should always be 1.
     */
    @Query("DELETE FROM Reminders WHERE entryid = :reminderId")
     fun deleteReminderById(reminderId: String): Int

    /**
     * Delete all Reminders.
     */
    @Query("DELETE FROM reminders")
    fun deleteReminders()

    /**
     * Delete all completed Reminders from the table.
     *
     * @return the number of Reminders deleted.
     */
    @Query("DELETE FROM Reminders WHERE completed = 1")
   fun deleteCompletedReminders(): Int
}
