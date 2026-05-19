package com.example.abgabestellenberlin.ui.viewmodel

import com.example.abgabestellenberlin.data.model.DropOffPoint
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val mockRepository: DropOffRepository = mock()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshData with empty list sets error message`() = runTest {
        // Arrange
        whenever(mockRepository.getDropOffPoints()).thenReturn(emptyList())

        // Act
        viewModel = MainViewModel(mockRepository)
        viewModel.refreshData()

        // Assert
        assertEquals("Keine Daten gefunden. Bitte prüfe deine Internetverbindung.", viewModel.errorMessage.value)
        assertEquals(emptyList<DropOffPoint>(), viewModel.dropOffPoints.value)
    }
}
