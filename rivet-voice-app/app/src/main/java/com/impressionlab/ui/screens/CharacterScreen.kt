package com.impressionlab.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.impressionlab.audio.AudioPlayer
import com.impressionlab.data.VoiceRepo
import com.impressionlab.ui.components.ConfirmDialog
import com.impressionlab.ui.components.EmptyState
import com.impressionlab.ui.components.GlassCard
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    characterName: String,
    onBack: () -> Unit,
    onOpenReference: (String) -> Unit,
) {
    val context = LocalContext.current
    val characterDir = remember(characterName) {
        File(VoiceRepo.charactersRoot(context), characterName)
    }
    var refresh by remember { mutableIntStateOf(0) }
    val references = remember(refresh) { VoiceRepo.references(characterDir) }
    var toDelete by remember { mutableStateOf<File?>(null) }
    var importing by remember { mutableStateOf(false) }

    val player = remember { AudioPlayer() }
    LaunchedEffect(player) { player.observeProgress() }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                importing = true
                withContext(Dispatchers.IO) { VoiceRepo.importReference(context, characterDir, uri) }
                importing = false
                refresh++
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(characterName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (!importing) picker.launch("audio/*") },
                icon = {
                    if (importing) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                    }
                },
                text = { Text(if (importing) "Importing…" else "Voice line") },
            )
        },
    ) { padding ->
        if (references.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.LibraryMusic,
                title = "No voice lines yet",
                body = "Add a reference audio file (e.g. “Rivet voice lines 1”) and it becomes a practice space with your attempts and notes.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(references, key = { it.name }) { refDir ->
                    ReferenceRow(
                        refDir = refDir,
                        player = player,
                        onClick = { onOpenReference(refDir.name) },
                        onLongClick = { toDelete = refDir },
                    )
                }
            }
        }
    }

    toDelete?.let { dir ->
        ConfirmDialog(
            title = "Delete “${dir.name}”?",
            body = "This deletes the reference audio, all your attempts and your notes for this line.",
            onConfirm = {
                player.stop()
                dir.deleteRecursively()
                refresh++
                toDelete = null
            },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun ReferenceRow(
    refDir: File,
    player: AudioPlayer,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val audio = remember(refDir) { VoiceRepo.referenceAudio(refDir) }
    val attemptCount = remember(refDir) { VoiceRepo.attempts(refDir).size }
    val playing = player.isPlayingFile(audio)

    GlassCard(onClick = onClick, onLongClick = onLongClick) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = { audio?.let { player.toggle(it) } },
                enabled = audio != null,
            ) {
                Icon(
                    if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play reference",
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    refDir.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )
                Text(
                    if (attemptCount == 1) "1 attempt" else "$attemptCount attempts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
