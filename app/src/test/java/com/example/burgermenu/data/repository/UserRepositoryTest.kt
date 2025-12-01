package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.network.ApiUser
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

class UserRepositoryTest {

    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        mockkObject(BurgerApiClient)
        repository = UserRepository()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAllUsers returns success with list of users`() = runTest {
        // Given
        val apiUsers = listOf(
            ApiUser("1", "user1", "user1@test.com", "User 1", "123", "Address 1", "admin", "password"),
            ApiUser("2", "user2", "user2@test.com", "User 2", "456", "Address 2", "user", "password")
        )
        coEvery { BurgerApiClient.getAllUsers() } returns Result.success(apiUsers)

        // When
        val result = repository.getAllUsers()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("user1", result.getOrNull()?.first()?.username)
    }

    @Test
    fun `getAllUsers returns failure when api fails`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { BurgerApiClient.getAllUsers() } returns Result.failure(exception)

        // When
        val result = repository.getAllUsers()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `getUserById returns success when user exists`() = runTest {
        // Given
        val apiUser = ApiUser("1", "user1", "user1@test.com", "User 1", "123", "Address 1", "admin", "password")
        coEvery { BurgerApiClient.getUserById("1") } returns Result.success(apiUser)

        // When
        val result = repository.getUserById("1")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("user1", result.getOrNull()?.username)
    }

    @Test
    fun `createUser returns success`() = runTest {
        // Given
        coEvery { BurgerApiClient.createUser(any()) } returns Result.success(ApiUser("1", "user1", "email", "name", "phone", "address", "role", "pass"))

        // When
        val result = repository.createUser("user1", "email", "name", "phone", "address")

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }
}
