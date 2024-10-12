package com.example.sangeet.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.repository.repo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class moodsViewModel @Inject constructor(val repo: repo) : ViewModel() {
    private val _moodSongs = MutableLiveData<List<Song>>()
    val moodSongs: LiveData<List<Song>> get() = _moodSongs
    fun getMoodData(subgenre: String): LiveData<List<Song>> {
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                _moodSongs.value = repo.getSongsByMood(subgenre).value
            }
        }
        Log.d("MoodSelect", "Final list as of viewModel is ${moodSongs.value}")
        return moodSongs
    }
}
