package com.noteapp.shared.domain.usecase

import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(private val repository: NoteRepository) {
    fun execute(): Flow<List<Note>> = repository.getAllNotes()
}
