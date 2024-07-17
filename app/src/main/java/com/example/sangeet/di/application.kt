package com.example.sangeet.di

import android.app.Application
import com.example.sangeet.di.Firebase.provideFirebaseApp
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Sangeet: Application() {
    override fun onCreate() {
        super.onCreate()
        provideFirebaseApp(this)
    }
}