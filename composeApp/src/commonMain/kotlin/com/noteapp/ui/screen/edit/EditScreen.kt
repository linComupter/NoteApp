package com.noteapp.ui.screen.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.noteapp.ui.theme.noteColorPalette
import com.noteapp.ui.screen.home.parseHexColor
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    noteId: String?,
    onBack: () -> Unit
) {
    val viewModel: EditViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val title by viewModel.title.collectAsState()
    val isWysiwyg by viewModel.isWysiwygMode.collectAsState()
    val markdownSource by viewModel.markdownSource.collectAsState()
    val initialHtml by viewModel.initialHtml.collectAsState()
    val colorHex by viewModel.colorHex.collectAsState()

    val richTextState = rememberRichTextState()

    LaunchedEffect(initialHtml) {
        if (initialHtml.isNotEmpty()) {
            richTextState.setHtml(initialHtml)
        }
    }

    LaunchedEffect(isWysiwyg) {
        if (!isWysiwyg) {
            viewModel.updateMarkdownSource(richTextState.toMarkdown())
        } else {
            richTextState.setMarkdown(markdownSource)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveNote(richTextState.toHtml())
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                title = {
                    TextField(
                        value = title,
                        onValueChange = viewModel::updateTitle,
                        placeholder = { Text("标题") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                },
                actions = {
                    TextButton(onClick = viewModel::toggleMode) {
                        Text(if (isWysiwyg) "MD源码" else "富文本")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (isWysiwyg) {
                    HorizontalDivider()
                    RichEditorToolbar(richTextState)
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    noteColorPalette.forEach { color ->
                        val hex = "#" + color.value.toString(16).uppercase().takeLast(6)
                        OutlinedButton(
                            onClick = { viewModel.updateColorHex(hex) },
                            modifier = Modifier.size(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = if (hex == colorHex)
                                ButtonDefaults.outlinedButtonBorder(true)
                            else null,
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = color)
                        ) {}
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isWysiwyg) {
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                )
            } else {
                TextField(
                    value = markdownSource,
                    onValueChange = viewModel::updateMarkdownSource,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
