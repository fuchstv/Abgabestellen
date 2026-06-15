package de.foodsharing.abgabestellen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.foodsharing.abgabestellen.data.model.DropOffPoint
import de.foodsharing.abgabestellen.data.repository.DropOffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DropOffRepository) : ViewModel() {

    private val _dropOffPoints = MutableStateFlow<List<DropOffPoint>>(emptyList())
    val dropOffPoints: StateFlow<List<DropOffPoint>> = _dropOffPoints

    private val _selectedPoint = MutableStateFlow<DropOffPoint?>(null)
    val selectedPoint: StateFlow<DropOffPoint?> = _selectedPoint

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        refreshData()
    }

    fun selectPoint(point: DropOffPoint?) {
        _selectedPoint.value = point
    }

    fun refreshData() {
        viewModelScope.launch {
            _errorMessage.value = null
            val points = repository.getDropOffPoints()
            _dropOffPoints.value = points
            if (points.isEmpty()) {
                _errorMessage.value = "Keine Daten gefunden. Bitte prüfe deine Internetverbindung."
            }
        }
    }
}
