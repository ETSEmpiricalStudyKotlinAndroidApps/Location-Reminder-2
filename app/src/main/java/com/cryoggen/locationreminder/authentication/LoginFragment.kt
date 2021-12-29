package com.cryoggen.locationreminder.authentication


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.databinding.LoginFragmentBinding
import com.cryoggen.locationreminder.map.ConstantsPermissions.SIGN_IN_RESULT_CODE
import com.cryoggen.locationreminder.map._chekStatusLocationSettingsAndStartGeofence
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.login_fragment, container, false)
//        observeAuthenticationState()
        observePermissonState()
        return binding.root
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    val textViewNavHeader =
                        requireActivity().findViewById<TextView>(R.id.textViewNavHeader)
                    textViewNavHeader.text = FirebaseAuth.getInstance().currentUser?.displayName
                    val action = LoginFragmentDirections
                        .actionLoginFragmentToRemindersFragmentDest()
                    findNavController().navigate(action)
                }
                else -> {
                    launchSignInFlow()
                }
            }
        })
    }

    private fun observePermissonState() {
        _chekStatusLocationSettingsAndStartGeofence.observe(
            viewLifecycleOwner,
            Observer { premissionState ->
                if (premissionState) {
                    observeAuthenticationState()
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