package com.example.sangeet.dataClasses

data class Song(
    val trackName: String,
    val artistName: List<String>,
    val albumName: String,
    val genre: String?,
    val subGenre: List<String>?,
    val language: String?,
    val audioUrl: String,
    val imageUrl: String
)