package com.example.abgabestellenberlin.ui.viewmodel

import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.mock
import com.example.abgabestellenberlin.data.repository.DropOffRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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
}
