package com.elsharif.dailyseventy.presentation.language

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.AppPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppPreferences.SupportedLanguage,
    onLanguageSelected: (AppPreferences.SupportedLanguage) -> Unit,
    onDismiss: () -> Unit,
    isChangingLanguage: Boolean = false
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Language options
                AppPreferences.SupportedLanguage.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedLanguage == language,
                                onClick = {
                                    selectedLanguage = language
                                }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == language,
                            onClick = {
                                selectedLanguage = language
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Show restart notice if language will change
                if (selectedLanguage != currentLanguage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ℹ️",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.restart_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedLanguage != currentLanguage) {
                                onLanguageSelected(selectedLanguage)
                            } else {
                                onDismiss()
                            }
                        },
                        enabled = !isChangingLanguage
                    ) {
                        if (isChangingLanguage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }
}