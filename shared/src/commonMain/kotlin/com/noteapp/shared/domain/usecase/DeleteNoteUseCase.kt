package com.noteapp.shared.domain.usecase

import com.noteapp.shared.domain.repository.NoteRepository

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend fun execute(id: String) = repository.deleteNote(id)
}
