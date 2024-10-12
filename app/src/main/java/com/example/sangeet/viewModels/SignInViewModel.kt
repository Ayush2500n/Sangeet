package com.example.sangeet.viewModels

import androidx.lifecycle.ViewModel
import com.example.sangeet.dataClasses.SignInResult
import com.example.sangeet.dataClasses.SignInState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel: ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()
//    init {
//        Firebase.auth.addAuthStateListener { auth ->
//            _currentUser.value = auth.currentUser
//        }
//    }
    fun onSignInResult(result: SignInResult){
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }
    fun resetState(){
        _state.update { SignInState() }
    }
}