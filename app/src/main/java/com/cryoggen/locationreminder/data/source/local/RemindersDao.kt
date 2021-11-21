/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    @Query("SELECT * FROM Reminders")
    fun observeReminders(): LiveData<List<Reminder>>

    /**
     * Observes a single Reminder.
     *
     * @param ReminderId the Reminder id.
     * @return the Reminder with ReminderId.
     */
    @Query("SELECT * FROM Reminders WHERE entryid = :ReminderId")
    fun observeReminderById(ReminderId: String): LiveData<Reminder>

    /**
     * Select all Reminders from the Reminders table.
     *
     * @return all Reminders.
     */
    @Query("SELECT * FROM Reminders")
    suspend fun getReminders(): List<Reminder>

    /**
     * Select a Reminder by id.
     *
     * @param ReminderId the Reminder id.
     * @return the Reminder with ReminderId.
     */
    @Query("SELECT * FROM Reminders WHERE entryid = :ReminderId")
    suspend fun getReminderById(ReminderId: String): Reminder?

    /**
     * Insert a Reminder in the database. If the Reminder already exists, replace it.
     *
     * @param Reminder the Reminder to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(Reminder: Reminder)

    /**
     * Update a Reminder.
     *
     * @param Reminder Reminder to be updated
     * @return the number of Reminders updated. This should always be 1.
     */
    @Update
    suspend fun updateReminder(Reminder: Reminder): Int

    /**
     * Update the complete status of a Reminder
     *
     * @param ReminderId    id of the Reminder
     * @param completed status to be updated
     */
    @Query("UPDATE Reminders SET completed = :completed WHERE entryid = :ReminderId")
    suspend fun updateCompleted(ReminderId: String, completed: Boolean)

    /**
     * Delete a Reminder by id.
     *
     * @return the number of Reminders deleted. This should always be 1.
     */
    @Query("DELETE FROM Reminders WHERE entryid = :ReminderId")
    suspend fun deleteReminderById(ReminderId: String): Int

    /**
     * Delete all Reminders.
     */
    @Query("DELETE FROM Reminders")
    suspend fun deleteReminders()

    /**
     * Delete all completed Reminders from the table.
     *
     * @return the number of Reminders deleted.
     */
    @Query("DELETE FROM Reminders WHERE completed = 1")
    suspend fun deleteCompletedReminders(): Int
}
