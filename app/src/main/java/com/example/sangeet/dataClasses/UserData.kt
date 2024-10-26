package com.example.sangeet.dataClasses

data class UserData(
    val username: String? = "Unknown",
    val profilePictureUrl: String? = "",
    val userId: String? = "",
    val preferences: List<Artists>? = emptyList(),
    val playlist: List<Song>? = emptyList(),
    val likedSongs: List<Song>? = emptyList(),
)
