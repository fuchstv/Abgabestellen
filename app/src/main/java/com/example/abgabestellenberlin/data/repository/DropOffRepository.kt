package com.example.abgabestellenberlin.data.repository

import com.example.abgabestellenberlin.data.model.DropOffPoint
import com.example.abgabestellenberlin.data.model.DropOffPointMapper
import com.example.abgabestellenberlin.data.remote.GoogleSheetsService
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DropOffRepository(
    private val sheetsService: GoogleSheetsService
) {
    suspend fun getDropOffPoints(account: GoogleSignInAccount?): List<DropOffPoint> = withContext(Dispatchers.IO) {
        try {
            val service = if (account != null) {
                sheetsService.getSheetsService(account)
            } else {
                sheetsService.getPublicSheetsService()
            }
            val rows = sheetsService.fetchDropOffPoints(service)
            rows?.map { DropOffPointMapper.fromSheetRow(it) } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("DropOffRepository", "Error fetching points", e)
            emptyList()
        }
    }

    suspend fun getCollaborators(account: GoogleSignInAccount): List<String> = withContext(Dispatchers.IO) {
        try {
            val service = sheetsService.getSheetsService(account)
            sheetsService.fetchCollaborators(service)
        } catch (e: Exception) {
            android.util.Log.e("DropOffRepository", "Error fetching collaborators", e)
            emptyList()
        }
    }

    fun getSheetsService(account: GoogleSignInAccount) = sheetsService.getSheetsService(account)

    suspend fun submitSuggestion(service: com.google.api.services.sheets.v4.Sheets, values: List<Any>) = withContext(Dispatchers.IO) {
        sheetsService.submitSuggestion(service, values)
    }
}
