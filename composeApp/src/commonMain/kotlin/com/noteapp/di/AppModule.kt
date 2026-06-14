package com.noteapp.di

import com.noteapp.ui.screen.edit.EditViewModel
import com.noteapp.ui.screen.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::HomeViewModel)
    viewModel { (noteId: String?) -> EditViewModel(noteId, get(), get(), get()) }
}
