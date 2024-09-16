package com.example.sangeet.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.repository.repo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class moodsViewModel @Inject constructor(val repo: repo) : ViewModel() {
    suspend fun getMoodData(subgenre: String): List<Song> {
        return repo.getSongsByMood(subgenre)  // Trigger data fetch
    }
}
