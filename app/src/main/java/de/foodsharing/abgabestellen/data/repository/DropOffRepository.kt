package de.foodsharing.abgabestellen.data.repository

import android.util.Log
import de.foodsharing.abgabestellen.data.model.DropOffPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException

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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("DropOffRepository", "Error fetching points", e)
            emptyList()
        }
    }
}
