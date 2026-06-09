package com.noteapp.shared.domain.model

data class Note(
    val id: String,
    val title: String,
    val htmlContent: String,
    val markdownContent: String,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long
)
