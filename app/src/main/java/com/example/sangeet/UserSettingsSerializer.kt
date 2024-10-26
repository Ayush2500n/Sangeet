package com.example.sangeet

import android.util.Log
import androidx.datastore.core.IOException
import com.example.sangeet.dataClasses.UserPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserSettingsSerializer: androidx.datastore.core.Serializer<UserPref> {
    override val defaultValue: UserPref
        get() = UserPref() // Make sure this initializes with valid default data

    override suspend fun readFrom(input: InputStream): UserPref {
        return try {
            val jsonString = input.readBytes().decodeToString()
            Log.d("Serializer", "Reading from datastore: $jsonString")
            if (jsonString.isEmpty()) {
                Log.w("Serializer", "DataStore is empty, returning default value")
                defaultValue
            } else {
                Json.decodeFromString(
                    deserializer = UserPref.serializer(),
                    string = jsonString
                )
            }
        } catch (e: SerializationException) {
            Log.e("Serializer", "Error reading preferences", e)
            defaultValue // Ensure to return a valid default in case of error
        } catch (e: IOException) {
            Log.e("Serializer", "IO Exception while reading preferences", e)
            defaultValue // Also return default on IO exceptions
        }
    }

    override suspend fun writeTo(t: UserPref, output: OutputStream) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = Json.encodeToString(UserPref.serializer(), value = t)
                Log.d("Serializer", "Writing to datastore: $jsonString")
                output.write(jsonString.encodeToByteArray())
            } catch (e: Exception) {
                Log.e("Serializer", "Error writing preferences", e)
            }
        }
    }
}
