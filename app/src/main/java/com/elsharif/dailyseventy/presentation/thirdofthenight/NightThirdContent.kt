package com.elsharif.dailyseventy.presentation.thirdofthenight

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThird
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThirdPrefs
import com.elsharif.dailyseventy.domain.thirdnight.cancelNightThirdNotifications
import com.elsharif.dailyseventy.domain.thirdnight.scheduleNightThirdNotifications

import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NightThirdContent(
    viewModel: PrayerTimeViewModel,
    onSaved: () -> Unit
) {
    val context = LocalContext.current

    var nightThirdText by remember { mutableStateOf("...") }
    var maghrib by remember { mutableStateOf<LocalTime?>(null) }
    var fajr by remember { mutableStateOf<LocalTime?>(null) }

    var enabled by remember { mutableStateOf(NightThirdPrefs.isEnabled(context)) }
    var selection by remember {
        mutableStateOf(
            NightThirdPrefs.getSelection(context).ifEmpty {
                setOf(NightThird.THIRD)
            }
        )
    }

    // ✅ جمع مواقيت الصلاة
    LaunchedEffect(Unit) {
        viewModel.prayerTimesState.collect { state ->
            if (state is PrayerUiState.Success) {
                val prayers = state.prayers
                val mag = prayers.firstOrNull { it.name.contains("Maghrib", true) }?.time
                    ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }
                val fajrTime = prayers.firstOrNull { it.name.contains("Fajr", true) }?.time
                    ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }

                if (mag != null && fajrTime != null) {
                    maghrib = mag
                    fajr = fajrTime

                    val now = LocalTime.now()
                    nightThirdText = if (now.isAfter(fajrTime)) {
                        "النهار 🌞"
                    } else {
                        getNightThird(mag, fajrTime)
                    }
                }
            }
        }
    }

    Column {
        Text("الثلث الحالي: $nightThirdText", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(16.dp))

        // ✅ سويتش التفعيل
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("تفعيل التذكير")
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = enabled,
                onCheckedChange = { isOn ->
                    enabled = isOn
                    NightThirdPrefs.saveEnabled(context, isOn)

                    if (!isOn) {
                        // ✨ لو اتقفل السويتش → امسح أي جدولة قديمة
                        cancelNightThirdNotifications(context)
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("اختر الأثلاث:")
        Spacer(Modifier.height(8.dp))

        fun toggle(third: NightThird) {
            selection = selection.toMutableSet().also {
                if (it.contains(third)) it.remove(third) else it.add(third)
            }
        }

        listOf(
            NightThird.FIRST to "الثلث الأول",
            NightThird.SECOND to "الثلث الثاني",
            NightThird.THIRD to "الثلث الأخير"
        ).forEach { (t, label) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = selection.contains(t),
                        onValueChange = { toggle(t) }
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = selection.contains(t), onCheckedChange = null)
                Spacer(Modifier.width(8.dp))
                Text(label)
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // ✨ احفظ الاختيارات
                NightThirdPrefs.saveSelection(context, selection)

                if (enabled && maghrib != null && fajr != null) {
                    // ✅ امسح القديم قبل ما تعمل جدولة جديدة
                    cancelNightThirdNotifications(context)

                    scheduleNightThirdNotifications(
                        context = context,
                        maghrib = maghrib!!,
                        fajr = fajr!!,
                        selection = selection
                    )
                }

                onSaved()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ وتفعيل")
        }
    }
}
