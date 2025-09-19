package com.elsharif.dailyseventy.presentation.colorselection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.ui.theme.UserColors

@Composable
fun ColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // عنوان
                Text(
                    text = stringResource(R.string.choosecolor),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // الألوان
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserColors.forEach { colorSet ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colorSet.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(25.dp) // دوائر أكبر
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable {
                                            onColorSelected(color)
                                            onDismiss() // ✅ يقفل بعد اختيار اللون
                                        }
                                )
                            }
                        }
                    }
                }

                // زر إغلاق
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
