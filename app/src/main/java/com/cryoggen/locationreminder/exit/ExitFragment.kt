package com.cryoggen.locationreminder.exit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cryoggen.locationreminder.R
import com.firebase.ui.auth.AuthUI

class ExitFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AuthUI.getInstance().signOut(requireContext())
        val action = ExitFragmentDirections
            .actionExitFragmentDestToRemindersFragmentDest()
        findNavController().navigate(action)
        return inflater.inflate(R.layout.fragment_exit, container, false)
    }


}