package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.network.ApiProduct
import com.example.burgermenu.data.network.BurgerApiClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductRepositoryTest {

    private lateinit var repository: ProductRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        mockkObject(BurgerApiClient)
        repository = ProductRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAllProducts returns success with list of products`() = runTest {
        // Given
        val apiProducts = listOf(
            ApiProduct("1", "Burger", "Delicious", 10.0, "Food", "", 1),
            ApiProduct("2", "Coke", "Cold", 2.0, "Drink", "", 1)
        )
        coEvery { BurgerApiClient.getAllProducts() } returns Result.success(apiProducts)

        // When
        val result = repository.getAllProducts()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Burger", result.getOrNull()?.first()?.name)
        assertEquals(1000, result.getOrNull()?.first()?.price) // Price converted to cents
    }

    @Test
    fun `getAllProducts returns failure when api fails`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { BurgerApiClient.getAllProducts() } returns Result.failure(exception)

        // When
        val result = repository.getAllProducts()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
