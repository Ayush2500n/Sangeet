package com.example.sangeet.di

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.sangeet.di.Firebase.provideFirebaseApp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.android.HiltAndroidApp

val CHANNEL_ID = "music_channel_id"
val CHANNEL_NAME = "music_channel_name"
@HiltAndroidApp
class Sangeet: Application() {

    override fun onCreate() {
        super.onCreate()
        provideFirebaseApp(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

        }
    }
}