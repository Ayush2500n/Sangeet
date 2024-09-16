package com.example.sangeet.repository

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sangeet.dataClasses.Song
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


class repo @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val db: FirebaseFirestore
) {
    private val storageRef = firebaseStorage.reference
    val collectionOfMoods =
        listOf("feel good", "party", "rap", "retro", "rock", "romance", "sad", "travel", "workout")
    private val languages = listOf("english", "hindi", "punjabi")
    suspend fun getSongsByMood(mood: String): MutableList<Song> {
        val listOfSongs = mutableListOf<Song>()
        for (language in languages) {
            withContext(Dispatchers.IO) {
                val songs = db.collection(mood).document(language).get().await()
                Log.d("Songs", songs.toObject(Song::class.java).toString())
                songs.toObject(Song::class.java)?.let { listOfSongs.add(it) }
            }
        }
        return listOfSongs
    }

    private val _songsList = MutableLiveData<List<Song>>()
    val songsList: LiveData<List<Song>> = _songsList
    private var hasFetched = false
    suspend fun getAllSongs(limit: Int = 4, forceRefresh: Boolean = false): LiveData<List<Song>> {
        val tempList = mutableListOf<Song>()
        if (hasFetched && !forceRefresh) {
            return songsList
        }
        withContext(Dispatchers.IO)
        {
        for (mood in collectionOfMoods) {
            for (language in languages) {
                    try {
                        val documentSnapshot = db.collection(mood).document(language).get().await()

                        if (documentSnapshot.exists()) {
                            val songsData = documentSnapshot.data
                            val limitedSongs =
                                songsData?.entries?.toList()?.shuffled()?.take(limit)

                            limitedSongs?.forEach { entry ->
                                val songMap = entry.value as? Map<*, *>
                                if (songMap != null) {
                                    val name = songMap["name"] as? String ?: return@forEach
                                    val genre = songMap["genre"] as? String
                                    val language = songMap["language"] as? String ?: return@forEach
                                    val audioUrl = songMap["audioUrl"] as? String ?: return@forEach
                                    val song = Song(name, genre, null, language, audioUrl, null)
                                    tempList.add(song)
                                }
                                Log.d("repo limited songs", "$tempList")
                            }
                        } else {
                            Log.d(
                                "Firestore",
                                "Document does not exist for mood: $mood and language: $language"
                            )
                        }

                    } catch (e: Exception) {
                        Log.e(
                            "Firestore Error",
                            "Error fetching songs for mood: $mood, language: $language. ${e.message}"
                        )
                    }
                }
            }
        }

        // Update LiveData with the limited songs
        _songsList.value = tempList
        hasFetched = true
        return songsList
    }


}
