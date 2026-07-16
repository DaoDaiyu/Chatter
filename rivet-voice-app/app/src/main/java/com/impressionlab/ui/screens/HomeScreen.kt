package com.impressionlab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.impressionlab.data.VoiceRepo
import com.impressionlab.ui.components.ConfirmDialog
import com.impressionlab.ui.components.EmptyState
import com.impressionlab.ui.components.GlassCard
import com.impressionlab.ui.components.NameDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenCharacter: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val characters = remember(refresh) { VoiceRepo.characters(context) }
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<File?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Impression Lab", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Character") },
            )
        },
    ) { padding ->
        if (characters.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.RecordVoiceOver,
                title = "No characters yet",
                body = "Create a folder for a character — like Rivet — then add reference voice lines to practice against.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(characters, key = { it.name }) { dir ->
                    CharacterCard(
                        dir = dir,
                        onClick = { onOpenCharacter(dir.name) },
                        onLongClick = { toDelete = dir },
                    )
                }
            }
        }
    }

    if (showAdd) {
        NameDialog(
            title = "New character",
            placeholder = "e.g. Rivet",
            onDismiss = { showAdd = false },
            onConfirm = { name ->
                VoiceRepo.createCharacter(context, name)
                refresh++
                showAdd = false
            },
        )
    }

    toDelete?.let { dir ->
        ConfirmDialog(
            title = "Delete “${dir.name}”?",
            body = "This deletes all of this character's voice lines, attempts and notes.",
            onConfirm = {
                dir.deleteRecursively()
                refresh++
                toDelete = null
            },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun CharacterCard(dir: File, onClick: () -> Unit, onLongClick: () -> Unit) {
    val lineCount = remember(dir) { VoiceRepo.references(dir).size }
    val colors = MaterialTheme.colorScheme
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Box(
            Modifier
                .padding(top = 20.dp, start = 20.dp)
                .size(52.dp)
                .background(
                    Brush.linearGradient(listOf(colors.primary, colors.tertiary)),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Folder,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            dir.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 20.dp, top = 14.dp, end = 20.dp),
            maxLines = 2,
        )
        Text(
            if (lineCount == 1) "1 voice line" else "$lineCount voice lines",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 20.dp),
        )
    }
}
