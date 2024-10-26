package com.example.sangeet.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.dataClasses.Artists
import com.example.sangeet.repositry.onboarding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class onboardingViewModel @Inject constructor(val repo: onboarding): ViewModel()  {
    private var _artistsList = MutableLiveData<List<Artists>>()
    val artistsList: LiveData<List<Artists>> get() = _artistsList
    private var _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading
    fun getArtists(): LiveData<List<Artists>> {
        try {
            viewModelScope.launch {
                _isLoading.value = true
                _artistsList.value = repo.getArtists().value
                _isLoading.value = false
            }
            Log.d("Artists in ViewModel", "Artists fetched: ${artistsList.value}")
        } catch (e: Exception) {
            Log.e("Artists in ViewModel", "Error fetching artists: ${e.message}")
        }
        return artistsList
    }
}