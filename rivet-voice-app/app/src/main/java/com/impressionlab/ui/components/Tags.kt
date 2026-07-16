package com.impressionlab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.impressionlab.data.TagStore

/** Small colored pill showing one tag on an attempt row. */
@Composable
fun TagPill(tag: String) {
    val color = TagStore.colorFor(tag)
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.16f)) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Text(
                tag,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

/** Colored dot used as a leading icon in tag filter chips. */
@Composable
fun TagDot(tag: String) {
    Box(Modifier.size(10.dp).background(TagStore.colorFor(tag), CircleShape))
}

/** Checkbox-style tag picker for an attempt, with inline creation of new tags. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagEditDialog(
    title: String,
    allTags: List<String>,
    initial: List<String>,
    onAddTag: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(initial) } }
    var newTag by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    allTags.forEach { tag ->
                        FilterChip(
                            selected = tag in selected,
                            onClick = {
                                if (tag in selected) selected.remove(tag) else selected.add(tag)
                            },
                            label = { Text(tag) },
                            leadingIcon = { TagDot(tag) },
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        singleLine = true,
                        placeholder = { Text("New tag") },
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        enabled = newTag.isNotBlank(),
                        onClick = {
                            val clean = newTag.trim()
                            onAddTag(clean)
                            if (selected.none { it.equals(clean, ignoreCase = true) }) selected.add(clean)
                            newTag = ""
                        },
                    ) {
                        Text("Add")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toList()) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
