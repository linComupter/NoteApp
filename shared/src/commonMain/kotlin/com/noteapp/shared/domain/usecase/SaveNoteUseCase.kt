package com.noteapp.shared.domain.usecase

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.datetime.Clock

class SaveNoteUseCase(private val repository: NoteRepository) {
    suspend fun execute(note: Note) {
        val now = Clock.System.now().toEpochMilliseconds()
        val toSave = if (note.createdAt == 0L) {
            note.copy(createdAt = now, updatedAt = now)
        } else {
            note.copy(updatedAt = now)
        }
        repository.saveNote(toSave)
    }
}
