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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.DELETE_RESULT_OK
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.authentication.AuthenticationState
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.databinding.ReminderdetailFragBinding
import com.cryoggen.locationreminder.map.MapReminder
import com.cryoggen.locationreminder.reminders.util.setupRefreshLayout
import com.cryoggen.locationreminder.reminders.util.setupSnackbar
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the Reminder detail screen.
 */
class ReminderDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapReminder:MapReminder


    private lateinit var viewDataBinding: ReminderdetailFragBinding

    private val args: ReminderDetailFragmentArgs by navArgs()

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
                    args.reminderId,
                    resources.getString(R.string.edit_reminder)
                )
            findNavController().navigate(action)
        })
    }

    private fun setupFab() {
        activity?.findViewById<View>(R.id.edit_reminder_fab)?.setOnClickListener {
            viewModel.editReminder()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.reminderdetail_frag, container, false)

        viewDataBinding = ReminderdetailFragBinding.bind(view).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        observeReminderState()

        viewModel.start(args.reminderId)

        observeReminderState()

        setHasOptionsMenu(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_detail_reminder) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onStart() {
        super.onStart()
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
        inflater.inflate(R.menu.reminderdetail_fragment_menu, menu)
    }

    private fun observeReminderState() {
        viewModel.reminder.observe(viewLifecycleOwner, Observer { reminderState ->
           if (reminderState != null) {
               mapReminder.latitude = reminderState.latitude
                mapReminder.longitude = reminderState.longitude
               mapReminder.addMarker()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapReminder = MapReminder(googleMap,context)
    }
}
