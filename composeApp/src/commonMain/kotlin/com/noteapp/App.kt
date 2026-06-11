package com.noteapp

import androidx.compose.runtime.Composable
import com.noteapp.ui.navigation.AppNavigation
import com.noteapp.ui.theme.AppTheme
import com.noteapp.ui.theme.ThemeMode

@Composable
fun App(themeMode: ThemeMode = ThemeMode.COLORFUL) {
    AppTheme(mode = themeMode) {
        AppNavigation()
    }
}
