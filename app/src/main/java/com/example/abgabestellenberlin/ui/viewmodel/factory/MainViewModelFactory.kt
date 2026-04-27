package com.example.abgabestellenberlin.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import com.example.abgabestellenberlin.ui.viewmodel.MainViewModel

class MainViewModelFactory(private val repository: DropOffRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
