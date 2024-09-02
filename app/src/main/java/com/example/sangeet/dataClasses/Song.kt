package com.example.sangeet.dataClasses

data class Song(
    val trackName: String,
    val genre: String?,
    val subGenre: String,
    val audioUrl: String,
    val imageUrl: String?
)