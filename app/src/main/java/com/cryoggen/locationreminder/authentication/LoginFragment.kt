package com.cryoggen.locationreminder.authentication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cryoggen.locationreminder.BuildConfig
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.databinding.LoginFragmentBinding
import com.cryoggen.locationreminder.map.ConstantsPermissions.BACKGROUND_LOCATION_PERMISSION_INDEX
import com.cryoggen.locationreminder.map.ConstantsPermissions.LOCATION_PERMISSION_INDEX
import com.cryoggen.locationreminder.map.ConstantsPermissions.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.cryoggen.locationreminder.map.ConstantsPermissions.REQUEST_TURN_DEVICE_LOCATION_ON
import com.cryoggen.locationreminder.map.ConstantsPermissions.SIGN_IN_RESULT_CODE
import com.cryoggen.locationreminder.map.ConstantsPermissions.TAG
import com.cryoggen.locationreminder.map._chekStatusLocationSettingsAndStartGeofence
import com.cryoggen.locationreminder.map.checkDeviceLocationSettingsAndStartGeofence
import com.cryoggen.locationreminder.map.checkPermissionsAndStartGeofencing
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.login_fragment, container, false)
        observeAuthenticationState()
        observePermissonState()
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {


                // Пользователь успешно вошел в систему
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.getUid()}!"
                )

            } else {
                // Ошибка входа. Если ответ равен нулю, пользователь отменил
                // процесс входа с помощью кнопки возврата. В противном случае проверьте
                // response.getError (). getErrorCode () и обрабатываем ошибку.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    checkPermissionsAndStartGeofencing()
                }
                else -> {
                    launchSignInFlow()
                }
            }
        })
    }


    private fun observePermissonState() {
        _chekStatusLocationSettingsAndStartGeofence.observe(viewLifecycleOwner, Observer { premissionState ->
            if (premissionState) {
                    val action = LoginFragmentDirections
                        .actionLoginFragmentToRemindersFragmentDest()
                    findNavController().navigate(action)
                }

        })
    }


    private fun launchSignInFlow() {
        // Give users the ability to login / register by email
        // If users choose to register with their email address,
        // they will also need to create a password
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and run the login intent.
        // We listen for the response of this action with
        // SIGN_IN_RESULT_CODE code
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }




}