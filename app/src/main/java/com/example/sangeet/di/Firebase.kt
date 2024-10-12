package com.example.sangeet.di

import android.content.Context
import com.example.sangeet.repository.repo
import com.example.sangeet.repositry.onboarding
import com.example.sangeet.viewModels.viewModel
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
    fun provideRepo(firebaseStorage: FirebaseStorage, db: FirebaseFirestore): repo {
        return repo(firebaseStorage, db)
    }

    @Provides
    fun provideOnboardingRepo(db: FirebaseFirestore, storage: FirebaseStorage): onboarding {
        return onboarding(db, storage)
    }

}