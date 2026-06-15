package de.foodsharing.abgabestellen.ui.viewmodel

import de.foodsharing.abgabestellen.data.model.DropOffPoint
import de.foodsharing.abgabestellen.data.repository.DropOffRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun `refreshData with non-empty list updates dropOffPoints and sets errorMessage to null`() = runTest {
        // Arrange
        val points = listOf(
            DropOffPoint(id = "1", name = "Point 1"),
            DropOffPoint(id = "2", name = "Point 2")
        )
        whenever(mockRepository.getDropOffPoints()).thenReturn(points)

        // Act
        viewModel = MainViewModel(mockRepository)
        viewModel.refreshData()

        // Assert
        assertEquals(null, viewModel.errorMessage.value)
        assertEquals(points, viewModel.dropOffPoints.value)
    }

    @Test
    fun `selectPoint updates selectedPoint StateFlow`() = runTest {
        // Arrange
        whenever(mockRepository.getDropOffPoints()).thenReturn(emptyList())
        viewModel = MainViewModel(mockRepository)
        val testPoint = DropOffPoint(id = "test-id", name = "Test Point")

        // Act
        viewModel.selectPoint(testPoint)

        // Assert
        assertEquals(testPoint, viewModel.selectedPoint.value)

        // Act (Set to null)
        viewModel.selectPoint(null)

        // Assert
        assertEquals(null, viewModel.selectedPoint.value)
    }
}
