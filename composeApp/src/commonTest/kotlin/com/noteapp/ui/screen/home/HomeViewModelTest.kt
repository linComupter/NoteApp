package com.noteapp.ui.screen.home

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.DeleteNoteUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.ui.FakeNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeNoteRepository
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeNoteRepository()
        viewModel = HomeViewModel(GetAllNotesUseCase(repo), DeleteNoteUseCase(repo))
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun sampleNote(id: String) = Note(
        id = id, title = "Note $id", htmlContent = "<p>content</p>",
        markdownContent = "content", colorHex = "#FEF3C7",
        createdAt = 1000L, updatedAt = 1000L
    )

    @Test
    fun `notes state reflects repository flow`() = runTest {
        repo.saveNote(sampleNote("1"))
        repo.saveNote(sampleNote("2"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.notes.value.size)
    }

    @Test
    fun `deleteNote removes note`() = runTest {
        repo.saveNote(sampleNote("1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.deleteNote("1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.notes.value.isEmpty())
    }
}
