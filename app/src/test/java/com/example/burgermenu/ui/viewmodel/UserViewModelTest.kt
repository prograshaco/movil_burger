package com.example.burgermenu.ui.viewmodel

import com.example.burgermenu.data.models.User
import com.example.burgermenu.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
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
class UserViewModelTest {

    private lateinit var viewModel: UserViewModel
    private lateinit var repository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers updates state with users on success`() = runTest {
        // Given
        val users = listOf(
            User("1", "user1", "email", "pass", "name", "role", "phone", "address")
        )
        coEvery { repository.getAllUsers() } returns Result.success(users)

        // When
        viewModel = UserViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(users, state.users)
        assertEquals(null, state.error)
    }

    @Test
    fun `loadUsers updates state with error on failure`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { repository.getAllUsers() } returns Result.failure(exception)

        // When
        viewModel = UserViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.users.isEmpty())
        assertEquals("Network error", state.error)
    }
}
