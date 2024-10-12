package com.example.sangeet.repositry

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sangeet.dataClasses.Artists
import com.example.sangeet.dataClasses.Song
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

class onboarding @Inject constructor(
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) {
    private val _artistList = MutableLiveData<List<Artists>>()
    val artistsList: LiveData<List<Artists>> = _artistList

    suspend fun getArtists(): LiveData<List<Artists>> {
        val tempList = mutableListOf<Artists>()
        val artists = db.collection("artists").get().await()

        for (document in artists.documents) {
            val artistName = document.id
            val imageUrlList = document.get("imageUrl") as? List<String>
            val photo: String = if (imageUrlList != null && imageUrlList.isNotEmpty()) {
                imageUrlList[Random.nextInt(imageUrlList.size)]
            } else ({
                Log.d("Artists", "Empty list fetched for artists")
            }).toString()

            Log.d("Artists", "Artist: $artistName, Selected Photo: $photo")
            val artist = Artists(artistName, photo)
            tempList.add(artist)
        }

        Log.d("Artists", "List of artists fetched: ${artists.documents}")
        _artistList.value = tempList
        Log.d("Artists", "Artists fetched: ${artistsList.value}")

        return artistsList
    }
}