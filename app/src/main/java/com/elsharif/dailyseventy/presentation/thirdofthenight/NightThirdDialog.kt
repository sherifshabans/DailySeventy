package com.elsharif.dailyseventy.presentation.thirdofthenight

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NightThirdDialog(
    onDismiss: () -> Unit,
    viewModel: PrayerTimeViewModel = hiltViewModel()
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.thirdsSettings),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(12.dp))

                // ✅ Your NightThirdScreen content (copy relevant parts, not navigation)
                NightThirdContent(
                    viewModel = viewModel,
                    onSaved = { onDismiss() } // close after save
                )

                Spacer(Modifier.height(16.dp))

                // Close button
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
