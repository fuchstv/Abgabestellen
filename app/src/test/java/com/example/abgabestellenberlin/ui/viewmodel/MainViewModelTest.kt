package com.example.abgabestellenberlin.ui.viewmodel

import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.anyOrNull
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import com.example.abgabestellenberlin.data.model.DropOffPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.mockito.kotlin.any
import org.junit.After
import org.junit.Before
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSetUserAccountNull() = runTest(testDispatcher) {
        val repository = mock<DropOffRepository>()

        whenever(repository.getDropOffPoints(null)).thenReturn(emptyList())

        // init block will call refreshData
        val viewModel = MainViewModel(repository)

        // Initial state before testing
        assertEquals(null, viewModel.userAccount.value)

        viewModel.setUserAccount(null)

        advanceUntilIdle()

        assertEquals(false, viewModel.isCollaborator.value)
        assertEquals(null, viewModel.userAccount.value)
    }

    @Test
    fun testSetUserAccountNotNull() = runTest(testDispatcher) {
        val repository = mock<DropOffRepository>()
        val mockAccount = mock<GoogleSignInAccount>()
        whenever(mockAccount.email).thenReturn("test@example.com")

        val dummyPoint = mock<DropOffPoint>()
        val mockList = listOf(dummyPoint)

        whenever(repository.getDropOffPoints(null)).thenReturn(emptyList())
        whenever(repository.getDropOffPoints(mockAccount)).thenReturn(mockList)
        whenever(repository.getCollaborators(any())).thenReturn(listOf("test@example.com"))

        val viewModel = MainViewModel(repository)

        // Advance initial refreshData coroutine
        advanceUntilIdle()

        viewModel.setUserAccount(mockAccount)

        // Advance setUserAccount coroutines
        advanceUntilIdle()

        assertEquals(mockAccount, viewModel.userAccount.value)
        assertEquals(true, viewModel.isCollaborator.value)
        // verify that refresh data was actually triggered and set the new points
        assertEquals(mockList, viewModel.dropOffPoints.value)
    }

    @Test
    fun testRefreshDataEmptyDataError() = runTest(testDispatcher) {
        val repository = mock<DropOffRepository>()
        whenever(repository.getDropOffPoints(anyOrNull())).thenReturn(emptyList())

        val viewModel = MainViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            "Keine Daten gefunden. Bitte prüfe deine Internetverbindung oder API-Konfiguration.",
            viewModel.errorMessage.value
        )
    }

    @Test
    fun testSelectPoint() {
        val repository = mock<DropOffRepository>()
        val viewModel = MainViewModel(repository)

        // Initial state should be null
        assertNull(viewModel.selectedPoint.value)

        // Create a dummy DropOffPoint
        val dummyPoint = DropOffPoint(
            name = "Test Point",
            address = "Test Address",
            zipCode = "12345",
            district = "Test District",
            neighborhood = "Test Neighborhood",
            contactPerson = "Test Contact",
            phone = "1234567890",
            dropOffTimes = "Test Times",
            acceptedItems = "Test Items",
            phoneRegistrationRequired = "No",
            capacity = "High",
            remarks = "Test Remarks",
            website = "www.test.com",
            lastUpdated = "2023-01-01"
        )

        // Select the point
        viewModel.selectPoint(dummyPoint)
        assertEquals(dummyPoint, viewModel.selectedPoint.value)

        // Deselect the point
        viewModel.selectPoint(null)
        assertNull(viewModel.selectedPoint.value)
    }
}
