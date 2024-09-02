package com.example.sangeet.repository

import android.util.Log
import com.example.sangeet.dataClasses.Song
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.google.firebase.firestore.FieldPath


class repo @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val db: FirebaseFirestore
) {
    private val storageRef = firebaseStorage.reference

    suspend fun getSongs() {
        val genres = listOf("Bollywood", "English", "Punjabi")

        withContext(Dispatchers.IO) {
            for (genre in genres) {
                val genreRef = storageRef.child(genre)
                val subgenres = genreRef.listAll().await().prefixes

                for (subgenre in subgenres) {
                    val songFolder = subgenre.child("audio")
                    val coverArtFolder = subgenre.child("cover")

                    val songFiles = songFolder.listAll().await().items
                    val coverArtFiles = coverArtFolder.listAll().await().items

                    // Creating a map of cover art files with the cleaned-up names
                    val coverArtMap = coverArtFiles.associateBy { cleanFileName(it.name) }

                    for (songRef in songFiles) {
                        // Extract the base name of the song file (without extension)
                        val rawSongName = songRef.name.substringBeforeLast('.')
                        val cleanedSongName = cleanFileName(rawSongName)

                        // Log the file name being processed
                        Log.d("Processing Song", "Processing song: $cleanedSongName")

                        // Attempt to find a matching cover art file
                        val coverArtUrl = try {
                            val coverArtRef = coverArtMap[cleanedSongName]
                            coverArtRef?.let {
                                withContext(Dispatchers.IO) {
                                    it.downloadUrl.await().toString()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Cover Art Error", "Cover art not found for: $cleanedSongName")
                            // No matching cover art found, set to null
                            null
                        }

                        val songUrl = try {
                            songRef.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            Log.e("Song URL Error", "Failed to get song URL for: $cleanedSongName")
                            // Skip to next if song URL cannot be fetched
                            continue
                        }

                        if (!checkIfSongExists(cleanedSongName)) {
                            val song = Song(
                                trackName = cleanedSongName,
                                genre = genre,
                                subGenre = subgenre.name,
                                audioUrl = songUrl,
                                imageUrl = coverArtUrl
                            )
                            Log.d("Song", "${song.subGenre} ${song.genre.toString()}, ${song.trackName}, ${song.subGenre}, ${song.audioUrl}, ${song.imageUrl}")
                            saveSong(song)
                        }
                    }
                }
            }
        }
    }
    suspend fun getAllData(): DocumentSnapshot? {
        val data = db.collection("songs").document("allSongs").get().addOnSuccessListener {
            Log.d("Data", it.data.toString())
        }
        return data.await()
    }
    private fun cleanFileName(fileName: String): String {
        val specialCharacters = listOf('(', ')', '|', '.')
        val otherSpecialCharacters = listOf(':', "#")
        var cleanedName = fileName
        for (char in otherSpecialCharacters) {
            if (cleanedName.contains(char.toString())) {
                cleanedName = cleanedName.substringAfter(char.toString()).trim()
            }
        }
        for (char in specialCharacters) {
            if (cleanedName.contains(char.toString())) {
                cleanedName = cleanedName.substringBefore(char.toString())
            }
        }


        return cleanedName.trim()
    }

    private suspend fun checkIfSongExists(songName: String): Boolean {
        Log.d("Check song exists", "Checking if song exists: $songName")
        return try {
            val docSnapshot = db.collection("songs").document("allSongs")
                .get().await()

            docSnapshot.exists() && docSnapshot.contains(songName)
        } catch (e: Exception) {
            Log.e("Check song exists error", e.message.toString())
            false
        }
    }

    private fun saveSong(song: Song) {
        val songMap = mapOf(
            "songName" to song.trackName,
            "genre" to song.genre,
            "subgenre" to song.subGenre,
            "songUrl" to song.audioUrl,
            "imageUrl" to song.imageUrl
        )

        try {
            // Use FieldPath.of() to handle special characters in field names
            val songRef = db.collection("songs").document("allSongs")
            songRef.update(FieldPath.of(song.trackName), songMap)
                .addOnSuccessListener {
                    Log.d("Save success", "Song saved: ${song.trackName}")
                }
                .addOnFailureListener {
                    Log.e("Save failure", it.message.toString())
                }
        } catch (e: Exception) {
            Log.e("Save error", e.message.toString())
        }
    }
}
