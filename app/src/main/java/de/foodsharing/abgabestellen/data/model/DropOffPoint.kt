package de.foodsharing.abgabestellen.data.model

data class DropOffPoint(
    val id: String = "", // Wird von Firestore generiert
    val name: String = "",
    val anschrift: String = "",
    val plz: String = "",
    val ortsteil: String = "",
    val ansprechpartner: String = "",
    val telefon: String = "",
    val annahmezeiten: String = "",
    val akzeptiert: String = "",
    val anmeldungNoetig: Boolean = false,
    val bemerkungen: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
