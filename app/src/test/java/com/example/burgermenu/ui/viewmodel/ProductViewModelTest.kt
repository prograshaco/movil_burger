package com.example.burgermenu.ui.viewmodel

import android.util.Log
import com.example.burgermenu.data.models.Product
import com.example.burgermenu.data.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private lateinit var viewModel: ProductViewModel
    private lateinit var repository: ProductRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProducts updates state with products on success`() = runTest {
        // Given
        val products = listOf(
            Product("1", "Burger", "Desc", 1000, "Food", "", 1, "")
        )
        val categories = listOf("Food")
        coEvery { repository.getAllProducts() } returns Result.success(products)
        coEvery { repository.getCategories() } returns Result.success(categories)

        // When
        viewModel = ProductViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(products, state.products)
        assertEquals(null, state.error)
    }

    @Test
    fun `loadProducts updates state with error on failure`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { repository.getAllProducts() } returns Result.failure(exception)
        coEvery { repository.getCategories() } returns Result.success(emptyList())

        // When
        viewModel = ProductViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.products.isEmpty())
        assertEquals("Network error", state.error)
    }
}
