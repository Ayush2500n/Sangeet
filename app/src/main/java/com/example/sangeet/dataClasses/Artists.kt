package com.example.sangeet.dataClasses

import kotlinx.serialization.Serializable

@Serializable
data class Artists(
    val artistName: String = "",
    val photo: String = ""
)
