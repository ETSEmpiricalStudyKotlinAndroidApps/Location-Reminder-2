
package com.cryoggen.locationreminder.reminders

/**
 * Used with the filter spinner in the tasks list.
 */
enum class RemindersFilterType {
    /**
     * Do not filter tasks.
     */
    ALL_REMINDERS,

    /**
     * Filters only the active (not completed yet) tasks.
     */
    ACTIVE_REMINDERS,

    /**
     * Filters only the completed tasks.
     */
    COMPLETED_REMINDERS
}
