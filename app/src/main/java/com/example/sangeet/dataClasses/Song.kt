package com.example.sangeet.dataClasses

data class Song(
    val name: String?= "Unknown title",
    val genre: String?= "Unknown genre",
    val album: String? = "Unknown Album",
    val language: String?= "Unknown language",
    val audioUrl: String,
    val coverUrl: ByteArray?
)