package com.elsharif.dailyseventy.presentation.prayertimes

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.prayertimes.model.AzanSound
import com.elsharif.dailyseventy.domain.data.preferences.AzanSoundPrefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzanSoundSelectorDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sounds = listOf(
        AzanSound(stringResource(R.string.azan_ali_elmola), R.raw.elmola),
        AzanSound(stringResource(R.string.azan_abo_elenin), R.raw.aboelenin),
        AzanSound(stringResource(R.string.azan_abdelbasset), R.raw.abdelbasset),
        AzanSound(stringResource(R.string.muezzin_nuaaynah), R.raw.nayna),
        AzanSound(stringResource(R.string.muezzin_saad_alghamidi), R.raw.ghamdy),
        AzanSound(stringResource(R.string.elbna), R.raw.elbna),
        AzanSound(stringResource(R.string.refat), R.raw.refat),

    )
    val fajrSounds = listOf(
        AzanSound(stringResource(R.string.azan_mosharyfajr), R.raw.mosharyfajr),
        AzanSound(stringResource(R.string.muezzin_ahmed_alnafees), R.raw.nafesfajr),
    )

    val selectedRegularSound = remember {
        mutableIntStateOf(AzanSoundPrefs.loadSelectedSound(context))
    }
    val selectedFajrSound = remember {
        mutableIntStateOf(AzanSoundPrefs.loadSelectedFajrSound(context))
    }

    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPlayingResId by remember { mutableIntStateOf(-1) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying && isActive) {
            mediaPlayer?.let { mp ->
                currentPosition = mp.currentPosition.toFloat()
                duration = mp.duration.toFloat()
            }
            delay(100)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.select_azan_sound),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.select_preferred_sound),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Custom Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabItem(
                    text = stringResource(R.string.regular_prayers),
                    icon = "🕌",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = stringResource(R.string.fajr_prayer),
                    icon = "🌅",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentSelectedSound = if (selectedTab == 0) selectedRegularSound else selectedFajrSound
            val isFajrTab = selectedTab == 1
            val soundList = if (isFajrTab) fajrSounds else sounds

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(soundList) { sound ->
                    SoundItemCard(
                        sound = sound,
                        isSelected = sound.resId == currentSelectedSound.intValue,
                        isPlaying = isPlaying && currentPlayingResId == sound.resId,
                        currentPosition = if (currentPlayingResId == sound.resId) currentPosition else 0f,
                        duration = if (currentPlayingResId == sound.resId) duration else 0f,
                        onSelect = {
                            currentSelectedSound.intValue = sound.resId
                            if (isFajrTab) {
                                AzanSoundPrefs.saveSelectedFajrSound(context, sound.resId)
                            } else {
                                AzanSoundPrefs.saveSelectedSound(context, sound.resId)
                            }
                        },
                        onPlayPause = {
                            if (currentPlayingResId == sound.resId && isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else if (currentPlayingResId == sound.resId && !isPlaying) {
                                mediaPlayer?.start()
                                isPlaying = true
                            } else {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = android.media.MediaPlayer.create(context, sound.resId)
                                mediaPlayer?.let { mp ->
                                    mp.setOnCompletionListener {
                                        isPlaying = false
                                        currentPlayingResId = -1
                                    }
                                    mp.start()
                                    isPlaying = true
                                    currentPlayingResId = sound.resId
                                    duration = mp.duration.toFloat()
                                }
                            }
                        },
                        onSeek = { position ->
                            if (currentPlayingResId == sound.resId) {
                                mediaPlayer?.seekTo(position.toInt())
                                currentPosition = position
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Close button - always visible
            Button(
                onClick = {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.close),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateDpAsState(
        targetValue = if (selected) 4.dp else 0.dp,
        label = "tab_elevation"
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0.01f),
        tonalElevation = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SoundItemCard(
    sound: AzanSound,
    isSelected: Boolean,
    isPlaying: Boolean,
    currentPosition: Float,
    duration: Float,
    onSelect: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 2.dp,
        label = "card_elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = sound.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Text(
                                text = stringResource(R.string.selected_currently),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = if (isPlaying)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (isPlaying && duration > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Slider(
                        value = currentPosition,
                        onValueChange = onSeek,
                        valueRange = 0f..duration,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition.toLong()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatTime(duration.toLong()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}