package com.elsharif.dailyseventy.presentation.thirdofthenight

import android.os.Build
import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.NightThird
import com.elsharif.dailyseventy.domain.data.preferences.NightThirdPrefs
import com.elsharif.dailyseventy.domain.thirdnight.cancelNightThirdNotifications
import com.elsharif.dailyseventy.domain.thirdnight.scheduleNightThirdNotifications
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class NightThirdPart(val labelRes: Int) {
    FIRST(R.string.night_third_first),
    SECOND(R.string.night_third_second),
    THIRD(R.string.night_third_third)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NightThirdContent(
    viewModel: PrayerTimeViewModel,
    onSaved: () -> Unit
) {
    val context = LocalContext.current

    // استخدام remember للحفاظ على حالة الإعدادات داخل الـ Content
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

    // تحديث القيم عند فتح الـ Content
    LaunchedEffect(Unit) {
        enabled = NightThirdPrefs.isEnabled(context)
        selection = NightThirdPrefs.getSelection(context).ifEmpty {
            setOf(NightThird.THIRD)
        }
    }

    // دالة مساعدة بسيطة لإيجاد مواقيت الصلاة
    fun findPrayerTime(prayers: List<UiPrayerTime>, keywords: List<String>): LocalTime? {
        for (keyword in keywords) {
            val prayer = prayers.firstOrNull { it.name.contains(keyword, ignoreCase = true) }
            if (prayer != null) {
                return try {
                    // جرب تنسيقات مختلفة للوقت
                    listOf("hh:mm a", "HH:mm", "h:mm a", "H:mm").forEach { format ->
                        try {
                            return LocalTime.parse(prayer.time, DateTimeFormatter.ofPattern(format))
                        } catch (ignored: Exception) { }
                    }
                    null
                } catch (e: Exception) {
                    Log.e("NightThird", "Error parsing time: ${prayer.time}", e)
                    null
                }
            }
        }
        return null
    }

    // ✅ جمع مواقيت الصلاة بطريقة محسنة
    LaunchedEffect(viewModel) {
        viewModel.prayerTimesState.collect { state ->
            if (state is PrayerUiState.Success) {
                val prayers = state.prayers

                // البحث عن المغرب بكلمات مختلفة
                val maghribKeywords = listOf("maghrib", "مغرب", "sunset", "coucher")
                val fajrKeywords = listOf("fajr", "فجر", "dawn", "aube", "subh")

                val maghribTime = findPrayerTime(prayers, maghribKeywords)
                val fajrTime = findPrayerTime(prayers, fajrKeywords)

                Log.d("NightThird", "Found Maghrib: $maghribTime, Fajr: $fajrTime")

                if (maghribTime != null && fajrTime != null) {
                    maghrib = maghribTime
                    fajr = fajrTime

                    val now = LocalTime.now()

                    // تحسين لوجيك تحديد فترة الليل
                    nightThirdText = try {
                        // التحقق من أننا في فترة الليل
                        val isNightTime = if (maghribTime.isBefore(fajrTime)) {
                            // نفس اليوم (مغرب قبل فجر - حالة نادرة)
                            now.isAfter(maghribTime) && now.isBefore(fajrTime)
                        } else {
                            // المعتاد: مغرب اليوم إلى فجر الغد
                            now.isAfter(maghribTime) || now.isBefore(fajrTime)
                        }

                        if (isNightTime) {
                            context.getString(getNightThird(maghribTime, fajrTime).labelRes)
                        } else {
                            context.getString(R.string.afternoon) // أو نهار
                        }
                    } catch (e: Exception) {
                        Log.e("NightThird", "Error calculating night third", e)
                        context.getString(R.string.afternoon)
                    }

                    Log.d("NightThird", "Night third text: $nightThirdText")
                } else {
                    Log.w("NightThird", "Could not find prayer times")
                    nightThirdText = context.getString(R.string.afternoon)
                }
            }
        }
    }

    Column {
        Row {
            Text(
                text = stringResource(R.string.current_third),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = nightThirdText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(16.dp))

        // ✅ سويتش التفعيل
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.openzekr))
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

        Text(stringResource(R.string.choosethird))
        Spacer(Modifier.height(8.dp))

        // دالة toggle داخلية
        fun toggle(third: NightThird) {
            selection = selection.toMutableSet().also {
                if (it.contains(third)) it.remove(third) else it.add(third)
            }
        }

        listOf(NightThird.FIRST, NightThird.SECOND, NightThird.THIRD).forEach { third ->
            val label = stringResource(id = third.labelRes)
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = selection.contains(third),
                        onValueChange = { toggle(third) }
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = selection.contains(third), onCheckedChange = null)
                Spacer(Modifier.width(8.dp))
                Text(label)
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // ✅ احفظ الاختيارات مع تحديث الـ local state
                NightThirdPrefs.saveSelection(context, selection)
                NightThirdPrefs.saveEnabled(context, enabled)

                // ✅ امسح أي جدولة قديمة أولاً
                cancelNightThirdNotifications(context)

                // ✅ لو متفعل وفي اختيارات، اجدول جديد
                if (enabled && selection.isNotEmpty() && maghrib != null && fajr != null) {
                    scheduleNightThirdNotifications(
                        context = context,
                        maghrib = maghrib!!,
                        fajr = fajr!!,
                        selection = selection
                    )
                    Log.d("NightThird", "Scheduled for: $selection")
                } else {
                    Log.d("NightThird", "Not scheduling: enabled=$enabled, selection=$selection")
                }

                onSaved()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selection.isNotEmpty() // تفعيل الزر فقط لو فيه اختيارات
        ) {
            Text(stringResource(R.string.save))
        }
    }
}