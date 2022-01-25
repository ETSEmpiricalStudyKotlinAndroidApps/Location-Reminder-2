package com.cryoggen.locationreminder.addeditreminder

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.databinding.AddreminderFragBinding
import com.cryoggen.locationreminder.main.ADD_EDIT_RESULT_OK
import com.cryoggen.locationreminder.map.MapReminder
import com.cryoggen.locationreminder.reminders.util.setupSnackbar
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar


/**
 * Main UI for the add reminder screen. Users can enter a reminder title and description.
 */
class AddEditReminderFragment : Fragment(), OnMapReadyCallback {

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback


    private var idUploadReminder = ""

    private lateinit var googleMap: GoogleMap

    private lateinit var mapReminder: MapReminder

    private lateinit var viewDataBinding: AddreminderFragBinding

    private val args: AddEditReminderFragmentArgs by navArgs()

    private val viewModel by viewModels<AddEditReminderViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root = inflater.inflate(R.layout.addreminder_frag, container, false)
        viewDataBinding = AddreminderFragBinding.bind(root).apply {
            this.viewmodel = viewModel
        }
        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        setingsMapView()

        return viewDataBinding.root
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
        layoutParams.marginEnd = 65
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

    private fun observeUploadReminderDataToMap() {
        viewModel.uploadReminderDataToMap.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mapReminder.longitude = it.longitude
                mapReminder.latitude = it.latitude
                mapReminder.addMarker()

                //save upload Reminder id
                idUploadReminder = it.id

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribeToLocationUpdates()
    }

    private fun observeLoadDataFromMap() {
        viewModel.loadDataFromMap.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.updateСoordinatesFromMap(mapReminder.latitude, mapReminder.longitude)
            }
        })
    }

    private fun observeSaveReminder() {
        viewModel.savedReminder.observe(viewLifecycleOwner, Observer {
        })
    }

    private fun setupNavigation() {
        viewModel.reminderUpdatedEvent.observe(viewLifecycleOwner, EventObserver {
            val action = AddEditReminderFragmentDirections
                .actionAddEditReminderFragmentToRemindersFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        moveCameraСurrentLocation()
        this.googleMap = googleMap

        mapReminder = MapReminder(googleMap, requireActivity())
        mapReminder.switchMapLongClick(true)

        //after the map is ready, we are waiting for the data to be loaded from the database, and then we load it onto the map
        observeUploadReminderDataToMap()

        //keeps track of saving data from the map to the repository
        observeLoadDataFromMap()

        //when saving a reminder, create a geofence for the reminder
        observeSaveReminder()

    }
    @SuppressLint("MissingPermission")
    private fun moveCameraСurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 100
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
               val homeLatLng = LatLng(
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15f))
                //            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
            }

        }
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            //checks if there are permissions to access geolocation

        }
    }

    fun unsubscribeToLocationUpdates() {

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)


        } catch (unlikely: SecurityException) {

        }

    }

}
