package com.example.sangeet.dataClasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Song(
    val name: String?= "Unknown title",
    val genre: String?= "Unknown genre",
    val album: String? = "Unknown Album",
    val language: String?= "Unknown language",
    val audioUrl: String? = "No Url for audio",
    var coverUrl: ByteArray?,
    val subgenre: String?= "Unknown subgenre",
    val artists: String? = "Unknown Artists"
) : Parcelable