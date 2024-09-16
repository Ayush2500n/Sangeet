package com.example.sangeet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.repository.repo
import com.example.sangeet.screens.HomeScreen
import com.example.sangeet.ui.theme.SangeetTheme
import com.example.sangeet.viewModels.moodsViewModel
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
        setContent {
            SangeetTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "HomeScreen") {
                    composable("HomeScreen") {
                        HomeScreen(navController)
                    }
                     composable("MoodsSelect") { backStackEntry ->
                        val moodsViewModel: moodsViewModel = hiltViewModel()
                        val mood = navController.previousBackStackEntry?.savedStateHandle?.get<Moods>("mood")
                        Log.d("MoodNav2", "${mood!!.label}, ${mood.coverArt}" )
//                        MoodsSelect(moodsViewModel, mood)
                    }
                }
                LaunchedEffect(key1 = null) {
                    authUser()
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

