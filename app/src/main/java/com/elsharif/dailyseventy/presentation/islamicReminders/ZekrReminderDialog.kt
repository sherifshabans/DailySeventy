package com.elsharif.dailyseventy.presentation.islamicReminders

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.ZekrPrefs
import com.elsharif.dailyseventy.domain.zekr.ZekrAlarmManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZekrReminderBottomSheet(
    context: Context,
    onDismiss: () -> Unit
) {
    var enabled by remember { mutableStateOf(ZekrPrefs.isEnabled(context)) }
    var selectedInterval by remember { mutableStateOf(ZekrPrefs.getInterval(context)) }
    var intervalText by remember { mutableStateOf(ZekrPrefs.getInterval(context).toString()) }
    var showCustomInput by remember { mutableStateOf(false) }
    val intervalOptions = listOf(15, 30, 45, 60, 90, 120, 180)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // ── Header ──────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.zekr_reminder_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.enable_reminder), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ── Interval Options ─────────────────────────────────
            AnimatedVisibility(visible = enabled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column {
                    Text(
                        stringResource(R.string.repeat_every),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        intervalOptions.forEach { interval ->
                            val isSelected = selectedInterval == interval && !showCustomInput
                            Surface(
                                onClick = { selectedInterval = interval; showCustomInput = false },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = null)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = when (interval) {
                                            60 -> stringResource(R.string.one_hour)
                                            90 -> stringResource(R.string.one_and_half_hour)
                                            120 -> stringResource(R.string.two_hours)
                                            180 -> stringResource(R.string.three_hours)
                                            else -> stringResource(R.string.minutes, interval)
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // Custom option
                        Surface(
                            onClick = { showCustomInput = true },
                            shape = RoundedCornerShape(14.dp),
                            color = if (showCustomInput) MaterialTheme.colorScheme.primaryContainer.copy(0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = showCustomInput, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(stringResource(R.string.custom_time), fontWeight = if (showCustomInput) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        AnimatedVisibility(visible = showCustomInput, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = intervalText,
                                    onValueChange = { if (it.all(Char::isDigit) && it.length <= 4) intervalText = it },
                                    label = { Text(stringResource(R.string.interval_minutes)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Text(
                                    stringResource(R.string.min_interval_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Buttons ──────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Text(stringResource(R.string.exit))
                }
                Button(
                    onClick = {
                        val finalInterval = if (showCustomInput) intervalText.toIntOrNull()?.coerceAtLeast(15) ?: 15 else selectedInterval
                        ZekrPrefs.save(context, enabled, finalInterval)
                        if (enabled) ZekrAlarmManager.scheduleNext(context) else ZekrAlarmManager.cancel(context)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}