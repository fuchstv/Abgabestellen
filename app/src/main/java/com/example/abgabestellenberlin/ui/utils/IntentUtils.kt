package com.example.abgabestellenberlin.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun sendSuggestionEmail(context: Context, dropOffPointName: String) {
    val emailAddress = "abgabestellen.berlin@foodsharing.network"
    val subject = "Änderungsvorschlag für Abgabestelle: $dropOffPointName"
    val body = "Hallo liebes Team,\n\nich habe folgende Ergänzung/Korrektur für die Abgabestelle '$dropOffPointName':\n\n"

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        // Das "mailto:" stellt sicher, dass NUR E-Mail-Apps (und keine Messenger) geöffnet werden
        data = Uri.parse("mailto:") 
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback, falls der Nutzer gar keine E-Mail-App installiert hat
        Toast.makeText(context, "Keine E-Mail-App gefunden.", Toast.LENGTH_LONG).show()
    }
}
