package com.example.sangeet.dataClasses

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class UserPref(
    val preferredArtists: List<Artists> = emptyList(),
    val likedSongs: List<Song> = emptyList(),
    val playlists: List<Song> = emptyList()
)