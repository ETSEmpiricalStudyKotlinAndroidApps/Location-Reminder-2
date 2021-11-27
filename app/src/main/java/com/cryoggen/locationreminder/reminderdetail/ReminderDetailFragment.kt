package com.cryoggen.locationreminder.reminderdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.databinding.ReminderdetailFragBinding
import com.cryoggen.locationreminder.reminders.util.setupRefreshLayout
import com.cryoggen.locationreminder.reminders.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the Reminder detail screen.
 */
class ReminderDetailFragment : Fragment() {
    private lateinit var viewDataBinding: ReminderdetailFragBinding

    private val args: ReminderdetailFragmentArgs by navArgs()

    private val viewModel by viewModels<ReminderDetailViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFab()
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
    }

    private fun setupNavigation() {
        viewModel.deleteReminderEvent.observe(viewLifecycleOwner, EventObserver {
            val action = ReminderDetailFragmentDirections
                .actionReminderDetailFragmentToRemindersFragment(DELETE_RESULT_OK)
            findNavController().navigate(action)
        })
        viewModel.editReminderEvent.observe(viewLifecycleOwner, EventObserver {
            val action = ReminderDetailFragmentDirections
                .actionReminderDetailFragmentToAddEditReminderFragment(
                    args.ReminderId,
                    resources.getString(R.string.edit_Reminder)
                )
            findNavController().navigate(action)
        })
    }

    private fun setupFab() {
        activity?.findViewById<View>(R.id.edit_Reminder_fab)?.setOnClickListener {
            viewModel.editReminder()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.Reminderdetail_frag, container, false)
        viewDataBinding = ReminderdetailFragBinding.bind(view).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        viewModel.start(args.ReminderId)

        setHasOptionsMenu(true)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteReminder()
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.Reminderdetail_fragment_menu, menu)
    }
}
