package com.noteapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.noteapp.shared.data.local.DatabaseDriverFactory
import com.noteapp.shared.di.sharedModule
import com.noteapp.di.appModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() = application {
    startKoin {
        modules(
            module { single { DatabaseDriverFactory() } },
            sharedModule,
            appModule
        )
    }

    Window(onCloseRequest = ::exitApplication, title = "NoteApp") {
        App()
    }
}
