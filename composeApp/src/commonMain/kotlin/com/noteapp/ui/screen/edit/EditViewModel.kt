package com.noteapp.ui.screen.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteapp.shared.domain.model.Note
import com.noteapp.shared.domain.usecase.ExportMarkdownUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.shared.domain.usecase.SaveNoteUseCase
import com.noteapp.ui.theme.noteColorPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class EditViewModel(
    noteId: String?,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val exportMarkdownUseCase: ExportMarkdownUseCase
) : ViewModel() {

    val noteId: String = noteId ?: generateId()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _isWysiwygMode = MutableStateFlow(true)
    val isWysiwygMode: StateFlow<Boolean> = _isWysiwygMode

    private val _markdownSource = MutableStateFlow("")
    val markdownSource: StateFlow<String> = _markdownSource

    private val _colorHex = MutableStateFlow(randomNoteColor())
    val colorHex: StateFlow<String> = _colorHex

    private val _initialHtml = MutableStateFlow("")
    val initialHtml: StateFlow<String> = _initialHtml

    private var isNewNote = noteId == null

    init {
        if (!isNewNote) {
            viewModelScope.launch { loadNote() }
        }
    }

    private suspend fun loadNote() {
        val notes = getAllNotesUseCase.execute().first()
        notes.find { it.id == this.noteId }?.let { note ->
            _title.value = note.title
            _initialHtml.value = note.htmlContent
            _colorHex.value = note.colorHex
        }
    }

    fun updateTitle(value: String) { _title.value = value }

    fun updateColorHex(hex: String) { _colorHex.value = hex }

    fun toggleMode() { _isWysiwygMode.value = !_isWysiwygMode.value }

    fun updateMarkdownSource(md: String) { _markdownSource.value = md }

    fun saveNote(htmlContent: String) {
        val markdown = exportMarkdownUseCase.execute(htmlContent)
        val now = Clock.System.now().toEpochMilliseconds()
        val note = Note(
            id = noteId,
            title = _title.value,
            htmlContent = htmlContent,
            markdownContent = markdown,
            colorHex = _colorHex.value,
            createdAt = if (isNewNote) 0L else now,
            updatedAt = 0L
        )
        viewModelScope.launch {
            saveNoteUseCase.execute(note)
            isNewNote = false
        }
    }

    private fun generateId(): String {
        val time = Clock.System.now().toEpochMilliseconds().toString(16)
        val rand = (0..0xFFFF).random().toString(16).padStart(4, '0')
        return "$time-$rand"
    }

    private fun randomNoteColor(): String {
        val color = noteColorPalette.random()
        val hex = color.value.toString(16).uppercase()
        return "#" + if (hex.length >= 6) hex.takeLast(6) else hex.padStart(6, '0')
    }
}
