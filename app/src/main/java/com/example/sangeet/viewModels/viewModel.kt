package com.example.sangeet.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Artists
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.repository.HomeScreenRepo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class viewModel @Inject constructor(val repo: HomeScreenRepo, val db: FirebaseFirestore): ViewModel() {
    private val _songsList = MutableLiveData<List<Song>>()
    val songsList: LiveData<List<Song>> get() = _songsList
    fun fetchSongs() {
            try {
                viewModelScope.launch {
                    withContext(Dispatchers.Main){
                        _songsList.value = repo.getAllSongs().value
                    }
                }
                Log.d("ViewModel", "Songs fetched: ${songsList.value}")
            } catch (e: Exception) {
                Log.e("ViewModel Error", "Error fetching songs: ${e.message}")
            }
    }
    suspend fun getMoods(): List<Moods> {
        val uniqueMoods = repo.determineMoodsList()
        return uniqueMoods.map { mood ->
            val formattedMood = mood.capitalizeFirstLetter() // Capitalize the first letter
            val resourceId = getDrawableResourceId(mood)
            Moods(formattedMood, resourceId)
        }
    }

    fun fetchSongsByUserPref(artists: List<Artists>) {
        viewModelScope.launch {
            // Fetch the songs using the suspend function
            val songs = repo.getSongsByUserPref(artists)
            _songsList.value = songs.value
        }
    }

    private fun getDrawableResourceId(mood: String): Int {
        val drawableName = mood.replace(" ", "_").lowercase()
        return try {
            val resId = R.drawable::class.java.getField(drawableName).getInt(null)
            resId
        } catch (e: Exception) {
            Log.d("Error creating image", "$e")
        }
    }
    private fun String.capitalizeFirstLetter(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}