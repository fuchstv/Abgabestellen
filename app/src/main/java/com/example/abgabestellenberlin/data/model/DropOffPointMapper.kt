package com.example.abgabestellenberlin.data.model

object DropOffPointMapper {
    private const val COL_NAME = 0
    private const val COL_ADDRESS = 1
    private const val COL_ZIP_CODE = 2
    private const val COL_DISTRICT = 3
    private const val COL_NEIGHBORHOOD = 4
    private const val COL_CONTACT_PERSON = 5
    private const val COL_PHONE = 6
    private const val COL_DROP_OFF_TIMES = 7
    private const val COL_ACCEPTED_ITEMS = 8
    private const val COL_PHONE_REG_REQUIRED = 9
    private const val COL_CAPACITY = 10
    private const val COL_REMARKS = 11
    private const val COL_WEBSITE = 12
    private const val COL_LAST_UPDATED = 13
    private const val COL_LATITUDE = 14
    private const val COL_LONGITUDE = 15

    fun fromSheetRow(row: List<Any>): DropOffPoint {
        return DropOffPoint(
            name = row.getOrNull(COL_NAME)?.toString() ?: "",
            address = row.getOrNull(COL_ADDRESS)?.toString() ?: "",
            zipCode = row.getOrNull(COL_ZIP_CODE)?.toString() ?: "",
            district = row.getOrNull(COL_DISTRICT)?.toString() ?: "",
            neighborhood = row.getOrNull(COL_NEIGHBORHOOD)?.toString() ?: "",
            contactPerson = row.getOrNull(COL_CONTACT_PERSON)?.toString() ?: "",
            phone = row.getOrNull(COL_PHONE)?.toString() ?: "",
            dropOffTimes = row.getOrNull(COL_DROP_OFF_TIMES)?.toString() ?: "",
            acceptedItems = row.getOrNull(COL_ACCEPTED_ITEMS)?.toString() ?: "",
            phoneRegistrationRequired = row.getOrNull(COL_PHONE_REG_REQUIRED)?.toString() ?: "",
            capacity = row.getOrNull(COL_CAPACITY)?.toString() ?: "",
            remarks = row.getOrNull(COL_REMARKS)?.toString() ?: "",
            website = row.getOrNull(COL_WEBSITE)?.toString() ?: "",
            lastUpdated = row.getOrNull(COL_LAST_UPDATED)?.toString() ?: "",
            latitude = row.getOrNull(COL_LATITUDE)?.toString()?.replace(",", ".")?.toDoubleOrNull(),
            longitude = row.getOrNull(COL_LONGITUDE)?.toString()?.replace(",", ".")?.toDoubleOrNull()
        )
    }
}
