package com.example.abgabestellenberlin.data.remote

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.example.abgabestellenberlin.BuildConfig

class GoogleSheetsService(private val context: Context) {

    companion object {
        private const val APP_NAME = "Abgabestellen Berlin"
        private const val RANGE_DROP_OFF_POINTS = "Abgabestellen!A2:P"
        private const val RANGE_SUGGESTIONS = "Vorschläge!A:E"
        private const val RANGE_COLLABORATORS = "Mitarbeiter!A:A"
    }


    fun getPublicSheetsService(): Sheets {
        return Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            null
        ).setApplicationName(APP_NAME).build()
    }

    fun getSheetsService(account: GoogleSignInAccount): Sheets {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(SheetsScopes.SPREADSHEETS)
        ).setSelectedAccount(account.account)

        return Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(APP_NAME).build()
    }

    suspend fun fetchDropOffPoints(service: Sheets): List<List<Any>>? {
        val range = RANGE_DROP_OFF_POINTS // Assuming headers are in row 1, Lat is O (14), Lon is P (15)
        val response = service.spreadsheets().values()
            .get(BuildConfig.SPREADSHEET_ID, range)
            .setKey(BuildConfig.SHEETS_API_KEY)
            .execute()
        return response.getValues()
    }

    suspend fun submitSuggestion(service: Sheets, values: List<Any>) {
        val range = RANGE_SUGGESTIONS
        val valueRange = com.google.api.services.sheets.v4.model.ValueRange()
        valueRange.setValues(listOf(values))
        
        service.spreadsheets().values()
            .append(BuildConfig.SPREADSHEET_ID, range, valueRange)
            .setValueInputOption("RAW")
            .execute()
    }

    suspend fun fetchCollaborators(service: Sheets): List<String> {
        val range = RANGE_COLLABORATORS
        val response = service.spreadsheets().values()
            .get(BuildConfig.SPREADSHEET_ID, range)
            .setKey(BuildConfig.SHEETS_API_KEY)
            .execute()
        return response.getValues()?.flatMap { row -> row.map { it.toString() } } ?: emptyList()
    }
}
