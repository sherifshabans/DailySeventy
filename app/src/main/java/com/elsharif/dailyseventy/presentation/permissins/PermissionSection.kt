package com.elsharif.dailyseventy.presentation.permissins

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.Permissions.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsGuideBottomSheet(
    context: Context,
    onDismiss: () -> Unit
) {
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // ── Header ──────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.general_settings), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.appPermissionsInstructions), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ── Permission Sections ──────────────────────────────
            val sections = listOf(
                PermSection(R.string.notifications_title, R.string.notifications_subtitle,
                    listOf(R.string.notifications_step1, R.string.notifications_step2, R.string.notifications_step3),
                    Icons.Default.Notifications, "🔔") { openNotificationSettings(context) },
                PermSection(R.string.overlay_title, R.string.overlay_subtitle,
                    listOf(R.string.overlay_step1, R.string.overlay_step2, R.string.overlay_step3),
                    Icons.Default.OpenInNew, "🖼️") { openOverlaySettings(context) },
                PermSection(R.string.sensors_title, R.string.sensors_subtitle,
                    listOf(R.string.sensors_step1, R.string.sensors_step2, R.string.sensors_step3),
                    Icons.Default.Sensors, "📡") { openAppSettings(context) },
                PermSection(R.string.battery_title, R.string.battery_subtitle,
                    listOf(R.string.battery_step1, R.string.battery_step2, R.string.battery_step3),
                    Icons.Default.BatteryFull, "🔋") { openAppSettings(context) }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                sections.forEach { section ->
                    PermissionCard(section = section, context = context)
                }

                // Info note
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Text("💡", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.permissions_note),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.understand), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private data class PermSection(
    val titleRes: Int,
    val subtitleRes: Int,
    val stepsRes: List<Int>,
    val icon: ImageVector,
    val emoji: String,
    val onOpen: () -> Unit
)

@Composable
private fun PermissionCard(section: PermSection, context: Context) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(section.emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(section.titleRes), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(section.subtitleRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(
                    onClick = section.onOpen,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.open_settings), fontSize = 11.sp)
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.stepsRes.forEachIndexed { i, res ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(res), style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}