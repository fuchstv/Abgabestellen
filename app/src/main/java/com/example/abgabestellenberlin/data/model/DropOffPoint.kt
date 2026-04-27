package com.example.abgabestellenberlin.data.model

data class DropOffPoint(
    val name: String,
    val address: String,
    val zipCode: String,
    val district: String,
    val neighborhood: String,
    val contactPerson: String,
    val phone: String,
    val dropOffTimes: String,
    val acceptedItems: String,
    val phoneRegistrationRequired: String,
    val capacity: String,
    val remarks: String,
    val website: String,
    val lastUpdated: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)
