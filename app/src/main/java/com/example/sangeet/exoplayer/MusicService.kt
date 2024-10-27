package com.example.sangeet.exoplayer

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.di.CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


const val PREV = "prev"
const val PLAY = "play"
const val NEXT = "next"

class MusicService: Service()  {

    private val currentTrack = MutableStateFlow<Song?>(null)
    private val currentTrackIndex = MutableStateFlow<Int?>(0)
    private var songsList = MutableStateFlow<List<Song>>(emptyList())
    private val maxDuration = MutableStateFlow(0f)
    private val currentDuration = MutableStateFlow(0f)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null
    private val isPlaying = MutableStateFlow(false)
    val binder = MusicBinder()
    inner class MusicBinder: Binder(){
        fun getService() = this@MusicService
        fun setSongsList(songs: List<Song>){
            this@MusicService.songsList.value = emptyList()
            this@MusicService.songsList.value = songs
        }
        fun currentDuration() = this@MusicService.currentDuration
        fun maxDuration() = this@MusicService.maxDuration
        fun isPlaying() = this@MusicService.isPlaying
        fun currentTrack() = this@MusicService.currentTrack
    }
    private var mediaPlayer = MediaPlayer()
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    fun updateTrack(selectedSong: Song?, songList: List<Song?>, selectedSongIndex: Int?) {
        Log.d("Player", "updateTrack called with selectedSong: $selectedSong")
        if (selectedSong != null) {
            currentTrack.value = selectedSong
            currentTrackIndex.value = selectedSongIndex
            songsList.value = songList.filterNotNull() // Set the songsList if needed
            playSong(selectedSong) // Start playback for the selected song
        } else {
            Log.e("MusicService", "Selected song or song list is empty")
        }
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                PREV -> prev()
                PLAY -> play()
                NEXT -> next()
                else -> {
                    scope.launch {
                        songsList.collectLatest { songs ->
                            if (songs.isNotEmpty()) {
                                currentTrack.update { songsList.value[currentTrackIndex.value ?: 0] }
                                playSong(currentTrack.value ?: return@collectLatest)
                            } else {
                                Log.e("MusicPlayer", "songsList is empty; delaying playback")
                            }
                        }
                    }
                }
            }
        }
        return START_REDELIVER_INTENT
    }


    fun updateDurations(){
        scope.launch {
            if (mediaPlayer.isPlaying.not()) return@launch
            maxDuration.update { mediaPlayer.duration.toFloat() }
            while (true){
                currentDuration.update { mediaPlayer.currentPosition.toFloat() }
                delay(1000)
            }
        }
    }

    fun prev(){
        job?.cancel()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer()
        val index = songsList.value.indexOf(currentTrack.value)
        val prevIndex = if (index < 0) songsList.value.size - 1 else index - 1
        val prevSong = songsList.value[prevIndex]
        currentTrack.update { prevSong }
        prevSong.audioUrl?.toUri()?.let { mediaPlayer.setDataSource(this, it) }
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            sendNotification(prevSong)
            updateDurations()
        }
    }

    fun next(){
        job?.cancel()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer()
        val index = songsList.value.indexOf(currentTrack.value)
        val nextIndex = (index + 1) % songsList.value.size
        val nextSong = songsList.value[nextIndex]
        currentTrack.update { nextSong }
        nextSong.audioUrl?.let { mediaPlayer.setDataSource(this, it.toUri()) }
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            sendNotification(nextSong)
            updateDurations()
        }
    }

    fun play(){
        if (mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }else{
            mediaPlayer.start()
        }
        currentTrack.value?.let { sendNotification(it) }
    }
    private fun playSong(song: Song){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(song.audioUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            sendNotification(song)
            updateDurations()
        }
    }

    private fun sendNotification(song: Song){
        isPlaying.update { mediaPlayer.isPlaying }
        val session = MediaSessionCompat(this, "music")
        val style = androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2).setMediaSession(session.sessionToken)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(style)
            .setContentTitle(song.name)
            .setContentText(song.artists)
            .addAction(R.drawable.previous, "prev", createPrevPendingIntent())
            .addAction(if (mediaPlayer.isPlaying) R.drawable.pause else R.drawable.play, "play", createPlayPendingIntent())
            .addAction(R.drawable.next, "next", createNextPendingIntent())
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background))
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED){
                startForeground(1, notification)
            }
            else{
                startForeground(1, notification)
            }
        }
    }
    fun createPrevPendingIntent(): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { action = PREV }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    fun createPlayPendingIntent(): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { action = PLAY }
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    fun createNextPendingIntent(): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { action = NEXT }
        return PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
}