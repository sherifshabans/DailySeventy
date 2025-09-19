package com.elsharif.dailyseventy.presentation.prayertimes

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.prayertimes.model.AzanSound
import com.elsharif.dailyseventy.domain.data.sharedpreferences.AzanSoundPrefs
import com.elsharif.dailyseventy.domain.azan.prayersnotification.updateAzanChannel

@Composable
fun AzanSoundSelectorDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    val sounds = listOf(
        AzanSound(stringResource(R.string.azan_ali_elmola), R.raw.elmola),
        AzanSound(stringResource(R.string.azan_abo_elenin), R.raw.aboelenin),
        AzanSound(stringResource(R.string.azan_abdelbasset), R.raw.abdelbasset),
        AzanSound(stringResource(R.string.azan_mosharyfajr), R.raw.mosharyfajr)
    )


    val selectedSoundResId = remember {
        mutableIntStateOf(AzanSoundPrefs.loadSelectedSound(context))
    }

    // MediaPlayer حالة
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    // لو الـDialog اتقفل → نوقف الصوت
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        },
        title = {
            Text(
                text = stringResource(R.string.select_azan_sound),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                sounds.forEach { sound ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = sound.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (sound.resId == selectedSoundResId.intValue)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        RadioButton(
                            selected = sound.resId == selectedSoundResId.intValue,
                            onClick = {
                                selectedSoundResId.intValue = sound.resId
                                AzanSoundPrefs.saveSelectedSound(context, sound.resId)

                                // تحديث الصوت أثناء الاختيار
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = android.media.MediaPlayer.create(context, sound.resId)
                                mediaPlayer?.isLooping = true
                                mediaPlayer?.start()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
