package com.noteapp.di

import com.noteapp.ui.screen.edit.EditViewModel
import com.noteapp.ui.screen.home.HomeViewModel
import org.koin.dsl.module

val appModule = module {
    factory { HomeViewModel(get(), get()) }
    factory { (noteId: String?) -> EditViewModel(noteId, get(), get(), get()) }
}
