package com.example.sangeet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import com.example.sangeet.repositry.repo
import com.example.sangeet.ui.theme.SangeetTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
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
