package com.noteapp.ui.screen.edit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun RichEditorToolbar(state: RichTextState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToolbarButton("B", state.currentSpanStyle.fontWeight == FontWeight.Bold) {
            state.toggleSpanStyle(state.currentSpanStyle.copy(fontWeight = FontWeight.Bold))
        }
        ToolbarButton("I", state.currentSpanStyle.fontStyle == FontStyle.Italic) {
            state.toggleSpanStyle(state.currentSpanStyle.copy(fontStyle = FontStyle.Italic))
        }
        ToolbarButton("• 列表", false) {
            state.toggleUnorderedList()
        }
        ToolbarButton("1. 列表", false) {
            state.toggleOrderedList()
        }
    }
}

@Composable
private fun ToolbarButton(label: String, active: Boolean, onClick: () -> Unit) {
    val containerColor = if (active)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
