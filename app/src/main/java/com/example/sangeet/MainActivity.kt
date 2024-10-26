package com.example.sangeet

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.dataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.exoplayer.MusicService
import com.example.sangeet.onboardingPages.FirstPage
import com.example.sangeet.onboardingPages.SecondPage
import com.example.sangeet.repository.HomeScreenRepo
import com.example.sangeet.repositry.GoogleAuthClient
import com.example.sangeet.repositry.Users
import com.example.sangeet.screens.HomeScreen
import com.example.sangeet.screens.MoodsSelect
import com.example.sangeet.screens.Player
import com.example.sangeet.ui.theme.SangeetTheme
import com.example.sangeet.viewModels.PlayerSharedViewModel
import com.example.sangeet.viewModels.SignInViewModel
import com.example.sangeet.viewModels.UserSharedViewModel
import com.example.sangeet.viewModels.moodsViewModel
import com.example.sangeet.viewModels.onboardingViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

val Context.datastore by dataStore("user_settings", UserSettingsSerializer)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repo: HomeScreenRepo
    @Inject
    lateinit var userRepo: Users

    private val googleAuthUiClient by lazy {
        GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    private val maxDuration = MutableStateFlow(0f)
    private val currentDuration = MutableStateFlow(0f)
    private val currentTrack = MutableStateFlow<Song>(Song(coverUrl = null))
    private val isPlaying = MutableStateFlow<Boolean>(false)
    private var service: MusicService? = null
    private var isBound = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SangeetTheme {
                // Launch background task to determine moods list.
                LaunchedEffect(key1 = null) {
                    withContext(Dispatchers.IO) {
                        repo.determineMoodsList()
                    }
                }
                val userSharedViewModel: UserSharedViewModel = hiltViewModel()
                val playerSharedViewModel: PlayerSharedViewModel = hiltViewModel()
                val connection = object : ServiceConnection{
                    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                        service = (binder as MusicService.MusicBinder).getService()
                        val selectedSong = playerSharedViewModel.currentSong
                        val selectedSongsList = playerSharedViewModel.currentSongList
                        val selectedSongIndex = playerSharedViewModel.currentSongIndex
                        Log.d("Player", "Information received for Intent $selectedSong, $selectedSongsList, $selectedSongIndex")
                        lifecycleScope.launch {
                            if (selectedSongsList != null) {
                                binder.setSongsList(selectedSongsList)
                            }
                            service?.updateTrack(selectedSong.value, selectedSongsList, selectedSongIndex.value)
                        }
                        lifecycleScope.launch {
                            binder.isPlaying().collectLatest {
                                isPlaying.value = it
                            }
                        }
                        lifecycleScope.launch {
                            binder.currentTrack().collectLatest {
                                if (it != null) {
                                    currentTrack.value = it
                                }
                            }
                        }
                        lifecycleScope.launch {
                            binder.currentDuration().collectLatest {
                                currentDuration.value = it
                            }
                        }
                        lifecycleScope.launch {
                            binder.maxDuration().collectLatest {
                                maxDuration.value = it
                            }
                        }
                        isBound = true
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        isBound = false
                    }

                }

                val context = LocalContext.current
                LaunchedEffect(key1 = true) {
                    userSharedViewModel.userCheck(context)
                }
                val navController = rememberNavController()
                var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
                var splashVisible by remember { mutableStateOf(true) }
                val userPreferences by userSharedViewModel.response.collectAsState(initial = false)
                Log.d("UserCheck - MainActivity", "userPreferences: $userPreferences")
                val splash by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(R.raw.sangeet_splash_screen)
                )
                val splashTime = splash?.duration ?: 4000
                LaunchedEffect(key1 = currentUser, key2 = userPreferences) {
                    if (currentUser != null) {
                        if (userPreferences == true){
                            delay(splashTime.toLong())
                            splashVisible = false
                        }else{
                            delay(splashTime.toLong())
                            splashVisible = false
                        }
                    }
                }
                if (splashVisible){
                    LottieAnimation(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black), composition = splash, iterations = 1)
                }else {
                    // Navigation based on the current user and preferences state
                    NavHost(
                        navController = navController,
                        startDestination = when {
                            currentUser == null -> "firstPage"
                            currentUser != null && userPreferences == false -> "secondPage"
                            currentUser != null && userPreferences == true -> "HomeScreen"
                            else -> "firstPage"
                        }
                    ) {
                        composable("firstPage") {
                            val googleAuthViewModel = viewModel<SignInViewModel>()
                            val state by googleAuthViewModel.state.collectAsStateWithLifecycle()

                            val coroutineScope = rememberCoroutineScope()
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        coroutineScope.launch {
                                            val signInResult =
                                                googleAuthUiClient.getSignInResultFromIntent(
                                                    intent = result.data ?: return@launch
                                                )
                                            googleAuthViewModel.onSignInResult(signInResult)

                                            // After successful sign-in, update Firestore with user details
                                            googleAuthViewModel.state.collect { state ->
                                                if (state.isSignInSuccessful) {
                                                    Log.d(
                                                        "GoogleSignIn",
                                                        "Sign-in successful, now saving user basic information"
                                                    )
                                                    currentUser =
                                                        FirebaseAuth.getInstance().currentUser
                                                    currentUser?.let { user ->
                                                        userSharedViewModel.setBasicDetails(
                                                            uid = user.uid,
                                                            profileUrl = user.photoUrl.toString(),
                                                            username = user.displayName ?: "Unknown"
                                                        )
                                                    }
                                                } else if (state.signInLoading) {
                                                    Log.d("GoogleSignIn", "Sign-in in progress")
                                                } else {
                                                    Log.d(
                                                        "GoogleSignIn",
                                                        "Sign-in failed: ${state.signInError}"
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Log.d("GoogleSignIn", "Sign-in failed or canceled")
                                    }
                                }
                            )

                            DisposableEffect(Unit) {
                                val listener = FirebaseAuth.AuthStateListener { auth ->
                                    currentUser = auth.currentUser
                                }
                                FirebaseAuth.getInstance().addAuthStateListener(listener)

                                onDispose {
                                    FirebaseAuth.getInstance().removeAuthStateListener(listener)
                                }
                            }

                            FirstPage(
                                state = state,
                                onSignInClick = {
                                    coroutineScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        signInIntentSender?.let {
                                            launcher.launch(IntentSenderRequest.Builder(it).build())
                                        } ?: Log.d("GoogleSignIn", "SignInIntentSender is null")
                                    }
                                },
                                onContinueGuestClick = {
                                    coroutineScope.launch {
                                        googleAuthUiClient.authUser()
                                    }
                                },
                                navController,
                                currentUser
                            )
                        }

                        composable("secondPage") {
                            val onboardingViewModel: onboardingViewModel = hiltViewModel()
                            SecondPage(
                                onboardingViewModel,
                                navController,
                                currentUser,
                                userSharedViewModel
                            )
                        }

                        composable("HomeScreen") {
                            HomeScreen(navController, userSharedViewModel = userSharedViewModel,
                                playerSharedViewModel = playerSharedViewModel, connection = connection
                            )
                        }

                        composable("MoodsSelect") {
                            val moodsViewModel: moodsViewModel = hiltViewModel()
                            val mood =
                                navController.previousBackStackEntry?.savedStateHandle?.get<Moods>("mood")
                            BackHandler {
                                navController.popBackStack()
                            }
                            MoodsSelect(moodsViewModel, mood, navController, playerSharedViewModel, connection)
                        }
                        composable("Player"){
                            Player(playerSharedViewModel, service)
                        }
                    }
                }
            }
        }
    }
}
