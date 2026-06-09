package com.noteapp.shared.domain.repository

import com.noteapp.shared.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
    // Reserved for future sync:
    // suspend fun syncNotes()
}
