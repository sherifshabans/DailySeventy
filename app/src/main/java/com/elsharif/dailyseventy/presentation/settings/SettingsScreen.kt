package com.elsharif.dailyseventy.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PermDataSetting
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.IslamicReminderPreferences
import com.elsharif.dailyseventy.presentation.colorselection.ColorPickerDialog
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.presentation.friday.FridayReminderDialog
import com.elsharif.dailyseventy.presentation.islamicReminders.ReminderSettingsDialog
import com.elsharif.dailyseventy.presentation.islamicReminders.ZekrReminderDialog
import com.elsharif.dailyseventy.presentation.language.LanguageSelectionDialog
import com.elsharif.dailyseventy.presentation.language.LanguageViewModel
import com.elsharif.dailyseventy.presentation.permissins.PermissionsGuideDialog
import com.elsharif.dailyseventy.presentation.prayertimes.AzanSoundSelectorDialog
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentation.sensor.StepAlarmSettingsDialog
import com.elsharif.dailyseventy.presentation.sensor.StepAlarmViewModel
import com.elsharif.dailyseventy.presentation.thirdofthenight.NightThirdDialog
import com.elsharif.dailyseventy.ui.theme.ThemeViewModel
import com.elsharif.dailyseventy.util.Screen
import kotlinx.coroutines.delay

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    context: Context,
    prayerTimeViewModel: PrayerTimeViewModel,
    stepAlarmViewModel: StepAlarmViewModel,
    languageViewModel: LanguageViewModel = hiltViewModel(),
) {

    var showNightThirdDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showAzanDialog by remember { mutableStateOf(false) }
    var showFridayDialog by remember { mutableStateOf(false) }
    var showStepAlarmDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var showZekrDialog by remember { mutableStateOf(false) }

    // إنشاء الـ preferences مرة واحدة فقط
    val preferences = remember { IslamicReminderPreferences(context) }

    // Language Dialog States
    val showLanguageDialog: Boolean by languageViewModel.showLanguageDialog.collectAsStateWithLifecycle()
    val currentLanguage by languageViewModel.currentLanguage.collectAsStateWithLifecycle()
    val shouldRecreateActivity: Boolean by languageViewModel.shouldRecreateActivity.collectAsStateWithLifecycle()
    val isChangingLanguage: Boolean by languageViewModel.isChangingLanguage.collectAsStateWithLifecycle()

    // Handle activity recreation after language change with delay
    LaunchedEffect(shouldRecreateActivity) {
        if (shouldRecreateActivity) {
            // Add a small delay to ensure preference is saved
            delay(200)

            // Recreate the activity to apply language changes immediately
            (context as? ComponentActivity)?.recreate()
            languageViewModel.acknowledgeActivityRecreation()
        }
    }


    // Show Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                languageViewModel.changeLanguage(language)
            },
            onDismiss = {
                languageViewModel.hideLanguageSelectionDialog()
            },
            isChangingLanguage = isChangingLanguage
        )
    }

    Scaffold(
        topBar = {
            DashboardScreenTopBar(Screen.Settings.titleRes, navController)
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsItem(
                    title = stringResource(R.string.languageSettings),
                    icon = Icons.Default.Language,
                    subtitle = "${stringResource(R.string.current_language)}: ${currentLanguage.displayName}"
                ) {
                    languageViewModel.showLanguageSelectionDialog()
                }
            }

            item {
                SettingsItem(stringResource(R.string.colorpickerSettings), Icons.Default.ColorLens) {
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
                SettingsItem(stringResource(R.string.prayerSettings), Icons.Default.Mosque) {
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
                SettingsItem(stringResource(R.string.thirdsTimeSettings), Icons.Default.Notifications) {
                    showNightThirdDialog = true
                }
                if (showNightThirdDialog) {
                    NightThirdDialog(
                        onDismiss = { showNightThirdDialog = false }
                    )
                }
            }
            item {
                SettingsItem(stringResource(R.string.zekr_reminder_title), Icons.Default.Notifications){
                    showZekrDialog = true
                }
                if (showZekrDialog) {
                    ZekrReminderDialog(
                        context = context,
                        onDismiss = { showZekrDialog = false }
                    )
                }
            }
            item {
                SettingsItem(stringResource(R.string.fraidaySettings), Icons.Default.Notifications) {
                    showFridayDialog = true
                }
                if (showFridayDialog) {
                    FridayReminderDialog(
                        context = LocalContext.current,
                        prayerTimeViewModel = prayerTimeViewModel
                    ) {
                        showFridayDialog = false
                    }
                }
            }

            item {
                SettingsItem(stringResource(R.string.FastSettings), Icons.Default.Notifications) {
                showSettingsDialog = true
                }
                // نقل الـ Dialog خارج الـ LazyColumn
                if (showSettingsDialog) {
                    ReminderSettingsDialog(
                        context,
                        showDialog = showSettingsDialog,
                        onDismiss = { showSettingsDialog = false },
                        preferences = preferences
                    )
                }

            }

            item {
                SettingsItem(stringResource(R.string.fajralarmSettings), Icons.Default.AccessAlarm) {
                    showStepAlarmDialog = true
                }
                if (showStepAlarmDialog) {
                    StepAlarmSettingsDialog(stepAlarmViewModel) {
                        showStepAlarmDialog = false
                    }
                }
            }
            item {
                SettingsItem(stringResource(R.string.appPermissionsInstructions), Icons.Default.PermDataSetting) {
                    showPermissionsDialog =true
                }

                PermissionsGuideDialog(
                    context = context,
                    showDialog = showPermissionsDialog,
                    onDismiss = { showPermissionsDialog = false }
                )

            }
            item {
                SettingsItem(stringResource(R.string.feedback_title), Icons.Default.Feedback) {

                    navController.navigate(Screen.FeedbackScreen.route)
                }
            }
            item {
                SettingsItem(stringResource(R.string.privacy_policy), Icons.Default.PrivacyTip) {

                    navController.navigate(Screen.PrivacyPolicyScreen.route)
                }
            }


        }

    }


}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    onClick: () -> Unit
) {
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

                // Title and subtitle column
                androidx.compose.foundation.layout.Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                    subtitle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
            }
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )

        }
    }
}