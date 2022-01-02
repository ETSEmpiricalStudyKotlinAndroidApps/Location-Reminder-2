package com.cryoggen.locationreminder.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

enum class AuthenticationState {
    AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
}

class MainActivityViewModel : ViewModel() {
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}