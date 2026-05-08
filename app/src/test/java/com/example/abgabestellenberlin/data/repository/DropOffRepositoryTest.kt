package com.example.abgabestellenberlin.data.repository

import com.example.abgabestellenberlin.data.model.DropOffPoint
import com.example.abgabestellenberlin.data.remote.GoogleSheetsService
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.sheets.v4.Sheets
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After

@OptIn(ExperimentalCoroutinesApi::class)
class DropOffRepositoryTest {

    private lateinit var sheetsService: GoogleSheetsService
    private lateinit var repository: DropOffRepository
    private lateinit var mockSheets: Sheets
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sheetsService = mock()
        mockSheets = mock()
        repository = DropOffRepository(sheetsService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGetDropOffPointsErrorHandling() = runTest(testDispatcher) {
        whenever(sheetsService.getPublicSheetsService()).thenReturn(mockSheets)
        whenever(sheetsService.fetchDropOffPoints(any())).thenThrow(RuntimeException("Network error"))

        // Mocking android.util.Log.e to avoid "Method e in android.util.Log not mocked"
        mockStatic(android.util.Log::class.java).use { mockedLog ->
            mockedLog.`when`<Int> { android.util.Log.e(any(), any(), any()) }.thenReturn(0)

            val result = repository.getDropOffPoints(null)

            assertEquals(emptyList<DropOffPoint>(), result)
        }
    }

    @Test
    fun testGetDropOffPointsSuccess() = runTest(testDispatcher) {
        whenever(sheetsService.getPublicSheetsService()).thenReturn(mockSheets)

        val mockData = listOf(
            listOf("Test Name", "Test Address", "10115", "Mitte", "Rosenthaler Platz", "Test Person", "12345", "Mo-Fr 10-18", "Kleidung", "Nein", "Hoch", "Test Remarks", "www.test.de", "2023-10-27", "52.5200", "13.4050")
        )
        whenever(sheetsService.fetchDropOffPoints(any())).thenReturn(mockData)

        val result = repository.getDropOffPoints(null)

        assertEquals(1, result.size)
        assertEquals("Test Name", result[0].name)
        assertEquals("Test Address", result[0].address)
        assertEquals("10115", result[0].zipCode)
        assertEquals(52.5200, result[0].latitude)
        assertEquals(13.4050, result[0].longitude)
    }

    @Test
    fun testGetDropOffPointsWithAccountSuccess() = runTest(testDispatcher) {
        val mockAccount = mock<GoogleSignInAccount>()
        whenever(sheetsService.getSheetsService(mockAccount)).thenReturn(mockSheets)

        val mockData = listOf(
            listOf("Private Name", "Private Address")
        )
        whenever(sheetsService.fetchDropOffPoints(any())).thenReturn(mockData)

        val result = repository.getDropOffPoints(mockAccount)

        assertEquals(1, result.size)
        assertEquals("Private Name", result[0].name)
        assertEquals("Private Address", result[0].address)
    }

    @Test
    fun testGetCollaboratorsErrorHandling() = runTest(testDispatcher) {
        val mockAccount = mock<GoogleSignInAccount>()
        whenever(sheetsService.getSheetsService(mockAccount)).thenReturn(mockSheets)
        whenever(sheetsService.fetchCollaborators(any())).thenThrow(RuntimeException("Auth error"))

        mockStatic(android.util.Log::class.java).use { mockedLog ->
            mockedLog.`when`<Int> { android.util.Log.e(any(), any(), any()) }.thenReturn(0)

            val result = repository.getCollaborators(mockAccount)

            assertEquals(emptyList<String>(), result)
        }
    }

    @Test
    fun testGetCollaboratorsSuccess() = runTest(testDispatcher) {
        val mockAccount = mock<GoogleSignInAccount>()
        whenever(sheetsService.getSheetsService(mockAccount)).thenReturn(mockSheets)
        val mockCollaborators = listOf("user1@example.com", "user2@example.com")
        whenever(sheetsService.fetchCollaborators(any())).thenReturn(mockCollaborators)

        val result = repository.getCollaborators(mockAccount)

        assertEquals(2, result.size)
        assertEquals("user1@example.com", result[0])
        assertEquals("user2@example.com", result[1])
    }
}
