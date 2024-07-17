package com.example.sangeet.repositry

import android.net.Uri
import android.util.Log
import com.example.sangeet.dataClasses.Song
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.datatype.Artwork
import java.io.File
import javax.inject.Inject

class repo @Inject constructor(val firebaseStorage: FirebaseStorage, val db: FirebaseFirestore) {
    private val storageRef = firebaseStorage.reference
    suspend fun getSongs() {
        val songsRef = storageRef.child("songs")
        val listItems = withContext(Dispatchers.IO){
            songsRef.listAll().await()
        }
        for (items in listItems.items){
            val songName = items.name
            val songUrl = items.downloadUrl.await().toString()
            val localFile = withContext(Dispatchers.IO) {
                File.createTempFile(songName,".mp3")
            }
            try {
                try {
                    items.getFile(localFile).await()
                } catch (e: Exception) {
                    Log.d("Download error", e.message.toString())
                }
                val meta = AudioFileIO.read(localFile)
                val artistName = meta.tag.getFirst(FieldKey.ARTIST).split(",")
                val albumName = meta.tag.getFirst(FieldKey.ALBUM)
                val genre = meta.tag.getFirst(FieldKey.GENRE)
                val language = meta.tag.getFirst(FieldKey.LANGUAGE)
                val cover = meta.tag.firstArtwork
                val imageUrl = getCover(cover)
                Log.d("Download success", "$songName, $artistName, $albumName, $genre, $language, $cover")

                val song = Song(songName, artistName, albumName, genre, subGenre = null, language, songUrl,imageUrl)
                saveSongs(song)
            } catch (e: Exception) {
                Log.d("Some error", e.message.toString())
            } finally {
                localFile.delete()
            }
        }

    }
    fun saveSongs(song: Song) {
        val songMap = mapOf(
            "songName" to song.trackName,
            "artistName" to song.artistName,
            "albumName" to song.albumName,
            "genre" to song.genre,
            "language" to song.language,
            "songUrl" to song.audioUrl,
            "imageUrl" to song.imageUrl
        )
        try {
            db.collection("songs").document("allSongs").set(songMap)
                .addOnSuccessListener {
                    Log.d("Save success", "Song saved: ${song.trackName}")
                }


        } catch (e: Exception) {
            Log.d("Save error", e.message.toString())
        }
    }
    suspend fun getCover(cover: Artwork): String {
        var downloadUrl = ""
        val coverFile = withContext(Dispatchers.IO) {
            File.createTempFile("cover", ".jpg")
        }
        coverFile.writeBytes(cover.binaryData)
        val coverUri = Uri.fromFile(coverFile)
        try {
            val imageRef = storageRef.child("covers/${coverFile.name}")
            val uploadTask = imageRef.putFile(coverUri)
            downloadUrl = uploadTask.await().storage.downloadUrl.await().toString()
            Log.d("Download success", downloadUrl)
        }catch (e:Exception){
            Log.d("Download error", e.message.toString())
        }finally {
            coverFile.delete()
        }
        return downloadUrl
    }
}