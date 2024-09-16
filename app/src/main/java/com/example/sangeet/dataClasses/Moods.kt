package com.example.sangeet.dataClasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
data class Moods(
    val label: String,
    val coverArt: Int
) : Parcelable
