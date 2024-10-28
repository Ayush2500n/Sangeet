package com.example.sangeet.viewModels

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.exoplayer.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerSharedViewModel @Inject constructor() : ViewModel() {

    private val _currentSong = MutableLiveData<Song?>(null)
    val currentSong: LiveData<Song?> = _currentSong

    private val _currentSongList = MutableLiveData<List<Song>>(emptyList())
    val currentSongList: LiveData<List<Song>> = _currentSongList

    private val _currentSongIndex = MutableLiveData<Int?>(0)
    val currentSongIndex: LiveData<Int?> = _currentSongIndex

    private val _isPlaying = MutableLiveData(false)
    val playStatus: LiveData<Boolean> = _isPlaying

    var _currentDuration = MutableStateFlow<Float?>(0f)

    private val _maxDuration = mutableStateOf<Float?>(null)
    val maxDuration: State<Float?> = _maxDuration

    fun observeDurationUpdates(service: MusicService) {
        viewModelScope.launch {
            while (true) {
                val duration = service.binder.currentDuration().value // Call MusicService for current duration
                _currentDuration.value = duration
                delay(1000L) // Update every second, adjust if needed
            }
        }
    }
    // Update current song
    fun sendSong(song: Song) {
        _currentSong.value = song
        _maxDuration.value = song.duration?.toFloat()
    }
    fun extractNewDuration(song: Song?){
        val metadataRetriever = MediaMetadataRetriever()
        viewModelScope.launch {
            try {
                metadataRetriever.setDataSource(song?.audioUrl)
                _maxDuration.value = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toFloat()
            }catch (e: Exception){
                Log.e("Player", "Error extracting duration: ${e.message}")
            }finally {
                metadataRetriever.release()
            }
        }
    }
    fun extractCover(song: Song): ByteArray? {
        var cover: ByteArray? = null
        val metadataRetriever = MediaMetadataRetriever()
        viewModelScope.launch {
            try {
                metadataRetriever.setDataSource(song.audioUrl)
                cover = metadataRetriever.embeddedPicture
                Log.d("Player", "Cover extracted: ${cover != null}")
                return@launch
            } catch (e: Exception) {
                Log.e("Player", "Error extracting cover: ${e.message}")
            } finally {
                metadataRetriever.release()
            }
        }
        return cover
    }
    // Update song list
    fun sendSongList(songList: List<Song>) {
        _currentSongList.value = songList
    }

    // Update the current song index
    fun getCurrentIndex(index: Int) {
        _currentSongIndex.value = index
    }

    // Update play status
    fun setPlayingStatus(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }
    fun nextSong() {
        _currentSongIndex.value?.let { index ->
            val newIndex = (index + 1) % _currentSongList.value?.size!!
            _currentSongIndex.value = newIndex
            _currentSong.value = _currentSongList.value?.get(newIndex)
            extractNewDuration(_currentSong.value)
            Log.d("Player", "Next song called, now the cover is ${_currentSong.value?.coverUrl}")
        }
    }

    fun previousSong() {
        _currentSongIndex.value?.let { index ->
            val newIndex = if (index - 1 < 0) _currentSongList.value?.lastIndex else index - 1
            _currentSongIndex.value = newIndex
            _currentSong.value = newIndex?.let { _currentSongList.value?.get(it) }
            extractNewDuration(_currentSong.value)
            Log.d("Player", "Previous song called, now the cover is ${_currentSong.value?.coverUrl}")
        }
    }
}
