package com.cryoggen.locationreminder.addeditreminder

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.ADD_EDIT_RESULT_OK
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.databinding.AddreminderFragBinding
import com.cryoggen.locationreminder.map.MapReminder
import com.cryoggen.locationreminder.reminders.util.setupSnackbar
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar


/**
 * Main UI for the add reminder screen. Users can enter a reminder title and description.
 */
class AddEditReminderFragment : Fragment(), OnMapReadyCallback {

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "ACTION_GEOFENCE_EVENT"
    }

    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private lateinit var googleMap: GoogleMap

    private lateinit var mapReminder: MapReminder

    private lateinit var viewDataBinding: AddreminderFragBinding

    private val args: AddEditReminderFragmentArgs by navArgs()

    private val viewModel by viewModels<AddEditReminderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.addreminder_frag, container, false)
        viewDataBinding = AddreminderFragBinding.bind(root).apply {
            this.viewmodel = viewModel
        }
        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        setingsMapView()

        return viewDataBinding.root
    }


    private fun addGeofenceForReminder() {

        val currentGeofenceData = GeofencingConstants.LANDMARK_DATA[0]

        val geofence = Geofence.Builder()

            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId("id_zone")

            // Set the circular region of this geofence.
            .setCircularRegion(
                0.0,
                0.0,
                100f
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

            // Create the geofence.
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

//        geofencingClient?.removeGeofences(geofencePendingIntent)?.run {
//            addOnCompleteListener {
                geofencingClient?.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Toast.makeText(
                            requireActivity(), R.string.geofences_added,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    addOnFailureListener {
                        Toast.makeText(
                            requireActivity(), R.string.geofences_not_added,
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
//            }
//        }
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                Toast.makeText(requireContext(), R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences

            }
        }
    }

    private fun setingsMapView() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_new_reminder) as SupportMapFragment
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        setupNavigation()
        viewModel.start(args.reminderId)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.reminderUpdatedEvent.observe(viewLifecycleOwner, EventObserver {
            addGeofenceForReminder()
            val action = AddEditReminderFragmentDirections
                .actionAddEditReminderFragmentToRemindersFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        mapReminder = MapReminder(googleMap, context)
        mapReminder.switchMapLongClick(true)
        viewModel.setMapReminder(mapReminder)
    }


}
