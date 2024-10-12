package com.example.sangeet.repositry

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.sangeet.R
import com.example.sangeet.dataClasses.SignInResult
import com.example.sangeet.dataClasses.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException


class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth
    suspend fun signIn(): IntentSender? {
        return try {
            oneTapClient.beginSignIn(buildSignInRequest()).await().pendingIntent.intentSender
        } catch (e: Exception) {
            when (e) {
                is ApiException -> {
                    Log.e("GoogleAuthClient", "ApiException: ${e.statusCode} ${e.message}")
                }
                is CancellationException -> throw e
                else -> {
                    Log.e("GoogleAuthClient", "Unknown exception: ${e.message}", e)
                }
            }

            null
        }
    }
    suspend fun authUser() {
        try {
            val auth = com.google.firebase.ktx.Firebase.auth
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
    suspend fun getSignInResultFromIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        if (googleIdToken.isNullOrEmpty()) {
            Log.d("GoogleAuthClient", "Google ID token is null")
            return SignInResult(data = null, errorMessage = "Google ID token is null")
        }

        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val auth = auth.signInWithCredential(googleCredentials).await().user
            Log.d("GoogleAuthClient", "Sign-in successfull")
            SignInResult(
                data = auth?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                })

        }catch (e:Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e else Log.d("GoogleAuthClient","Sign-in failed")
            SignInResult(
                data = null,
                errorMessage = e.message)
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser() = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
        Log.d("GoogleSignIn", "${displayName}, $uid")
    }

    fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.server_client_id))  // Ensure this is correct
                    .setFilterByAuthorizedAccounts(false) // Set to true if you want only accounts previously used in your app
                    .build()
            )
            .setAutoSelectEnabled(false) // If true, automatically signs in the user
            .build()
    }

}

