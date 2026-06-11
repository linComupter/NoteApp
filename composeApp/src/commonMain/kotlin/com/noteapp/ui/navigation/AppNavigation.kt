package com.noteapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.noteapp.ui.screen.edit.EditScreen
import com.noteapp.ui.screen.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNoteClick = { noteId -> navController.navigate("edit/$noteId") },
                onNewNote = { navController.navigate("edit/new") }
            )
        }
        composable(
            route = "edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getString("noteId")
            val noteId = if (rawId == "new") null else rawId
            EditScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
