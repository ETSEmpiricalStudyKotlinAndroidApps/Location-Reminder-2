package com.cryoggen.locationreminder.reminderdetail

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.databinding.ReminderdetailFragBinding
import com.cryoggen.locationreminder.main.DELETE_RESULT_OK
import com.cryoggen.locationreminder.map.MapReminder
import com.cryoggen.locationreminder.reminders.util.setupSnackbar
import com.cryoggen.locationreminder.services.RemindersService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the Reminder detail screen.
 */
class ReminderDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var mapReminder: MapReminder

    private lateinit var viewDataBinding: ReminderdetailFragBinding

    private val args: ReminderDetailFragmentArgs by navArgs()

    private val viewModel by viewModels<ReminderDetailViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFab()
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()
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

        viewModel.start(args.reminderId)

        setHasOptionsMenu(true)

        settingsMapView()

        observesCompletedCheckedState()

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
        inflater.inflate(R.menu.reminderdetail_fragment_menu, menu)
    }

    private fun settingsMapView() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_detail_reminder) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mapView = mapFragment.view
        val view = mapView?.findViewWithTag<View>("GoogleMapMyLocationButton")

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParams.marginEnd = 70
        layoutParams.bottomMargin = 260

        view!!.layoutParams = layoutParams
    }

    private fun observeReminderState() {
        viewModel.reminder.observe(viewLifecycleOwner, { reminderState ->
            if (reminderState != null) {
                mapReminder.switchOnMapClick(false)
                mapReminder.latitude = reminderState.latitude
                mapReminder.longitude = reminderState.longitude
                mapReminder.addMarker()
            }
        })
    }

    private fun observesCompletedCheckedState() {
        viewModel.setCompletedCheked.observe(viewLifecycleOwner, {
            val intentRemindersService = Intent(requireActivity(), RemindersService::class.java)
            ContextCompat.startForegroundService(requireActivity(), intentRemindersService)
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        checkDarkStyleMap()
        mapReminder = MapReminder(googleMap, requireActivity())
        observeReminderState()
    }

    private fun checkDarkStyleMap() {
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireActivity(),
                        R.raw.map_dark_style
                    )
                )
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireActivity(),
                        R.raw.map_light_style
                    )
                )
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireActivity(),
                        R.raw.map_light_style
                    )
                )
            }
        }
    }
}
