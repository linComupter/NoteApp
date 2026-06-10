package com.noteapp.shared.di

import com.noteapp.shared.data.local.DatabaseDriverFactory
import com.noteapp.shared.data.local.LocalNoteRepository
import com.noteapp.shared.database.NoteAppDatabase
import com.noteapp.shared.domain.repository.NoteRepository
import com.noteapp.shared.domain.usecase.DeleteNoteUseCase
import com.noteapp.shared.domain.usecase.ExportMarkdownUseCase
import com.noteapp.shared.domain.usecase.GetAllNotesUseCase
import com.noteapp.shared.domain.usecase.SaveNoteUseCase
import org.koin.dsl.module

val sharedModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { NoteAppDatabase(get()) }
    single<NoteRepository> { LocalNoteRepository(get()) }
    factory { GetAllNotesUseCase(get()) }
    factory { SaveNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }
    factory { ExportMarkdownUseCase() }
}
