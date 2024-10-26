package com.example.sangeet.di

import android.content.Context
import com.example.sangeet.repository.HomeScreenRepo
import com.example.sangeet.repositry.Users
import com.example.sangeet.repositry.onboarding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object Firebase {

    @Provides
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp {
        return FirebaseApp.initializeApp(context)!!
    }

        @Provides
    fun provideFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance(firebaseApp)
    }

    @Provides
    fun provideFirebaseStorage(firebaseApp: FirebaseApp): FirebaseStorage {
        return FirebaseStorage.getInstance(firebaseApp)
    }

    @Provides
    fun provideDatabase(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    fun provideRepo(firebaseStorage: FirebaseStorage, db: FirebaseFirestore): HomeScreenRepo {
        return HomeScreenRepo(firebaseStorage, db)
    }

    @Provides
    fun provideOnboardingRepo(db: FirebaseFirestore, storage: FirebaseStorage): onboarding {
        return onboarding(db, storage)
    }

    @Provides
    fun provideUserRepo(db: FirebaseFirestore): Users {
        return Users(db)
    }
}