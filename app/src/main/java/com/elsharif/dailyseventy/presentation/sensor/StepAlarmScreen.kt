package com.elsharif.dailyseventy.presentation.sensor

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.domain.data.sharedpreferences.AlarmPreferences
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepAlarmScreen(viewModel: StepAlarmViewModel = hiltViewModel(), navController: NavController) {
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }

    // Initialize alarm when screen opens
    LaunchedEffect(Unit) {
        viewModel.initializeAlarm(context)
        if (viewModel.currentAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
            viewModel.resetStepCountOnScreenOpen()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.unbindService(context) }
    }

    Scaffold(
        topBar = {
            DashboardScreenTopBar(Screen.AalarmRoute.route, navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Current alarm settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "الإعدادات الحالية",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = AlarmPreferences.getAlarmInfo(context),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Use ViewModel values directly
            val isActive = viewModel.isAlarmActive
            val stepsTaken = viewModel.stepsTaken
            val targetSteps = viewModel.targetSteps
            val isCompleted = viewModel.isAlarmCompleted
            val currentAlarmType = viewModel.currentAlarmType
            val isDark = viewModel.isDark

            if (isActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "منبه نشط",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val alarmTitle = when (currentAlarmType) {
                            AlarmPreferences.ALARM_TYPE_MOVEMENT -> "🚨 منبه الحركة يعمل الآن!"
                            AlarmPreferences.ALARM_TYPE_LIGHT -> "🚨 منبه الإضاءة يعمل الآن!"
                            else -> "🚨 المنبه يعمل الآن!"
                        }

                        Text(
                            text = alarmTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // عرض المحتوى حسب نوع المنبه
                        when (currentAlarmType) {
                            AlarmPreferences.ALARM_TYPE_MOVEMENT -> {
                                Log.d("StepAlarmScreen", "Steps taken: $stepsTaken")

                                Text(
                                    text = "$stepsTaken / $targetSteps",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "خطوة",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                val progress = if (targetSteps > 0) {
                                    (stepsTaken.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
                                } else 0f

                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = if (progress >= 1f) Color.Green else MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (isCompleted) {
                                    Text(
                                        text = "🎉 تم الوصول للهدف!",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Green
                                    )
                                    Text(
                                        text = "المنبه متوقف، أحسنت!",
                                        fontSize = 16.sp,
                                        color = Color.Green
                                    )
                                } else {
                                    Text(
                                        text = "🎵 الموسيقى تعمل...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "امش ${targetSteps - stepsTaken} خطوة إضافية لإيقاف المنبه",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "⚠️ لا يمكن إيقاف المنبه إلا بإنجاز الهدف",
                                        fontSize = 12.sp,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            AlarmPreferences.ALARM_TYPE_LIGHT -> {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "منبه الإضاءة",
                                    tint = if (isDark) Color.Gray else Color.Yellow,
                                    modifier = Modifier.size(64.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if (isDark) "🌙 الجو مظلم" else "☀️ هناك ضوء",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.Gray else Color.Red
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (isCompleted) {
                                    Text(
                                        text = "🎉 تم اكتشاف الضوء!",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Green
                                    )
                                    Text(
                                        text = "المنبه متوقف، أحسنت!",
                                        fontSize = 16.sp,
                                        color = Color.Green
                                    )
                                } else {
                                    Text(
                                        text = "🎵 الموسيقى تعمل...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "اقترب من مصدر ضوء لإيقاف المنبه",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "⚠️ لا يمكن إيقاف المنبه إلا بالتعرض للضوء",
                                        fontSize = 12.sp,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val statusText = when (currentAlarmType) {
                            AlarmPreferences.ALARM_TYPE_MOVEMENT -> "منبه الحركة غير نشط حاليًا"
                            AlarmPreferences.ALARM_TYPE_LIGHT -> "منبه الإضاءة غير نشط حاليًا"
                            else -> "المنبه غير نشط حاليًا"
                        }

                        Text(
                            text = statusText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (currentAlarmType) {
                            AlarmPreferences.ALARM_TYPE_MOVEMENT -> {
                                Text(
                                    text = "الخطوات: $stepsTaken / $targetSteps",
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "خطوة",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (AlarmPreferences.isAlarmEnabled(context)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "المنبه مفعل وسيعمل في الوقت المحدد",
                                        fontSize = 14.sp,
                                        color = Color.Green
                                    )
                                }
                            }

                            AlarmPreferences.ALARM_TYPE_LIGHT -> {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "منبه الإضاءة",
                                    tint = if (isDark) Color.Gray else Color.Yellow,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (isDark) "🌙 الجو مظلم حاليًا" else "☀️ هناك ضوء حاليًا",
                                    fontSize = 18.sp,
                                    color = if (isDark) Color.Gray else Color.Red,
                                    fontWeight = FontWeight.Medium
                                )

                                if (AlarmPreferences.isAlarmEnabled(context)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isDark) "المنبه سيعمل عند الظلام" else "المنبه مفعل ويراقب الإضاءة",
                                        fontSize = 14.sp,
                                        color = Color.Green
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Button (only show when movement alarm is not active)
            if (!isActive && currentAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
                OutlinedButton(
                    onClick = { viewModel.forceResetStepCount() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة ضبط",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعادة ضبط عداد الخطوات")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "إعدادات",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعدادات")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val isEnabled = AlarmPreferences.isAlarmEnabled(context)
                        when (currentAlarmType) {
                            AlarmPreferences.ALARM_TYPE_MOVEMENT -> {
                                viewModel.enableDailyAlarm(context, !isEnabled)
                            }
                            AlarmPreferences.ALARM_TYPE_LIGHT -> {
                                viewModel.enableLightAlarm(context, !isEnabled)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (AlarmPreferences.isAlarmEnabled(context))
                            Color.Red else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (AlarmPreferences.isAlarmEnabled(context))
                            "إلغاء المنبه" else "تفعيل المنبه",
                        fontSize = 14.sp
                    )
                }
            }

            if (showSettings) {
                StepAlarmSettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { showSettings = false }
                )
            }

            // ✅ كارت التنبيه يظهر فقط لو نوع المنبه حركة
            if (viewModel.currentAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ تنبيه هام",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "تأكد من أن حساسات الحركة مفعلة من إعدادات التليفون.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "خلال منبه الحركة، حاول تحريك الهاتف بشكل واضح لأن بعض الأجهزة تحتوي على مستشعر ضعيف وقد لا يلتقط الحركة بدقة.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

        }
    }
}