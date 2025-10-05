package com.elsharif.dailyseventy.presentation.islamicReminders

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.IslamicReminderPreferences
import com.elsharif.dailyseventy.domain.islamicReminder.IslamicReminderManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderSettingsDialog(
    context: Context,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    preferences: IslamicReminderPreferences,
) {
    var isFastingReminderEnabled by remember { mutableStateOf(preferences.isFastingReminderEnabled) }
    var isMondayThursdayEnabled by remember { mutableStateOf(preferences.isMondayThursdayEnabled) }
    var isWhiteDaysEnabled by remember { mutableStateOf(preferences.isWhiteDaysEnabled) }
    var isEidReminderEnabled by remember { mutableStateOf(preferences.isEidReminderEnabled) }
    var isReligiousOccasionReminderEnabled by remember { mutableStateOf(preferences.isReligiousOccasionReminderEnabled) }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            // إعادة تعيين القيم من Preferences عند فتح الدايلوج
            isFastingReminderEnabled = preferences.isFastingReminderEnabled
            isMondayThursdayEnabled = preferences.isMondayThursdayEnabled
            isWhiteDaysEnabled = preferences.isWhiteDaysEnabled
            isEidReminderEnabled = preferences.isEidReminderEnabled
            isReligiousOccasionReminderEnabled = preferences.isReligiousOccasionReminderEnabled
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.reminder_settings),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    ReminderToggleItem(
                        title = stringResource(R.string.fasting_reminders),
                        subtitle = stringResource(R.string.fasting_reminders_subtitle),
                        checked = isFastingReminderEnabled,
                        onCheckedChange = {
                            isFastingReminderEnabled = it
                            // لا تحفظ هنا - فقط عدل الـ state
                            if (!it) {
                                isMondayThursdayEnabled = false
                                isWhiteDaysEnabled = false
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ReminderToggleItem(
                        title = stringResource(R.string.monday_thursday),
                        subtitle = stringResource(R.string.monday_thursday_subtitle),
                        checked = isMondayThursdayEnabled,
                        onCheckedChange = { isMondayThursdayEnabled = it },
                        enabled = isFastingReminderEnabled
                    )

                    ReminderToggleItem(
                        title = stringResource(R.string.white_days),
                        subtitle = stringResource(R.string.white_days_subtitle),
                        checked = isWhiteDaysEnabled,
                        onCheckedChange = { isWhiteDaysEnabled = it },
                        enabled = isFastingReminderEnabled
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ReminderToggleItem(
                        title = stringResource(R.string.eid_reminders),
                        subtitle = stringResource(R.string.eid_reminders_subtitle),
                        checked = isEidReminderEnabled,
                        onCheckedChange = { isEidReminderEnabled = it }
                    )

                    ReminderToggleItem(
                        title = stringResource(R.string.religious_occasions),
                        subtitle = stringResource(R.string.religious_occasions_subtitle),
                        checked = isReligiousOccasionReminderEnabled,
                        onCheckedChange = { isReligiousOccasionReminderEnabled = it }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // احفظ كل الإعدادات مرة واحدة عند الضغط على OK
                    preferences.saveAllSettings(
                        isFastingReminderEnabled,
                        isEidReminderEnabled,
                        isReligiousOccasionReminderEnabled,
                        isMondayThursdayEnabled,
                        isWhiteDaysEnabled
                    )

                    // ✅ هنا - أعد جدولة التذكيرات بالإعدادات الجديدة
                    IslamicReminderManager.scheduleAllReminders(context, preferences)

                    onDismiss()
                }) {
                    Text(stringResource(R.string.save)) // غير من OK إلى Save
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel)) // أضف زر إلغاء
                }
            }
        )
    }
}
@Composable
fun ReminderToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = checked && enabled,
            onCheckedChange = if (enabled) onCheckedChange else { {} },
            enabled = enabled
        )
    }
}
