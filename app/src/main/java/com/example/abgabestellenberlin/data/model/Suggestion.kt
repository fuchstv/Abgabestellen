package com.example.abgabestellenberlin.data.model

data class Suggestion(
    val originalPointName: String,
    val suggestedChanges: Map<String, String>,
    val userEmail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: SuggestionStatus = SuggestionStatus.PENDING
)

enum class SuggestionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
