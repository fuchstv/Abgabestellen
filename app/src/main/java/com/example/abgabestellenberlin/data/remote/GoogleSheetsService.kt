package com.example.abgabestellenberlin.data.remote

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class GoogleSheetsService(private val context: Context) {

    private val spreadsheetId = "1-hx7MWXHTsewBqTg1MqRZmbWeGTQHmLUEIRkPSg60l0"
    private val apiKey = "AIzaSyBOUcHxAjEApgTywLfuVXH6a-mSrJAneWM"

    fun getPublicSheetsService(): Sheets {
        return Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            null
        ).setApplicationName("Abgabestellen Berlin").build()
    }

    fun getSheetsService(account: GoogleSignInAccount): Sheets {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(SheetsScopes.SPREADSHEETS)
        ).setSelectedAccount(account.account)

        return Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Abgabestellen Berlin").build()
    }

    suspend fun fetchDropOffPoints(service: Sheets): List<List<Any>>? {
        val range = "Abgabestellen!A2:P" // Assuming headers are in row 1, Lat is O (14), Lon is P (15)
        val response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .setKey(apiKey)
            .execute()
        return response.getValues()
    }

    suspend fun submitSuggestion(service: Sheets, values: List<Any>) {
        val range = "Vorschläge!A:E"
        val valueRange = com.google.api.services.sheets.v4.model.ValueRange()
        valueRange.setValues(listOf(values))
        
        service.spreadsheets().values()
            .append(spreadsheetId, range, valueRange)
            .setValueInputOption("RAW")
            .execute()
    }

    suspend fun fetchCollaborators(service: Sheets): List<String> {
        val range = "Mitarbeiter!A:A"
        val response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .setKey(apiKey)
            .execute()
        return response.getValues()?.flatten()?.map { it.toString() } ?: emptyList()
    }
}
