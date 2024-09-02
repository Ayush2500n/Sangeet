package com.example.sangeet.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.sangeet.R
import com.example.sangeet.dataClasses.Moods
import com.example.sangeet.dataClasses.Song
import com.example.sangeet.repository.repo
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class viewModel @Inject constructor(val repo: repo, val db: FirebaseFirestore): ViewModel() {
    suspend fun getData(): MutableMap<String, Song>? {
        val songs = repo.getAllData()
        Log.d("Viewmodel Data", "First Data fetched ${songs.toString()}, ${songs?.data.toString()}, ${songs?.data?.values.toString()}")
        if (songs != null && songs.data != null) {
            val songMap = mutableMapOf<String, Song>()

            // Iterate over the map and convert each entry to a Song object
            for ((key, value) in songs.data!!) {
                if (value is Map<*, *>) {
                    val songName = value["songName"] as? String ?: continue
                    val genre = value["genre"] as? String
                    val subgenre = value["subgenre"] as? String ?: continue
                    val songUrl = value["songUrl"] as? String ?: continue
                    val imageUrl = value["imageUrl"] as? String

                    // Create a Song object and add it to the map
                    val song = Song(songName, genre, subgenre, songUrl, imageUrl)
                    songMap[key] = song
                }
            }
            Log.d("Viewmodel Data", " Processed Data fetched ${songMap.values}")
            return songMap
        }
        Log.d("Viewmodel Data", "Data is null")
        return null
    }
    suspend fun getMoods(): List<Moods> {
        val moodsCollection = db.collection("songs")
        val uniqueMoods = mutableSetOf<String>()

        // Fetch the document that contains all songs
        val snapshot = moodsCollection.document("allSongs").get().await()

        // Check if the document exists
        if (snapshot.exists()) {
            // Log the raw snapshot data for debugging
            Log.d("Firestore Snapshot", snapshot.data.toString())

            // Iterate through each song in the document
            val songEntries = snapshot.data ?: emptyMap<String, Any>()
            for ((key, value) in songEntries) {
                Log.d("Processing Song", "Key: $key, Value: $value")

                if (value is Map<*, *>) {
                    // Access the subgenre
                    val subGenre = value["subgenre"] as? String
                    if (subGenre != null) {
                        Log.d("SubGenre Found", "SubGenre: $subGenre")
                        uniqueMoods.add(subGenre)
                    } else {
                        Log.w("SubGenre Missing", "SubGenre is missing for song: $key")
                    }
                }
            }
        } else {
            Log.e("Firestore Error", "Document 'allSongs' does not exist.")
        }

        // Log the unique moods found
        Log.d("Unique Moods", uniqueMoods.toString())

        return uniqueMoods.map { mood ->
            val formattedMood = mood.capitalizeFirstLetter() // Capitalize the first letter
            val resourceId = getDrawableResourceId(mood)
            Moods(formattedMood, resourceId)
        }
    }

    private fun getDrawableResourceId(mood: String): Int {
        val drawableName = mood.replace(" ", "_").lowercase() // Format the mood name to match the resource name
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