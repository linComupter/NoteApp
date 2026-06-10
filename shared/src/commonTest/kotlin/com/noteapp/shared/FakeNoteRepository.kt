package com.noteapp.shared

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    private val flow = MutableStateFlow<List<Note>>(emptyList())

    override fun getAllNotes(): Flow<List<Note>> = flow

    override suspend fun getNoteById(id: String): Note? = notes.find { it.id == id }

    override suspend fun saveNote(note: Note) {
        notes.removeAll { it.id == note.id }
        notes.add(note)
        flow.value = notes.toList()
    }

    override suspend fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
        flow.value = notes.toList()
    }
}
