package com.noteapp.shared.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.noteapp.shared.database.NoteAppDatabase
import com.noteapp.shared.database.NoteEntity
import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalNoteRepository(private val database: NoteAppDatabase) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        database.noteAppQueries.getAllNotes()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toNote() } }

    override suspend fun getNoteById(id: String): Note? =
        withContext(Dispatchers.Default) {
            database.noteAppQueries.getNoteById(id).executeAsOneOrNull()?.toNote()
        }

    override suspend fun saveNote(note: Note) =
        withContext(Dispatchers.Default) {
            database.noteAppQueries.insertOrReplaceNote(
                id = note.id,
                title = note.title,
                htmlContent = note.htmlContent,
                markdownContent = note.markdownContent,
                colorHex = note.colorHex,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt
            )
        }

    override suspend fun deleteNote(id: String) =
        withContext(Dispatchers.Default) {
            database.noteAppQueries.deleteNote(id)
        }

    private fun NoteEntity.toNote() = Note(
        id = id,
        title = title,
        htmlContent = htmlContent,
        markdownContent = markdownContent,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
