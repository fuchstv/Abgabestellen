package com.example.abgabestellenberlin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.abgabestellenberlin.data.model.DropOffPoint
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DropOffRepository) : ViewModel() {

    private val _dropOffPoints = MutableStateFlow<List<DropOffPoint>>(emptyList())
    val dropOffPoints: StateFlow<List<DropOffPoint>> = _dropOffPoints

    private val _userAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val userAccount: StateFlow<GoogleSignInAccount?> = _userAccount

    private val _isCollaborator = MutableStateFlow(false)
    val isCollaborator: StateFlow<Boolean> = _isCollaborator

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

    fun setUserAccount(account: GoogleSignInAccount?) {
        _userAccount.value = account
        if (account != null) {
            refreshData()
            checkCollaboratorStatus(account)
        } else {
            _isCollaborator.value = false
        }
    }

    private fun checkCollaboratorStatus(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val collaborators = repository.getCollaborators(account)
            _isCollaborator.value = collaborators.contains(account.email)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _errorMessage.value = null
            val points = repository.getDropOffPoints(_userAccount.value)
            _dropOffPoints.value = points
            if (points.isEmpty()) {
                _errorMessage.value = "Keine Daten gefunden. Bitte prüfe deine Internetverbindung oder API-Konfiguration."
            }
        }
    }

    fun submitSuggestion(point: DropOffPoint, suggestionText: String) {
        viewModelScope.launch {
            val account = _userAccount.value ?: return@launch
            val service = repository.getSheetsService(account)
            repository.submitSuggestion(service, listOf(
                point.name,
                suggestionText,
                account.email ?: "unknown",
                System.currentTimeMillis().toString(),
                "PENDING"
            ))
        }
    }
}
