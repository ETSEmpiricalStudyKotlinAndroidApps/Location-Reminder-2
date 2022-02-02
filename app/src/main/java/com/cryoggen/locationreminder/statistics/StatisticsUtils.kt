package com.cryoggen.locationreminder.statistics

import com.cryoggen.locationreminder.data.Reminder

internal fun getActiveAndCompletedStats(reminders: List<Reminder>?): StatsResult {

    return if (reminders == null || reminders.isEmpty()) {
        StatsResult(0f, 0f)
    } else {
        val totalReminders = reminders.size
        val numberOfActiveReminders = reminders.count { it.isActive }
        StatsResult(
            activeRemindersPercent = 100f * numberOfActiveReminders / reminders.size,
            completedRemindersPercent = 100f * (totalReminders - numberOfActiveReminders) / reminders.size
        )
    }
}

data class StatsResult(val activeRemindersPercent: Float, val completedRemindersPercent: Float)
