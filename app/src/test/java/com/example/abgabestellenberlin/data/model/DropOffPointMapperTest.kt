package com.example.abgabestellenberlin.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DropOffPointMapperTest {

    @Test
    fun `fromSheetRow with empty list returns DropOffPoint with default empty strings`() {
        val row = emptyList<Any>()
        val result = DropOffPointMapper.fromSheetRow(row)

        assertEquals("", result.name)
        assertEquals("", result.address)
        assertEquals("", result.zipCode)
        assertEquals("", result.district)
        assertEquals("", result.neighborhood)
        assertEquals("", result.contactPerson)
        assertEquals("", result.phone)
        assertEquals("", result.dropOffTimes)
        assertEquals("", result.acceptedItems)
        assertEquals("", result.phoneRegistrationRequired)
        assertEquals("", result.capacity)
        assertEquals("", result.remarks)
        assertEquals("", result.website)
        assertEquals("", result.lastUpdated)
        assertNull(result.latitude)
        assertNull(result.longitude)
    }

    @Test
    fun `fromSheetRow with short list returns mapped and default empty strings`() {
        val row = listOf("Name", "Address", "12345")
        val result = DropOffPointMapper.fromSheetRow(row)

        assertEquals("Name", result.name)
        assertEquals("Address", result.address)
        assertEquals("12345", result.zipCode)
        assertEquals("", result.district)
        assertEquals("", result.neighborhood)
        assertNull(result.latitude)
    }

    @Test
    fun `fromSheetRow with full list maps all fields correctly`() {
        val row = listOf(
            "Point A", "Street 1", "10115", "Mitte", "Wedding", "John Doe", "123456789",
            "10:00 - 18:00", "Clothes, Toys", "Yes", "High", "No remarks", "www.example.com",
            "2023-10-01", "52.5200", "13.4050"
        )
        val result = DropOffPointMapper.fromSheetRow(row)

        assertEquals("Point A", result.name)
        assertEquals("Street 1", result.address)
        assertEquals("10115", result.zipCode)
        assertEquals("Mitte", result.district)
        assertEquals("Wedding", result.neighborhood)
        assertEquals("John Doe", result.contactPerson)
        assertEquals("123456789", result.phone)
        assertEquals("10:00 - 18:00", result.dropOffTimes)
        assertEquals("Clothes, Toys", result.acceptedItems)
        assertEquals("Yes", result.phoneRegistrationRequired)
        assertEquals("High", result.capacity)
        assertEquals("No remarks", result.remarks)
        assertEquals("www.example.com", result.website)
        assertEquals("2023-10-01", result.lastUpdated)
        assertEquals(52.5200, result.latitude)
        assertEquals(13.4050, result.longitude)
    }

    @Test
    fun `fromSheetRow parses coordinates with commas correctly`() {
        val row = MutableList<Any>(16) { "" }
        row[14] = "52,5200"
        row[15] = "13,4050"

        val result = DropOffPointMapper.fromSheetRow(row)

        assertEquals(52.5200, result.latitude)
        assertEquals(13.4050, result.longitude)
    }
}
