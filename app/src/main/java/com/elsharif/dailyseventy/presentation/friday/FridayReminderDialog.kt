package com.elsharif.dailyseventy.presentation.friday

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elsharif.dailyseventy.domain.data.preferences.FridayPrefs
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.R

@SuppressLint("NewApi")
@Composable
fun FridayReminderDialog(
    prayerTimeViewModel: PrayerTimeViewModel,
    context: Context,
    onDismiss: () -> Unit
) {
    var kahfEnabled by remember { mutableStateOf(FridayPrefs.loadKahf(context)) }
    var asrEnabled by remember { mutableStateOf(FridayPrefs.loadAsr(context)) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Title
                Text(
                    text = stringResource(R.string.friday_reminders),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(20.dp))

                // Kahf reminder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = kahfEnabled,
                        onCheckedChange = { kahfEnabled = it }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.kahf_reminder),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Asr reminder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = asrEnabled,
                        onCheckedChange = { asrEnabled = it }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.asr_time_reminder),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(Modifier.height(28.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R .string.Exit))

                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {

                            // Schedule reminders
                            prayerTimeViewModel.scheduleFridayReminders(
                                context = context,
                                kahfEnabled = kahfEnabled,
                                asrEnabled = asrEnabled
                            )

                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
