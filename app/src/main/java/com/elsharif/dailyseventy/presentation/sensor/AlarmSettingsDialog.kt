package com.elsharif.dailyseventy.presentation.sensor

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.AlarmPreferences
import com.elsharif.dailyseventy.domain.sensordomain.AlarmMusicService
import com.elsharif.dailyseventy.domain.sensordomain.AlarmScheduler

@Composable
fun StepAlarmSettingsDialog(
    viewModel: StepAlarmViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // اقرأ القيم المخزنة في البريفيرنس
    val savedHour = AlarmPreferences.getAlarmHour(context)
    val savedMinute = AlarmPreferences.getAlarmMinute(context)
    val savedSteps = AlarmPreferences.getRequiredSteps(context)
    val savedAlarmType = AlarmPreferences.getAlarmType(context)

    // حطها في state عشان تتحكم فيها
    var tempHour by remember { mutableStateOf(savedHour) }
    var tempMinute by remember { mutableStateOf(savedMinute) }
    var tempSteps by remember { mutableStateOf(savedSteps) }
    var selectedAlarmType by remember { mutableStateOf(savedAlarmType) }

    // حالة لتتبع إذا كان الصوت شغال
    var isAlarmPlaying by remember { mutableStateOf(AlarmPreferences.isAlarmMusicPlaying(context)) }

    // تحقق من حالة الصوت عند فتح الدايلوج
    LaunchedEffect(Unit) {
        isAlarmPlaying = AlarmPreferences.isAlarmMusicPlaying(context)
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.fajralarmSettings),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            val scrollState = rememberScrollState()


            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {

                // ⏰ قسم إيقاف الصوت الإضافي - يظهر فقط عندما يكون الصوت شغال
                if (isAlarmPlaying) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "تنبيه",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(R.string.alarm_running),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Text(
                                text = stringResource(R.string.stop_alarm_hint),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Button(
                                onClick = {
                                    // إيقاف الصوت عبر السيرفيس
                                    val stopIntent = Intent(context, AlarmMusicService::class.java).apply {
                                        action = AlarmMusicService.ACTION_STOP
                                    }
                                    context.startService(stopIntent)
                                    isAlarmPlaying = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "إيقاف",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(R.string.stop_sound),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                }
                // اختيار نوع المنبه
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.typeofalarm),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // منبه الحركة
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT,
                                onClick = { selectedAlarmType = AlarmPreferences.ALARM_TYPE_MOVEMENT }
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.motionalarm),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.needssteps),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // منبه الإضاءة
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedAlarmType == AlarmPreferences.ALARM_TYPE_LIGHT,
                                onClick = { selectedAlarmType = AlarmPreferences.ALARM_TYPE_LIGHT }
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.lightalarm),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.needslight),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // إعدادات منبه الحركة (تظهر فقط إذا تم اختيار منبه الحركة)
                if (selectedAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "تنبيه",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(R.string.permission_required),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Text(
                                text = stringResource(R.string.permission_description),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                        } else {
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("Settings", "Error opening settings", e)
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.tertiary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات",
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.open_settings),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    // ضبط الوقت
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.alarmTime),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.currentTime))

                                OutlinedButton(
                                    onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                tempHour = hour
                                                tempMinute = minute
                                            },
                                            tempHour,
                                            tempMinute,
                                            true // استخدام نظام 24 ساعة
                                        ).show()
                                    }
                                ) {
                                    Text(
                                        text = "${tempHour.toString().padStart(2, '0')}:${tempMinute.toString().padStart(2, '0')}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // ضبط عدد الخطوات
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.requierSteps),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.steps))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (tempSteps > 10) tempSteps -= 10
                                        }
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "تقليل")
                                    }

                                    Text(
                                        text = "$tempSteps",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (tempSteps < 1000) tempSteps += 10
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "زيادة")
                                    }
                                }
                            }

                            // أزرار سريعة لقيم شائعة
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                val quickValues = listOf(5, 10, 20, 50)
                                quickValues.forEach { value ->
                                    FilterChip(
                                        onClick = { tempSteps = value },
                                        label = { Text(text = "$value", maxLines = 1) },
                                        selected = tempSteps == value,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }


                        }

                        // معلومات إضافية
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {


                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.notes),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                val noteText = if (selectedAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
                                    stringResource(R.string.note_movement)
                                } else {
                                    stringResource(R.string.note_light)
                                }

                                Text(
                                    text = noteText,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
                else {

                    // ضبط الوقت
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.alarmTime),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.currentTime))

                                OutlinedButton(
                                    onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                tempHour = hour
                                                tempMinute = minute
                                            },
                                            tempHour,
                                            tempMinute,
                                            true // استخدام نظام 24 ساعة
                                        ).show()
                                    }
                                ) {
                                    Text(
                                        text = "${tempHour.toString().padStart(2, '0')}:${tempMinute.toString().padStart(2, '0')}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }


                    // معلومات إضافية
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.notes),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            val noteText = if (selectedAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
                                stringResource(R.string.note_movement)
                            } else {
                                stringResource(R.string.note_light)
                            }

                            Text(
                                text = noteText,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }


            }
        },
        confirmButton = {
            Row {
                // زر إضافي لإيقاف الصوت إذا كان شغال
                if (isAlarmPlaying) {
                    OutlinedButton(
                        onClick = {
                            // إيقاف الصوت
                            val stopIntent = Intent(context, AlarmMusicService::class.java).apply {
                                action = AlarmMusicService.ACTION_STOP
                            }
                            context.startService(stopIntent)
                            isAlarmPlaying = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        stringResource(R.string.stop_sound)
                    }
                }

                Button(
                    onClick = {
                        // حفظ الإعدادات الحالية
                        AlarmPreferences.saveAlarmType(context, selectedAlarmType)
                        viewModel.switchAlarmType(context, selectedAlarmType)
                        viewModel.setAlarmTime(context, tempHour, tempMinute)

                        if (selectedAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
                            viewModel.setTargetSteps(context, tempSteps)
                            viewModel.enableDailyAlarm(context, true)
                            AlarmScheduler.scheduleStepAlarm(context)
                        } else {
                            viewModel.enableLightAlarm(context, true)
                        }

                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.Exit))
            }
        }
    )
}