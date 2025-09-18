package com.elsharif.dailyseventy.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentation.sensor.StepAlarmSettingsDialog
import com.elsharif.dailyseventy.presentation.sensor.StepAlarmViewModel
import com.elsharif.dailyseventy.presentation.colorselection.ColorPickerDialog
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.presentation.friday.FridayReminderDialog
import com.elsharif.dailyseventy.presentation.prayertimes.AzanSoundSelectorDialog
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentation.thirdofthenight.NightThirdDialog
import com.elsharif.dailyseventy.ui.theme.ThemeViewModel
import com.elsharif.dailyseventy.util.Screen

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    context: Context,
    prayerTimeViewModel: PrayerTimeViewModel,
    stepAlarmViewModel: StepAlarmViewModel
) {

    var showNightThirdDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showAzanDialog by remember { mutableStateOf(false) }
    var showFridayDialog by remember { mutableStateOf(false) }
    var showOverlayDialog by remember { mutableStateOf(false) }
    var showStepAlarmDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            DashboardScreenTopBar(Screen.Settings.route,navController)
        }
    ) { padding ->

        LazyColumn( // ✅ instead of Column
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsItem("إعدادات اللغة", Icons.Default.Language) {}
            }
            item {
                SettingsItem("إعدادات حساب الوقت", Icons.Default.AccessTime) {
                    showNightThirdDialog = true
                }
                if (showNightThirdDialog) {
                    NightThirdDialog(
                        onDismiss = { showNightThirdDialog = false }
                    )
                }
            }
            item {
                SettingsItem("سمات البرنامج", Icons.Default.ColorLens) {
                 //   navController.navigate(Screen.ColorPicker.route)

                    showColorDialog = true
                }
                if (showColorDialog) {
                    ColorPickerDialog(
                        onDismiss = { showColorDialog = false },
                        onColorSelected = { selectedColor ->
                            themeViewModel.updateColor(selectedColor)
                            showColorDialog = false
                        }
                    )
                }
            }
            item {
                SettingsItem("الإعدادات العامة للأذان", Icons.Default.Notifications)  {
                    showAzanDialog = true
                }
                if (showAzanDialog) {
                    AzanSoundSelectorDialog(
                        context = context,
                        onDismiss = { showAzanDialog = false },

                    )
                }
            }
            item {
                SettingsItem("إعدادات يوم الجمعة", Icons.Default.Settings) {
                    showFridayDialog = true
                }
                if (showFridayDialog) {
                    FridayReminderDialog(context = LocalContext.current, prayerTimeViewModel =prayerTimeViewModel ) {
                        showFridayDialog = false
                    }
                }
            }
            item {
                SettingsItem("خدمة النافذة عائمة", Icons.Default.Apps) {
                showOverlayDialog =true
                }

                /*OverlaySettingsDialog(
                    context = context,
                    showDialog = showOverlayDialog,
                    onDismiss = { showOverlayDialog = false }
                )*/

            }
            item {
                SettingsItem("إعدادات متتبع الصلوات والصيام", Icons.Default.Settings) {}
            }
            item {
                SettingsItem("إعدادات الشروق والنوافل", Icons.Default.Settings) {

                }
            }
            item {
                SettingsItem("إعدادات الأذكار", Icons.Default.Notifications) {}
            }
            item {
                SettingsItem("إعدادات منبه الفجر", Icons.Default.AccessAlarm) {
                    showStepAlarmDialog = true
                }
                if (showStepAlarmDialog) {
                    StepAlarmSettingsDialog(stepAlarmViewModel) {
                        showStepAlarmDialog = false
                    }
                }

            }
            item {
                SettingsItem("عن البرنامج", Icons.Default.Info) {}
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
            Icon(Icons.Default.Settings, contentDescription = "Arrow")
        }
    }
}
