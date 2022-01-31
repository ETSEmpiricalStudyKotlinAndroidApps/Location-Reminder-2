package com.cryoggen.locationreminder.addeditreminder

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.databinding.AddreminderFragBinding
import com.cryoggen.locationreminder.main.ADD_EDIT_RESULT_OK
import com.cryoggen.locationreminder.map.MapReminder
import com.cryoggen.locationreminder.util.setupSnackbar
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
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

    //id reminder, which is loaded onto the map
    private var idUploadReminder = ""

    private lateinit var googleMap: GoogleMap

    //wrapper object for googleMap object
    private lateinit var mapReminder: MapReminder

    private lateinit var viewDataBinding: AddreminderFragBinding

    private val args: AddEditReminderFragmentArgs by navArgs()

    private val viewModel by viewModels<AddEditReminderViewModel>()

    //checks if the googleMap.moveCamera() method has already been executed.
    // It is necessary that the method is not executed every time the current device coordinates are updated
    private var checkOnLocationResult = false

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

        settingsMapView()

        return viewDataBinding.root
    }

    //Map display settings
    private fun settingsMapView() {
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

    //If the data was loaded from the database, then we display it on the map.
    private fun observeUploadReminderDataToMap() {
        viewModel.uploadReminderDataToMap.observe(viewLifecycleOwner, {
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
        viewModel.loadDataFromMap.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.updateCoordinatesFromMap(mapReminder.latitude, mapReminder.longitude)
            }
        })
    }

    private fun observeSaveReminder() {
        viewModel.savedReminder.observe(viewLifecycleOwner, {
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

        this.googleMap = googleMap

        checkDarkStyleMap()

        mapReminder = MapReminder(googleMap, requireActivity())

        //sets whether it is necessary to track clicks on the map
        mapReminder.switchOnMapClick(true)

        //after the map is ready, we are waiting for the data to be loaded from the database, and then we load it onto the map
        observeUploadReminderDataToMap()

        //keeps track of saving data from the map to the repository
        observeLoadDataFromMap()

        //when saving a reminder, create a geofence for the reminder
        observeSaveReminder()
        moveCameraCurrentLocation()
    }

    //Determines the mode on the phone, night or day.
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

    @SuppressLint("MissingPermission")

    private fun moveCameraCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
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
                if (!checkOnLocationResult) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15f))
                    checkOnLocationResult = true
                }
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

    private fun unsubscribeToLocationUpdates() {

        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        } catch (unlikely: SecurityException) {

        }

    }

}
