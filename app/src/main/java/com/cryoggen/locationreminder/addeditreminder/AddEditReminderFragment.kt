package com.cryoggen.locationreminder.reminderdetail.addeditreminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cryoggen.locationreminder.EventObserver
import com.cryoggen.locationreminder.R
import com.cryoggen.locationreminder.reminders.util.setupRefreshLayout
import com.cryoggen.locationreminder.reminders.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the add reminder screen. Users can enter a reminder title and description.
 */
class AddEditReminderFragment : Fragment() {

    private lateinit var viewDataBinding: AddreminderFragBinding

    private val args: AddEditreminderFragmentArgs by navArgs()

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
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
        viewModel.start(args.reminderId)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.reminderUpdatedEvent.observe(viewLifecycleOwner, EventObserver {
            val action = AddEditreminderFragmentDirections
                .actionAddEditreminderFragmentToremindersFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }
}
