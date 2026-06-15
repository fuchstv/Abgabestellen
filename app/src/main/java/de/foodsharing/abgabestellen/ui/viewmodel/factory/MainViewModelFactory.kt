package de.foodsharing.abgabestellen.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.foodsharing.abgabestellen.data.repository.DropOffRepository
import de.foodsharing.abgabestellen.ui.viewmodel.MainViewModel

class MainViewModelFactory(private val repository: DropOffRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
