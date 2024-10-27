package com.example.sangeet.viewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.repositry.Users
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerSharedViewModel @Inject constructor(private val userRepo: Users) : ViewModel() {

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _currentSongList = MutableStateFlow<List<Song>>(emptyList())
    val currentSongList: StateFlow<List<Song>> = _currentSongList

    private val _currentSongIndex = MutableStateFlow<Int?>(null)
    val currentSongIndex: StateFlow<Int?> = _currentSongIndex

    private val _isPlaying = MutableStateFlow(false)
    val playStatus: StateFlow<Boolean> = _isPlaying

    // Update current song
    fun sendSong(song: Song) {
        _currentSong.value = song
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
            val newIndex = (index + 1) % _currentSongList.value.size
            _currentSongIndex.value = newIndex
            _currentSong.value = _currentSongList.value[newIndex]
        }
    }

    fun previousSong() {
        _currentSongIndex.value?.let { index ->
            val newIndex = if (index - 1 < 0) _currentSongList.value.lastIndex else index - 1
            _currentSongIndex.value = newIndex
            _currentSong.value = _currentSongList.value[newIndex]
        }
    }
}
