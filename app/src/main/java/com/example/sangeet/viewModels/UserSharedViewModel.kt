package com.example.sangeet.viewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.dataClasses.Artists
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.dataClasses.UserData
import com.example.sangeet.datastore
import com.example.sangeet.repositry.Users
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSharedViewModel @Inject constructor(private val userRepo: Users) : ViewModel() {
    private val _userData: MutableLiveData<UserData?> = MutableLiveData(null)
    val userData: LiveData<UserData?> get() = _userData
    private val _response = MutableStateFlow(false)
    val response: StateFlow<Boolean> get() = _response
    val currentUser = Firebase.auth.currentUser

    fun setPreferences(artists: List<Artists>, context: Context) {
       viewModelScope.launch {
           Log.d("UserCheck - UserSharedViewModel", "setPreferences: $artists")
           context.datastore.updateData {
               it.copy(preferredArtists = artists)
           }
       }
    }

    fun setLikes(songs: List<Song>) {
        _userData.value = _userData.value?.copy(likedSongs = songs)
        Log.d("SharedViewModel", "Liked songs updated: $songs")

        _userData.value?.let { currentUser ->
            saveUser(mapOf("likedSongs" to currentUser.likedSongs))
        }
    }

    fun setPlaylist(songs: List<Song>) {
        _userData.value = _userData.value?.copy(playlist = songs)
        Log.d("SharedViewModel", "Playlist updated: $songs")

        _userData.value?.let { currentUser ->
            saveUser(mapOf("playlist" to currentUser.playlist))
        }
    }

    fun setBasicDetails(uid: String, username: String, profileUrl: String) {
        if (_userData.value == null) {
            _userData.value = UserData(userId = uid, username = username, profilePictureUrl = profileUrl, likedSongs = null, playlist = null, preferences = null)
        } else {
            _userData.value = _userData.value?.copy(userId = uid, username = username, profilePictureUrl = profileUrl)
        }

        Log.d("SharedViewModel", "Basic details updated: ${_userData.value}")

        _userData.value?.let { currentUser ->
            saveUser(
                mapOf(
                    "userId" to currentUser.userId,
                    "username" to currentUser.username,
                    "profilePictureUrl" to currentUser.profilePictureUrl
                )
            )
        }
    }


    fun userCheck(context: Context): Boolean {
        viewModelScope.launch {
            context.datastore.data.collect { preferences ->
                Log.d("UserCheck - UserSharedViewModel", "setPreferences: $preferences")

                // Check if preferredArtists is not empty
                if (preferences.preferredArtists.isNotEmpty()) {
                    Log.d("UserCheck - UserSharedViewModel", "userCheck: Preferences found ${preferences.preferredArtists}")

                    // Initialize _userData if null
                    if (_userData.value == null) {
                        _userData.value = UserData(
                            userId = currentUser?.uid, // Adjust this according to your actual user data structure
                            username = currentUser?.email,
                            profilePictureUrl = currentUser?.photoUrl.toString(),
                            preferences = preferences.preferredArtists // Initialize with found preferences
                        )
                    } else if (_userData.value?.preferences?.isEmpty() == true) {
                        // Update userData if preferences were previously empty
                        _userData.value = _userData.value?.copy(preferences = preferences.preferredArtists)
                    } else {
                        // Otherwise, just update preferences
                        _userData.value = _userData.value?.copy(preferences = preferences.preferredArtists)
                    }

                    Log.d("UserCheck - UserSharedViewModel", "userCheck: Updated UserData ${_userData.value}")
                }

                // If preferredArtists is not empty, set response to true
                if (preferences.preferredArtists.isNotEmpty()) {
                    _response.value = true
                }
            }
        }
        return _response.value ?: false // Return response or false if it's null
    }


    fun saveUser(data: Map<String, Any?>) {
        viewModelScope.launch {
            val userId = userData.value?.userId ?: return@launch
            userRepo.saveUser(uid = userId, data = data)
        }
    }
}
