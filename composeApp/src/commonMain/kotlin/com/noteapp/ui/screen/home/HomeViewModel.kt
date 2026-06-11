package com.noteapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.DeleteNoteUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    getAllNotes: GetAllNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    val notes: StateFlow<List<Note>> = getAllNotes.execute()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteNote(id: String) {
        viewModelScope.launch { deleteNoteUseCase.execute(id) }
    }
}
