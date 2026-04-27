package com.example.abgabestellenberlin.data.model

object DropOffPointMapper {
    fun fromSheetRow(row: List<Any>): DropOffPoint {
        return DropOffPoint(
            name = row.getOrNull(0)?.toString() ?: "",
            address = row.getOrNull(1)?.toString() ?: "",
            zipCode = row.getOrNull(2)?.toString() ?: "",
            district = row.getOrNull(3)?.toString() ?: "",
            neighborhood = row.getOrNull(4)?.toString() ?: "",
            contactPerson = row.getOrNull(5)?.toString() ?: "",
            phone = row.getOrNull(6)?.toString() ?: "",
            dropOffTimes = row.getOrNull(7)?.toString() ?: "",
            acceptedItems = row.getOrNull(8)?.toString() ?: "",
            phoneRegistrationRequired = row.getOrNull(9)?.toString() ?: "",
            capacity = row.getOrNull(10)?.toString() ?: "",
            remarks = row.getOrNull(11)?.toString() ?: "",
            website = row.getOrNull(12)?.toString() ?: "",
            lastUpdated = row.getOrNull(13)?.toString() ?: "",
            latitude = row.getOrNull(14)?.toString()?.replace(",", ".")?.toDoubleOrNull(),
            longitude = row.getOrNull(15)?.toString()?.replace(",", ".")?.toDoubleOrNull()
        )
    }
}
