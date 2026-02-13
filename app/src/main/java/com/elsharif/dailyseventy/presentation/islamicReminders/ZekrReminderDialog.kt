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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.ZekrPrefs
import com.elsharif.dailyseventy.domain.zekr.ZekrAlarmManager

@Composable
fun ZekrReminderDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    var enabled by remember { mutableStateOf(ZekrPrefs.isEnabled(context)) }
    var intervalText by remember { mutableStateOf(ZekrPrefs.getInterval(context).toString()) }

    val intervalOptions = listOf(15, 30, 45, 60, 90, 120, 180)
    var selectedInterval by remember { mutableStateOf(ZekrPrefs.getInterval(context)) }
    var showCustomInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // ✨ Header with gradient and icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icon with circle background
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.zekr_reminder_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    // Enable/Disable Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.enable_reminder),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = enabled,
                                onCheckedChange = { enabled = it }
                            )
                        }
                    }

                    // Animated interval selection
                    AnimatedVisibility(
                        visible = enabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.repeat_every),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(8.dp))

                            // ✨ Scrollable interval options
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(vertical = 8.dp)
                                ) {
                                    intervalOptions.forEach { interval ->
                                        Surface(
                                            onClick = {
                                                selectedInterval = interval
                                                showCustomInput = false
                                            },
                                            color = if (selectedInterval == interval && !showCustomInput)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp)
                                            ) {
                                                RadioButton(
                                                    selected = selectedInterval == interval && !showCustomInput,
                                                    onClick = null
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    text = when (interval) {
                                                        60 -> stringResource(R.string.one_hour)
                                                        90 -> stringResource(R.string.one_and_half_hour)
                                                        120 -> stringResource(R.string.two_hours)
                                                        180 -> stringResource(R.string.three_hours)
                                                        else -> stringResource(R.string.minutes, interval)
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (selectedInterval == interval && !showCustomInput)
                                                        FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    // Custom option
                                    Surface(
                                        onClick = { showCustomInput = true },
                                        color = if (showCustomInput)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            RadioButton(
                                                selected = showCustomInput,
                                                onClick = null
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                text = stringResource(R.string.custom_time),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (showCustomInput)
                                                    FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            // Custom input field
                            AnimatedVisibility(
                                visible = showCustomInput,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column {
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = intervalText,
                                        onValueChange = {
                                            if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                                intervalText = it
                                            }
                                        },
                                        label = { Text(stringResource(R.string.interval_minutes)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    Text(
                                        text = stringResource(R.string.min_interval_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                stringResource(R.string.exit),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = {
                                val finalInterval = if (showCustomInput) {
                                    intervalText.toIntOrNull()?.coerceAtLeast(15) ?: 15
                                } else {
                                    selectedInterval
                                }

                                ZekrPrefs.save(context, enabled, finalInterval)

                                if (enabled) {
                                    // ✅ استخدام AlarmManager للدقة العالية
                                    ZekrAlarmManager.scheduleNext(context)
                                } else {
                                    // ✅ إلغاء المنبه
                                    ZekrAlarmManager.cancel(context)
                                }

                                onDismiss()
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                stringResource(R.string.save),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}