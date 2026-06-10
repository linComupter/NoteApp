package com.noteapp.shared.domain.usecase

import com.noteapp.shared.FakeNoteRepository
import com.noteapp.shared.domain.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteUseCaseTest {

    private val repo = FakeNoteRepository()
    private val getAllNotes = GetAllNotesUseCase(repo)
    private val saveNote = SaveNoteUseCase(repo)
    private val deleteNote = DeleteNoteUseCase(repo)

    private fun sampleNote(id: String = "1") = Note(
        id = id, title = "Test", htmlContent = "<p>Hello</p>",
        markdownContent = "Hello", colorHex = "#FEF3C7",
        createdAt = 0L, updatedAt = 0L
    )

    @Test
    fun `saveNote sets createdAt and updatedAt when new`() = runTest {
        saveNote.execute(sampleNote())
        val notes = getAllNotes.execute().first()
        assertTrue(notes.first().createdAt > 0L)
        assertTrue(notes.first().updatedAt > 0L)
    }

    @Test
    fun `saveNote updates updatedAt but preserves createdAt when existing`() = runTest {
        val original = sampleNote().copy(createdAt = 1000L, updatedAt = 1000L)
        saveNote.execute(original)
        saveNote.execute(original.copy(title = "Updated"))
        val notes = getAllNotes.execute().first()
        assertEquals(1, notes.size)
        assertEquals(1000L, notes.first().createdAt)
        assertTrue(notes.first().updatedAt >= 1000L)
    }

    @Test
    fun `deleteNote removes note from repository`() = runTest {
        saveNote.execute(sampleNote("a"))
        saveNote.execute(sampleNote("b"))
        deleteNote.execute("a")
        val notes = getAllNotes.execute().first()
        assertEquals(1, notes.size)
        assertEquals("b", notes.first().id)
    }

    @Test
    fun `getAllNotes returns empty list initially`() = runTest {
        val notes = getAllNotes.execute().first()
        assertTrue(notes.isEmpty())
    }
}
