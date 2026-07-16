package com.impressionlab.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.impressionlab.audio.AttemptRecorder
import com.impressionlab.audio.AudioPlayer
import com.impressionlab.data.TagStore
import com.impressionlab.data.VoiceRepo
import com.impressionlab.ui.components.ConfirmDialog
import com.impressionlab.ui.components.GlassCard
import com.impressionlab.ui.components.NameDialog
import com.impressionlab.ui.components.TagDot
import com.impressionlab.ui.components.TagEditDialog
import com.impressionlab.ui.components.TagPill
import com.impressionlab.ui.components.formatMs
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * The practice space for a single reference voice line:
 * play the reference, record auto-named attempts, tag what each attempt was
 * drilling, re-listen, filter by tag, and keep notes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(
    characterName: String,
    referenceName: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val referenceDir = remember(characterName, referenceName) {
        File(File(VoiceRepo.charactersRoot(context), characterName), referenceName)
    }
    val referenceAudio = remember(referenceDir) { VoiceRepo.referenceAudio(referenceDir) }

    val player = remember { AudioPlayer() }
    LaunchedEffect(player) { player.observeProgress() }

    val recorder = remember { AttemptRecorder(context) }
    DisposableEffect(Unit) {
        onDispose {
            recorder.stop()
            player.stop()
        }
    }

    var refresh by remember { mutableIntStateOf(0) }
    val attempts = remember(refresh) { VoiceRepo.attempts(referenceDir) }
    val tagMap = remember(refresh) { TagStore.readAll(referenceDir) }
    var attemptToDelete by remember { mutableStateOf<File?>(null) }
    var attemptToTag by remember { mutableStateOf<File?>(null) }

    // Tags: global list, the drill selection applied to new recordings, and the list filter.
    var globalTags by remember { mutableStateOf(TagStore.globalTags(context)) }
    val drillTags = remember { mutableStateListOf<String>() }
    var filterTag by remember { mutableStateOf<String?>(null) }
    var showNewTagDialog by remember { mutableStateOf(false) }

    val usedTags = remember(refresh, globalTags) {
        val used = tagMap.values.flatten().toSet()
        globalTags.filter { it in used } + (used - globalTags.toSet())
    }
    val visibleAttempts = if (filterTag == null) {
        attempts
    } else {
        attempts.filter { tagMap[it.name]?.contains(filterTag) == true }
    }

    var notes by remember { mutableStateOf(VoiceRepo.readNotes(referenceDir)) }
    LaunchedEffect(notes) {
        delay(400)
        withContext(Dispatchers.IO) { VoiceRepo.writeNotes(referenceDir, notes) }
    }

    fun startRecording() {
        player.stop()
        recorder.start(VoiceRepo.newAttemptFile(referenceDir))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startRecording() }

    fun toggleRecord() {
        if (recorder.isRecording) {
            val file = recorder.stop()
            if (file != null && drillTags.isNotEmpty()) {
                TagStore.setTags(referenceDir, file.name, drillTags.toList())
            }
            refresh++
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) startRecording() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(referenceName, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            RecordBar(
                recorder = recorder,
                allTags = globalTags,
                selectedTags = drillTags,
                onToggleTag = { tag ->
                    if (tag in drillTags) drillTags.remove(tag) else drillTags.add(tag)
                },
                onNewTag = { showNewTagDialog = true },
                onToggle = ::toggleRecord,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "reference") {
                ReferencePlayerCard(referenceName, referenceAudio, player)
            }
            item(key = "notes") {
                NotesCard(notes) { notes = it }
            }
            item(key = "attemptsHeader") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Your attempts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${visibleAttempts.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (usedTags.isNotEmpty()) {
                item(key = "tagFilter") {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = filterTag == null,
                            onClick = { filterTag = null },
                            label = { Text("All") },
                        )
                        usedTags.forEach { tag ->
                            FilterChip(
                                selected = filterTag == tag,
                                onClick = { filterTag = if (filterTag == tag) null else tag },
                                label = { Text(tag) },
                                leadingIcon = { TagDot(tag) },
                            )
                        }
                    }
                }
            }
            if (attempts.isEmpty()) {
                item(key = "noAttempts") {
                    Text(
                        "No attempts yet — pick what you're drilling below, hit the mic, and your take appears here automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                }
            } else if (visibleAttempts.isEmpty()) {
                item(key = "noFiltered") {
                    Text(
                        "No attempts with the “$filterTag” tag yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                }
            }
            items(visibleAttempts, key = { it.name }) { file ->
                AttemptRow(
                    file = file,
                    player = player,
                    tags = tagMap[file.name].orEmpty(),
                    onEditTags = { attemptToTag = file },
                    onDelete = { attemptToDelete = file },
                )
            }
        }
    }

    attemptToDelete?.let { file ->
        ConfirmDialog(
            title = "Delete “Attempt ${VoiceRepo.attemptNumber(file)}”?",
            body = "This recording will be gone for good.",
            onConfirm = {
                if (player.isCurrent(file)) player.stop()
                file.delete()
                TagStore.removeEntry(referenceDir, file.name)
                refresh++
                attemptToDelete = null
            },
            onDismiss = { attemptToDelete = null },
        )
    }

    attemptToTag?.let { file ->
        TagEditDialog(
            title = "Tags — Attempt ${VoiceRepo.attemptNumber(file)}",
            allTags = globalTags,
            initial = tagMap[file.name].orEmpty(),
            onAddTag = { globalTags = TagStore.addGlobalTag(context, it) },
            onDismiss = { attemptToTag = null },
            onConfirm = { tags ->
                TagStore.setTags(referenceDir, file.name, tags)
                attemptToTag = null
                refresh++
            },
        )
    }

    if (showNewTagDialog) {
        NameDialog(
            title = "New tag",
            placeholder = "e.g. Inflection",
            confirmLabel = "Add",
            onDismiss = { showNewTagDialog = false },
            onConfirm = { name ->
                globalTags = TagStore.addGlobalTag(context, name)
                showNewTagDialog = false
            },
        )
    }
}

@Composable
private fun ReferencePlayerCard(name: String, audio: File?, player: AudioPlayer) {
    val colors = MaterialTheme.colorScheme

    // Duration shown before the file has ever been played.
    val metadataDurationMs by produceState(initialValue = 0, audio) {
        value = withContext(Dispatchers.IO) {
            val path = audio?.absolutePath ?: return@withContext 0
            try {
                MediaMetadataRetriever().use { retriever ->
                    retriever.setDataSource(path)
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toIntOrNull() ?: 0
                }
            } catch (e: Exception) {
                0
            }
        }
    }

    val isCurrent = player.isCurrent(audio)
    val playing = player.isPlayingFile(audio)
    val totalMs = if (isCurrent && player.durationMs > 0) player.durationMs else metadataDurationMs

    var dragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    GlassCard {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = { audio?.let { player.toggle(it) } },
                    enabled = audio != null,
                    modifier = Modifier.size(60.dp),
                ) {
                    Icon(
                        if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playing) "Pause reference" else "Play reference",
                        modifier = Modifier.size(32.dp),
                    )
                }
                Column(Modifier.padding(start = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            "REFERENCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            if (audio == null) {
                Text(
                    "Reference audio file is missing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                Slider(
                    value = when {
                        dragging -> dragPosition
                        isCurrent -> player.positionMs.toFloat()
                        else -> 0f
                    },
                    onValueChange = {
                        if (isCurrent) {
                            dragging = true
                            dragPosition = it
                        }
                    },
                    onValueChangeFinished = {
                        if (dragging) {
                            player.seekTo(dragPosition.toInt())
                            dragging = false
                        }
                    },
                    valueRange = 0f..maxOf(1f, totalMs.toFloat()),
                    enabled = isCurrent,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        formatMs(if (dragging) dragPosition.toInt() else if (isCurrent) player.positionMs else 0),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        formatMs(totalMs),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String, onNotesChange: (String) -> Unit) {
    GlassCard {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Notes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                minLines = 3,
                placeholder = { Text("What to improve — pitch, rasp, pacing, accent…") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            )
        }
    }
}

@Composable
private fun AttemptRow(
    file: File,
    player: AudioPlayer,
    tags: List<String>,
    onEditTags: () -> Unit,
    onDelete: () -> Unit,
) {
    val playing = player.isPlayingFile(file)
    val dateLabel = remember(file) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
    }
    GlassCard {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = { player.toggle(file) }) {
                Icon(
                    if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) "Pause attempt" else "Play attempt",
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    file.nameWithoutExtension,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Text(
                    dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (tags.isNotEmpty()) {
                    Row(
                        Modifier.padding(top = 6.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tags.forEach { TagPill(it) }
                    }
                }
            }
            IconButton(onClick = onEditTags) {
                Icon(
                    Icons.Rounded.Sell,
                    contentDescription = "Edit tags",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete attempt",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RecordBar(
    recorder: AttemptRecorder,
    allTags: List<String>,
    selectedTags: List<String>,
    onToggleTag: (String) -> Unit,
    onNewTag: () -> Unit,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    // Live mic amplitude drives the button pulse while recording.
    var amplitude by remember { mutableFloatStateOf(0f) }
    var elapsedMs by remember { mutableIntStateOf(0) }
    LaunchedEffect(recorder.isRecording) {
        if (recorder.isRecording) {
            while (true) {
                amplitude = recorder.maxAmplitude() / 32767f
                elapsedMs = (System.currentTimeMillis() - recorder.startedAtMs).toInt()
                delay(90)
            }
        } else {
            amplitude = 0f
            elapsedMs = 0
        }
    }
    val pulse by animateFloatAsState(
        targetValue = if (recorder.isRecording) 1f + amplitude * 0.22f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "recordPulse",
    )
    val ripple = rememberInfiniteTransition(label = "recordRipple")
    val rippleProgress by ripple.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "rippleProgress",
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, colors.surface.copy(alpha = 0.9f))
                )
            )
            .navigationBarsPadding()
            .padding(bottom = 10.dp, top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Drill focus: tags applied automatically to the next recording.
        AnimatedVisibility(
            visible = !recorder.isRecording,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Drill:",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                allTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onToggleTag(tag) },
                        label = { Text(tag) },
                        leadingIcon = { TagDot(tag) },
                    )
                }
                AssistChip(onClick = onNewTag, label = { Text("+ New") })
            }
        }
        AnimatedVisibility(
            visible = recorder.isRecording,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Text(
                "● REC  ${formatMs(elapsedMs)}",
                style = MaterialTheme.typography.labelLarge,
                color = colors.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 8.dp)) {
            if (recorder.isRecording) {
                Box(
                    Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            val s = 1f + rippleProgress * 0.7f
                            scaleX = s
                            scaleY = s
                            alpha = (1f - rippleProgress) * 0.6f
                        }
                        .border(2.dp, colors.error, CircleShape)
                )
            }
            FilledIconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(76.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (recorder.isRecording) colors.error else colors.primary,
                    contentColor = if (recorder.isRecording) colors.onError else colors.onPrimary,
                ),
            ) {
                Icon(
                    if (recorder.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = if (recorder.isRecording) "Stop recording" else "Record attempt",
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        Text(
            when {
                recorder.isRecording -> "Tap to stop"
                selectedTags.isEmpty() -> "Tap to record an attempt"
                else -> "Next attempt tagged: ${selectedTags.joinToString(", ")}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
