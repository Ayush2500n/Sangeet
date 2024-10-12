package com.example.sangeet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.onboardingPages.FirstPage
import com.example.sangeet.onboardingPages.SecondPage
import com.example.sangeet.repository.repo
import com.example.sangeet.repositry.GoogleAuthClient
import com.example.sangeet.repositry.onboarding
import com.example.sangeet.screens.HomeScreen
import com.example.sangeet.screens.MoodsSelect
import com.example.sangeet.ui.theme.SangeetTheme
import com.example.sangeet.viewModels.SignInViewModel
import com.example.sangeet.viewModels.moodsViewModel
import com.example.sangeet.viewModels.onboardingViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repo: repo
    @Inject
    lateinit var onboardingRepo: onboarding
    private val googleAuthUiClient by lazy {
        GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SangeetTheme {
                LaunchedEffect(key1 = null) {
                    withContext(Dispatchers.IO){
                        repo.determineMoodsList()
                    }
                }
                val navController = rememberNavController()
                var currentUser by remember {
                    mutableStateOf(Firebase.auth.currentUser)
                }
                NavHost(navController = navController, startDestination = if (currentUser == null) "firstPage" else "secondPage") {
                    composable("firstPage") {
                        val googleAuthViewModel = viewModel<SignInViewModel>()
                        val state by googleAuthViewModel.state.collectAsStateWithLifecycle()
                        LaunchedEffect(key1 = Unit) {
                            if(googleAuthUiClient.getSignedInUser() != null) {
                                navController.navigate("HomeScreen")
                            }
                        }
                        val coroutineScope = rememberCoroutineScope()
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult(),
                            onResult = { result ->
                                if (result.resultCode == RESULT_OK) {
                                    coroutineScope.launch {
                                        val signInResult = googleAuthUiClient.getSignInResultFromIntent(
                                            intent = result.data ?: return@launch
                                        )
                                        googleAuthViewModel.onSignInResult(signInResult)
                                    }
                                }
                                else {
                                    Log.d("GoogleSignIn", "Sign-in failed or canceled")
                                }
                            }
                        )
                        DisposableEffect(Unit) {
                            val listener = FirebaseAuth.AuthStateListener { auth ->
                                currentUser = auth.currentUser
                            }
                            Firebase.auth.addAuthStateListener(listener)

                            onDispose {
                                Firebase.auth.removeAuthStateListener(listener)
                            }
                        }
                        LaunchedEffect(key1 = currentUser) {
                            if (currentUser != null){
                                navController.navigate("HomeScreen"){
                                    popUpTo("firstPage"){
                                        inclusive = true
                                    }
                                }
                            }
                        }
                        FirstPage(state =state,
                            onSignInClick = {
                                coroutineScope.launch {
                                    val signInIntentSender = googleAuthUiClient.signIn()
                                    if (signInIntentSender != null) {
                                        launcher.launch(
                                            IntentSenderRequest.Builder(signInIntentSender).build()
                                        )
                                    } else {
                                        Log.d("GoogleSignIn", "SignInIntentSender is null")
                                    }
                                }
                            }, onContinueGuestClick = {
                                coroutineScope.launch {
                                    googleAuthUiClient.authUser()
                                }
                            }, navController, currentUser)
                    }
                    composable("HomeScreen") {
                        HomeScreen(navController)
                    }
                     composable("MoodsSelect") { backStackEntry ->
                        val moodsViewModel: moodsViewModel = hiltViewModel()
                        val mood = navController.previousBackStackEntry?.savedStateHandle?.get<Moods>("mood")
                        BackHandler {
                             navController.popBackStack()
                        }
                        MoodsSelect(moodsViewModel, mood)

                    }
                    composable("secondPage"){
                        val onboardingViewModel: onboardingViewModel = hiltViewModel()
                        SecondPage(onboardingViewModel)
                    }
                }
            }
        }
    }
}

