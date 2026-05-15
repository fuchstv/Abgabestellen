package com.example.abgabestellenberlin.data.repository

import android.util.Log
import com.example.abgabestellenberlin.data.model.DropOffPoint
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class DropOffRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getDropOffPoints(): List<DropOffPoint> {
        return try {
            val snapshot: QuerySnapshot = db.collection("abgabestellen").get().await()
            snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
                val location = doc.getGeoPoint("location")
                DropOffPoint(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    anschrift = doc.getString("anschrift") ?: "",
                    plz = doc.getString("plz") ?: "",
                    ortsteil = doc.getString("ortsteil") ?: "",
                    ansprechpartner = doc.getString("ansprechpartner") ?: "",
                    telefon = doc.getString("telefon") ?: "",
                    annahmezeiten = doc.getString("annahmezeiten") ?: "",
                    akzeptiert = doc.getString("akzeptiert") ?: "",
                    anmeldungNoetig = doc.getBoolean("anmeldungNoetig") ?: false,
                    bemerkungen = doc.getString("bemerkungen") ?: "",
                    latitude = location?.latitude ?: 0.0,
                    longitude = location?.longitude ?: 0.0
                )
            }
        } catch (e: Exception) {
            Log.e("DropOffRepository", "Error fetching points", e)
            emptyList()
        }
    }

    suspend fun isCollaborator(email: String): Boolean {
        return try {
            val doc: DocumentSnapshot = db.collection("mitarbeiter").document(email).get().await()
            doc.exists()
        } catch (e: Exception) {
            Log.e("DropOffRepository", "Error checking collaborator status", e)
            false
        }
    }

    suspend fun submitSuggestion(pointId: String, name: String, suggestion: String, userEmail: String) {
        try {
            val data = hashMapOf(
                "pointId" to pointId,
                "name" to name,
                "suggestion" to suggestion,
                "userEmail" to userEmail,
                "timestamp" to Timestamp.now(),
                "status" to "PENDING"
            )
            db.collection("vorschlaege").add(data).await()
        } catch (e: Exception) {
            Log.e("DropOffRepository", "Error submitting suggestion", e)
            throw e
        }
    }
}
