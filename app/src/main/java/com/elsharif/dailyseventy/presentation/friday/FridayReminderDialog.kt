package com.elsharif.dailyseventy.presentation.friday

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.FridayPrefs
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridayReminderBottomSheet(
    context: Context,
    prayerTimeViewModel: PrayerTimeViewModel,
    onDismiss: () -> Unit
) {
    var kahfEnabled by remember { mutableStateOf(FridayPrefs.loadKahf(context)) }
    var asrEnabled by remember { mutableStateOf(FridayPrefs.loadAsr(context)) }
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // ── Header with gradient strip ───────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🕌", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.friday_reminders),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            stringResource(R.string.fraidaySettings),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Reminder Options ─────────────────────────────────
            listOf(
                Triple(kahfEnabled, { v: Boolean -> kahfEnabled = v }, Pair(R.string.kahf_reminder, "📖")),
                Triple(asrEnabled, { v: Boolean -> asrEnabled = v }, Pair(R.string.asr_time_reminder, "🌤️"))
            ).forEach { (enabled, onChange, meta) ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (enabled)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    tonalElevation = if (enabled) 4.dp else 0.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(meta.second, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(meta.first),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = onChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ── Buttons ──────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Text(stringResource(R.string.Exit))
                }
                Button(
                    onClick = {
                        prayerTimeViewModel.scheduleFridayReminders(context, kahfEnabled, asrEnabled)
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