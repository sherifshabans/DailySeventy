package com.elsharif.dailyseventy.presentation.permissins

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.Permissions.openAppSettings
import com.elsharif.dailyseventy.util.Permissions.openNotificationSettings
import com.elsharif.dailyseventy.util.Permissions.openOverlaySettings

@Composable
fun PermissionsGuideDialog(
    context: Context,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.general_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // قسم الإشعارات
                    item {
                        PermissionSection(
                            title = stringResource(R.string.notifications_title),
                            subtitle = stringResource(R.string.notifications_subtitle),
                            steps = listOf(
                                stringResource(R.string.notifications_step1),
                                stringResource(R.string.notifications_step2),
                                stringResource(R.string.notifications_step3)
                            ),
                            icon = Icons.Default.Notifications,
                            onOpenSettings = {
                                openNotificationSettings(context)
                            }
                        )
                    }

                    // قسم الظهور فوق التطبيقات
                    item {
                        PermissionSection(
                            title = stringResource(R.string.overlay_title),
                            subtitle = stringResource(R.string.overlay_subtitle),
                            steps = listOf(
                                stringResource(R.string.overlay_step1),
                                stringResource(R.string.overlay_step2),
                                stringResource(R.string.overlay_step3)
                            ),
                            icon = Icons.Default.OpenInNew,
                            onOpenSettings = {
                                openOverlaySettings(context)
                            }
                        )
                    }

                    // قسم الحساسات (الضوء والحركة)
                    item {
                        PermissionSection(
                            title = stringResource(R.string.sensors_title),
                            subtitle = stringResource(R.string.sensors_subtitle),
                            steps = listOf(
                                stringResource(R.string.sensors_step1),
                                stringResource(R.string.sensors_step2),
                                stringResource(R.string.sensors_step3)
                            ),
                            icon = Icons.Default.Sensors,
                            onOpenSettings = {
                                openAppSettings(context)
                            }
                        )
                    }

                    // قسم البطارية والخلفية
                    item {
                        PermissionSection(
                            title = stringResource(R.string.battery_title),
                            subtitle = stringResource(R.string.battery_subtitle),
                            steps = listOf(
                                stringResource(R.string.battery_step1),
                                stringResource(R.string.battery_step2),
                                stringResource(R.string.battery_step3)
                            ),
                            icon = Icons.Default.BatteryFull,
                            onOpenSettings = {
                                openAppSettings(context)
                            }
                        )
                    }

                    // ملاحظة نهائية
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.permissions_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.understand),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        )
    }
}

@Composable
private fun PermissionSection(
    title: String,
    subtitle: String,
    steps: List<String>,
    icon: ImageVector,
    onOpenSettings: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // العنوان والأيقونة مع زر الإعدادات
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,

                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // زر فتح الإعدادات
                if (onOpenSettings != null) {
                    OutlinedButton(
                        onClick = { onOpenSettings() },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.open_settings),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // الخطوات
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < steps.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
