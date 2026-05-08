package com.example.abgabestellenberlin.ui.viewmodel

import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.anyOrNull
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
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
    fun testSetUserAccountNull() {
        val repository = mock<DropOffRepository>()

        // init block will call refreshData
        val viewModel = MainViewModel(repository)

        // Initial state before testing
        assertEquals(null, viewModel.userAccount.value)

        viewModel.setUserAccount(null)

        assertEquals(false, viewModel.isCollaborator.value)
        assertEquals(null, viewModel.userAccount.value)
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
}
