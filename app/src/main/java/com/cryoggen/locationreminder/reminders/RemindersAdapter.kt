package com.cryoggen.locationreminder.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.databinding.ReminderItemBinding


// Adapter for the Reminder list. Has a reference to the [RemindersViewModel] to send actions back to it.

class RemindersAdapter(private val viewModel: RemindersViewModel) :
    ListAdapter<Reminder, RemindersAdapter.ViewHolder>(ReminderDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ReminderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: RemindersViewModel, item: Reminder) {

            binding.viewmodel = viewModel
            binding.reminder = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ReminderItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}


// Callback for calculating the diff between two non-null items in a list.
//
//  Used by ListAdapter to calculate the minimum number of changes between and old list and a new
//  list that's been passed to `submitList`.

class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem == newItem
    }
}
