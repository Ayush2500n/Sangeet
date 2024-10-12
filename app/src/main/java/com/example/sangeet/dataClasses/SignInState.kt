package com.example.sangeet.dataClasses

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val signInLoading: Boolean = false
)
