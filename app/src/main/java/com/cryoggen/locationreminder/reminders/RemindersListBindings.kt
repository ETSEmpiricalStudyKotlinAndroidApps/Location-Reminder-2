package com.cryoggen.locationreminder.reminders

import android.graphics.Paint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.servises.stopSound


/**
 * [BindingAdapter]s for the [Task]s list.
 */
@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<Reminder>?) {
    items?.let {
        (listView.adapter as RemindersAdapter).submitList(items)
    }
}

@BindingAdapter("app:completedReminder")
fun setStyle(textView: TextView, enabled: Boolean) {
    if (enabled) {
        stopSound(textView.context)
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}