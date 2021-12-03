
package com.cryoggen.locationreminder.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cryoggen.locationreminder.data.Reminder

/**
 * The Room Database that contains the Task table.
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(entities = [Reminder::class], version = 1, exportSchema = false)
abstract class ToDoDatabase : RoomDatabase() {

    abstract fun reminderDao(): RemindersDao
}
