package com.cryoggen.locationreminder.reminders

import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.addeditreminder.GeofencingConstants
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.databinding.FragmentRemindersBinding
import com.cryoggen.locationreminder.main.MainActivity
import com.cryoggen.locationreminder.reminders.util.setupRefreshLayout
import com.cryoggen.locationreminder.reminders.util.setupSnackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import android.content.Intent.getIntent
import com.cryoggen.locationreminder.sound.Sound


/**
 * Display a grid of [Reminder]s. User can choose to view all, active or completed Reminders.
 */
class RemindersFragment : Fragment() {

    private val viewModel by viewModels<RemindersViewModel>()

    private val args: RemindersFragmentArgs by navArgs()

    private lateinit var viewDataBinding: FragmentRemindersBinding

    private lateinit var listAdapter: RemindersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentRemindersBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }


    private fun handleIntentfromActivity() {
        val extras = MainActivity.activity.intent?.extras
        if (extras != null) {
            if (extras.containsKey(GeofencingConstants.EXTRA_GEOFENCE_INDEX)) {

                val intentService = Intent(context, Sound::class.java)
                context?.stopService(intentService)

                val idReminder = extras.getString(GeofencingConstants.EXTRA_GEOFENCE_INDEX)
                MainActivity.activity.intent?.removeExtra(GeofencingConstants.EXTRA_GEOFENCE_INDEX)
                viewModel.openReminder(idReminder!!)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_clear -> {
                viewModel.clearCompletedReminders()
                true
            }

            R.id.menu_clear_all -> {
                viewModel.clearAllReminders()
                true
            }

            R.id.menu_filter -> {
                showFilteringPopUpMenu()
                true
            }


            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminders_fragment_menu, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setupSnackbar()
        setupListAdapter()
        setupRefreshLayout(viewDataBinding.refreshLayout, viewDataBinding.remindersList)
        setupNavigation()
        setupFab()
        handleIntentfromActivity()
    }

    private fun setupNavigation() {
        viewModel.openReminderEvent.observe(viewLifecycleOwner, EventObserver {
            openReminderDetails(it)
        })
        viewModel.newReminderEvent.observe(viewLifecycleOwner, EventObserver {
            navigateToAddNewReminder()
        })
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    private fun showFilteringPopUpMenu() {
        val view = activity?.findViewById<View>(R.id.menu_filter) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_reminders, menu)

            setOnMenuItemClickListener {
                viewModel.setFiltering(
                    when (it.itemId) {
                        R.id.active -> RemindersFilterType.ACTIVE_REMINDERS
                        R.id.completed -> RemindersFilterType.COMPLETED_REMINDERS
                        else -> RemindersFilterType.ALL_REMINDERS
                    }
                )
                true
            }
            show()
        }
    }

    private fun setupFab() {
        activity?.findViewById<FloatingActionButton>(R.id.add_reminder_fab)?.let {
            it.setOnClickListener {
                navigateToAddNewReminder()
            }
        }
    }

    private fun navigateToAddNewReminder() {
        val action = RemindersFragmentDirections
            .actionRemindersFragmentToAddEditReminderFragment(
                null,
                resources.getString(R.string.add_reminder)
            )
        findNavController().navigate(action)
    }

    private fun openReminderDetails(ReminderId: String) {
        MainActivity.activity.intent?.extras?.clear()
        val action =
            RemindersFragmentDirections.actionRemindersFragmentToReminderDetailFragment(ReminderId)
        findNavController().navigate(action)
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = RemindersAdapter(viewModel)
            viewDataBinding.remindersList.adapter = listAdapter
        } else {
            Timber.w("ViewModel not initialized when attempting to set up adapter.")
        }
    }

}
