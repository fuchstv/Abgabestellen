package com.example.abgabestellenberlin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.abgabestellenberlin.data.model.DropOffPoint
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DropOffRepository) : ViewModel() {

    private val _dropOffPoints = MutableStateFlow<List<DropOffPoint>>(emptyList())
    val dropOffPoints: StateFlow<List<DropOffPoint>> = _dropOffPoints

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser

    private val _isCollaborator = MutableStateFlow(false)
    val isCollaborator: StateFlow<Boolean> = _isCollaborator

    private val _selectedPoint = MutableStateFlow<DropOffPoint?>(null)
    val selectedPoint: StateFlow<DropOffPoint?> = _selectedPoint

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        refreshData()
        _firebaseUser.value?.let { checkCollaboratorStatus(it.email) }
    }

    fun selectPoint(point: DropOffPoint?) {
        _selectedPoint.value = point
    }

    fun updateFirebaseUser(user: FirebaseUser?) {
        _firebaseUser.value = user
        if (user != null) {
            checkCollaboratorStatus(user.email)
        } else {
            _isCollaborator.value = false
        }
    }

    private fun checkCollaboratorStatus(email: String?) {
        if (email == null) return
        viewModelScope.launch {
            _isCollaborator.value = repository.isCollaborator(email)
        }
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

    fun submitSuggestion(point: DropOffPoint, suggestionText: String) {
        viewModelScope.launch {
            val user = _firebaseUser.value ?: return@launch
            try {
                repository.submitSuggestion(
                    pointId = point.id,
                    name = point.name,
                    suggestion = suggestionText,
                    userEmail = user.email ?: "unknown"
                )
            } catch (e: Exception) {
                _errorMessage.value = "Fehler beim Senden des Vorschlags."
            }
        }
    }
}
