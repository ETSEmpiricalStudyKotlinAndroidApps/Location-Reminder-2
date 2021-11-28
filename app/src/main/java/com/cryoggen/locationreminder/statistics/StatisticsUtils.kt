package com.cryoggen.locationreminder.statistics

import com.cryoggen.locationreminder.data.Reminder

/**
 * Function that does some trivial computation. Used to showcase unit tests.
 */
internal fun getActiveAndCompletedStats(reminders: List<Reminder>?): StatsResult {

    return if (reminders == null || reminders.isEmpty()) {
        StatsResult(0f, 0f)
    } else {
        val totalTasks = reminders.size
        val numberOfActiveTasks = reminders.count { it.isActive }
        StatsResult(
            activeTasksPercent = 100f * numberOfActiveTasks / reminders.size,
            completedTasksPercent = 100f * (totalTasks - numberOfActiveTasks) / reminders.size
        )
    }
}

data class StatsResult(val activeTasksPercent: Float, val completedTasksPercent: Float)
