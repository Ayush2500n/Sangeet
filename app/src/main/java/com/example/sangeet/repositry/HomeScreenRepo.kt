package com.example.sangeet.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sangeet.dataClasses.Artists
import com.example.sangeet.dataClasses.Song
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject


class HomeScreenRepo @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val db: FirebaseFirestore
) {
    private val storageRef = firebaseStorage.reference
    var collectionOfMoods =
        mutableListOf("feel good", "party", "rap", "retro", "rock", "romance", "sad", "travel", "workout")
    private val languages = listOf("english", "hindi", "punjabi")
    private val _songsList = MutableLiveData<List<Song>>()
    val songsList: LiveData<List<Song>> = _songsList
    private var hasFetched = false
    suspend fun determineMoodsList(): MutableList<String> {
        val tempList = mutableSetOf<String>()
        for (collection in collectionOfMoods){
            for (language in languages){
                try {
                    val documentSnapshot = db.collection(collection).document(language).get().await()
                    if (documentSnapshot.exists()){
                        tempList.add(collection)
                    }
                }catch (e:Exception){
                    Log.d("determine Moods", "Error occurred in repo ${e.message}")
                }
            }
        }
        collectionOfMoods = tempList.toMutableList()
        Log.d("determine Moods", "Moods available $collectionOfMoods")
        return collectionOfMoods
    }
    suspend fun getSongsByMood(mood: String, forceRefresh: Boolean = false): LiveData<List<Song>> {
        val listOfSongs = mutableListOf<Song>()
        if (hasFetched && !forceRefresh) {
            return songsList
        }
            withContext(Dispatchers.IO) {
                for (language in languages){
                    try {
                        val songs = db.collection(mood.lowercase(Locale.ROOT)).document(language).get().await()
                        if (songs.exists()){
                            val songsData = songs.data?.let { shuffleSongs(it, 50) }
                            if (songsData != null) {
                                songsData.forEach{
                                    val songMap = it.value as Map<*, *>
                                    val name = songMap["name"] as String
                                    val genre = songMap["genre"] as String
                                    val lang = songMap["language"] as String
                                    val audioUrl = songMap["audioUrl"] as String
                                    val song = Song(name,genre,null,lang,audioUrl,null)
                                    listOfSongs.add(song)
                                }
                            }
                            Log.d("MoodSelect","Song fetched in repo $listOfSongs")
                        }
                        else{
                            Log.d("MoodSelect","No songs found in repo for $language")
                        }
                    }catch (e:Exception){
                        Log.d("MoodSelect","Some error occured ${e.message}")
                    }
                }
            }
        _songsList.value = listOfSongs
        Log.d("Mood Select", "Final list as of repo ${songsList.value}")
        hasFetched = true
        return songsList
    }
    suspend fun getSongsByUserPref(artists: List<Artists>,forceRefresh: Boolean = false): LiveData<List<Song>> {
        val artistNames = artists.map { it.artistName }
        val allSongs = mutableListOf<Song>()

        if (hasFetched && !forceRefresh) {
            return songsList
        }

        withContext(Dispatchers.IO) {
            for (mood in collectionOfMoods) {
                val genreCollection = db.collection(mood)
                val languageSnapshots = genreCollection.get().await()

                for (languageDocument in languageSnapshots.documents) {
                    val songsData = languageDocument.data

                    val tempSongList = mutableListOf<Song>()

                    songsData?.forEach { (songName, songMap) ->
                        val songDetails = songMap as? Map<*, *>

                        // Retrieve contributing artists and filter based on preferred artists
                        val contributingArtists = songDetails?.get("albumArtist") as? String
                        if (contributingArtists != null) {
                            val songArtists = contributingArtists.split(", ").map { it.trim() }
                            if (songArtists.any { artistNames.contains(it) }) {
                                val song = Song(
                                    name = songDetails["name"] as? String ?: "",
                                    artists = contributingArtists,
                                    genre = songDetails["genre"] as? String,
                                    subgenre = songDetails["subgenre"] as? String,
                                    language = songDetails["language"] as? String,
                                    audioUrl = songDetails["audioUrl"] as? String ?: "",
                                    album = songDetails["album"] as? String,
                                    coverUrl = songDetails["coverUrl"] as? ByteArray
                                )
                                tempSongList.add(song)
                            }
                        }
                    }

                    tempSongList.shuffle()
                    allSongs.addAll(tempSongList.take(6))
                }
            }
        }

        // Shuffle the final list and take the first 16 songs
        allSongs.shuffle()
        _songsList.value = allSongs.take(20)
        hasFetched = true
        return songsList
    }





    suspend fun getAllSongs(forceRefresh: Boolean = false): LiveData<List<Song>> {
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
                            val limitedSongs = songsData?.let { shuffleSongs(it, 4) }


                            limitedSongs?.forEach { entry ->
                                val songMap = entry.value as? Map<*, *>
                                if (songMap != null) {
                                    val name = songMap["name"] as? String ?: return@forEach
                                    val genre = songMap["genre"] as? String
                                    val lang = songMap["language"] as? String ?: return@forEach
                                    val audioUrl = songMap["audioUrl"] as? String ?: return@forEach
                                    val song = Song(name, genre, null, lang, audioUrl, null)
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
    fun shuffleSongs(songsData:MutableMap<String, Any>,limit: Int): List<MutableMap.MutableEntry<String, Any>> {
        return songsData.entries.toList().shuffled().take(limit)
    }
}
