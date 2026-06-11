package com.noteapp.ui.screen.edit

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.ExportMarkdownUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.shared.domain.usecase.SaveNoteUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeNoteRepository
    private lateinit var viewModel: EditViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeNoteRepository()
    }

    @AfterTest
    fun teardown() { Dispatchers.resetMain() }

    private fun makeViewModel(noteId: String? = null) = EditViewModel(
        noteId = noteId,
        saveNoteUseCase = SaveNoteUseCase(repo),
        getAllNotesUseCase = GetAllNotesUseCase(repo),
        exportMarkdownUseCase = ExportMarkdownUseCase()
    )

    @Test
    fun `new note starts with empty title and WYSIWYG mode`() = runTest {
        viewModel = makeViewModel(noteId = null)
        assertEquals("", viewModel.title.value)
        assertTrue(viewModel.isWysiwygMode.value)
    }

    @Test
    fun `existing note loads title on init`() = runTest {
        repo.saveNote(Note("id1", "Hello", "<p>World</p>", "World", "#FEF3C7", 1000L, 1000L))
        viewModel = makeViewModel(noteId = "id1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Hello", viewModel.title.value)
    }

    @Test
    fun `toggleMode switches between WYSIWYG and MD source`() = runTest {
        viewModel = makeViewModel()
        assertTrue(viewModel.isWysiwygMode.value)
        viewModel.toggleMode()
        assertFalse(viewModel.isWysiwygMode.value)
        viewModel.toggleMode()
        assertTrue(viewModel.isWysiwygMode.value)
    }

    @Test
    fun `saveNote persists note to repository`() = runTest {
        viewModel = makeViewModel(noteId = null)
        viewModel.updateTitle("My Note")
        viewModel.saveNote("<p>Content</p>")
        testDispatcher.scheduler.advanceUntilIdle()
        val saved = repo.getNoteById(viewModel.noteId)
        assertNotNull(saved)
        assertEquals("My Note", saved.title)
    }
}
