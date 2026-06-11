package com.noteapp.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNoteClick: (String) -> Unit,
    onNewNote: () -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val notes by viewModel.notes.collectAsState()
    var noteToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("便签") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(Icons.Default.Add, contentDescription = "新建便签")
            }
        }
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(LayoutDirection.Ltr) + 12.dp,
                end = padding.calculateEndPadding(LayoutDirection.Ltr) + 12.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 80.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { noteToDelete = note.id }
                )
            }
        }
    }

    noteToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("删除便签") },
            text = { Text("确定要删除这条便签吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(id)
                    noteToDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("取消") }
            }
        )
    }
}
