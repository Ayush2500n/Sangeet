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
import javax.inject.Inject

@HiltViewModel
class PlayerSharedViewModel @Inject constructor(private val userRepo: Users) : ViewModel() {
    private val _currentSong = mutableStateOf<Song?>(null)
    val currentSong: State<Song?> get() = _currentSong
    private val _currentSongList = mutableStateListOf<Song>()
    val currentSongList: MutableList<Song> get() = _currentSongList
    private val _currentSongIndex = mutableStateOf<Int?>(null)
    val currentSongIndex: MutableState<Int?> get() = _currentSongIndex
    private val _isPlaying = mutableStateOf(false)
    var playStatus: MutableState<Boolean> =  _isPlaying

    fun sendSong(song: Song){
        _currentSong.value = song
    }
    fun sendSongList(songList: List<Song>){
        _currentSongList.addAll(songList)
    }
    fun getCurrentIndex(index: Int){
        currentSongIndex.value = index
    }
    fun setPlayingStatus(isPlaying: Boolean){
        playStatus.value = isPlaying
    }
}