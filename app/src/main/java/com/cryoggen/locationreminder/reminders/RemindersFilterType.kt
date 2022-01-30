
package com.cryoggen.locationreminder.reminders

/**
 * Used with the filter spinner in the reminders list.
 */
enum class RemindersFilterType {
    /**
     * Do not filter reminders.
     */
    ALL_REMINDERS,

    /**
     * Filters only the active (not completed yet) reminders.
     */
    ACTIVE_REMINDERS,

    /**
     * Filters only the completed reminders.
     */
    COMPLETED_REMINDERS
}
