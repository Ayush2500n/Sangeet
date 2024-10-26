package com.example.sangeet.repositry

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.sangeet.dataClasses.UserData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class Users @Inject constructor(private val db: FirebaseFirestore) {
    suspend fun userCheck(uid: String): Boolean {
        val users = db.collection("users").get().await()
        val userCheck = mutableStateOf(false)
        for(user in users){
            if (user.id == uid){
                Log.d("UserCheck", "User already exists")
                val userData = user.toObject(UserData::class.java)
                if (userData.preferences == null || userData.preferences.isEmpty()){
                    Log.d("UserCheck", "User doesn't have preferences")
                    userCheck.value = false
                } else {
                    userCheck.value = true
                }
            }else{
                Log.d("UserCheck", "User doesn't exists, saving it......")
            }
        }
        Log.d("UserCheck", "User check completed, preferences are set to ${userCheck.value}")
        return userCheck.value
    }
    fun saveUser(data: Map<String, Any?>, uid: String){
        db.collection("users").document(uid).set(data, SetOptions.merge()).addOnSuccessListener{
            Log.d("UserSave", "User saved successfully")
        }.addOnFailureListener{
            Log.d("UserSave", "User save failed")
        }
    }
}