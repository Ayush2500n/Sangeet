package com.example.sangeet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.example.sangeet.repository.repo
import com.example.sangeet.screens.HomeScreen
import com.example.sangeet.ui.theme.SangeetTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repo: repo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SangeetTheme {
                HomeScreen()
                LaunchedEffect(key1 = null) {
                    authUser()
                    repo.getSongs()
                }
            }
        }
    }
    suspend fun authUser() {
        try {
            val auth = Firebase.auth
            val authResult = auth.signInAnonymously().await()
            if (authResult.user != null){
                Log.d("Anonymous sign in ", "succesfull")
            }
            else{
                Log.d("Anonymous sign in ", "failed")
            }
        } catch (e: Exception) {
            Log.d("Anonymous sign in ", "failed ${e.message}")
        }
    }
}

