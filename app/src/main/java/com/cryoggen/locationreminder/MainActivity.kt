package com.cryoggen.locationreminder

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cryoggen.locationreminder.map.ConstantsPermissions
import com.cryoggen.locationreminder.map._chekStatusLocationSettingsAndStartGeofence
import com.cryoggen.locationreminder.map.checkDeviceLocationSettingsAndStartGeofence
import com.cryoggen.locationreminder.map.checkPermissionsAndStartGeofencing
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var activity: AppCompatActivity
    }

    private var startupRestrictionPermissionCheck = true
    private lateinit var chekPermissionLayout: ConstraintLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        setContentView(R.layout.activity_main)
        setupNavigationDrawer()
        chekPermissionLayout = findViewById<ConstraintLayout>(R.id.chek_permision_layout)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navController: NavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration =
            AppBarConfiguration.Builder(
                R.id.reminders_fragment_dest,
                R.id.statistics_fragment_dest,
                R.id.exit_fragment
            )
                .setDrawerLayout(drawerLayout)
                .build()
        setupActionBarWithNavController(navController, appBarConfiguration)
        findViewById<NavigationView>(R.id.nav_view)
            .setupWithNavController(navController)
        observePermissonState()
    }

    override fun onResume() {
        super.onResume()
        if (startupRestrictionPermissionCheck) {
            checkPermissionsAndStartGeofencing()
            startupRestrictionPermissionCheck = false
        }
    }

    override fun onStart() {
        startupRestrictionPermissionCheck = true
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()

    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun setupNavigationDrawer() {
        drawerLayout = (findViewById<DrawerLayout>(R.id.drawer_layout))
            .apply {
                setStatusBarBackground(R.color.colorPrimaryDark)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantsPermissions.REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)

            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(ConstantsPermissions.TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[ConstantsPermissions.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == ConstantsPermissions.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[ConstantsPermissions.BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            chekPermissionLayout.visibility = View.VISIBLE
            Snackbar.make(
                findViewById(R.id.drawer_layout),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startupRestrictionPermissionCheck = true
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun observePermissonState() {
        _chekStatusLocationSettingsAndStartGeofence.observe(this, Observer { premissionState ->
            if (premissionState) {
                chekPermissionLayout.visibility = View.INVISIBLE
            }

        })
    }

}

const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
