package de.foodsharing.abgabestellen.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import de.foodsharing.abgabestellen.data.repository.DropOffRepository
import de.foodsharing.abgabestellen.ui.viewmodel.MainViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock

class MainViewModelFactoryTest {

    private val repository = mock(DropOffRepository::class.java)
    private val factory = MainViewModelFactory(repository)

    @Test
    fun create_withMainViewModelClass_returnsMainViewModel() {
        // Act
        val viewModel = factory.create(MainViewModel::class.java)

        // Assert
        assertNotNull(viewModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun create_withUnknownClass_throwsIllegalArgumentException() {
        // Arrange
        class UnknownViewModel : ViewModel()

        // Act
        factory.create(UnknownViewModel::class.java)
    }
}
