package com.noteapp.ui.screen.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteapp.shared.domain.usecase.DeleteNoteUseCase
import com.noteapp.shared.domain.usecase.ExportMarkdownUseCase
import com.noteapp.shared.domain.usecase.SaveNoteUseCase

// Stub implementation — full implementation in Task 11
class EditViewModel(
    private val noteId: String?,
    private val saveNote: SaveNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val exportMarkdown: ExportMarkdownUseCase
) : ViewModel()
