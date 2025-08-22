package com.elsharif.dailyseventy.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.os.LocaleListCompat
import androidx.core.widget.ImageViewCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.elsharif.dailyseventy.presentaion.widgets.PrayerWidget.PrayerTimesWidget
import com.elsharif.dailyseventy.util.emit
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import net.time4j.SystemClock
import net.time4j.calendar.HijriCalendar
import net.time4j.engine.StartOfDay
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale
import kotlin.apply
import kotlin.text.replace
import kotlin.text.toRegex



fun Calendar.getDayMonthYear(): Triple<Int, Int, Int> {
    val calendar = Calendar.getInstance()
    val year = calendar[Calendar.YEAR]
    val month = calendar[Calendar.MONTH] + 1
    val day = calendar[Calendar.DAY_OF_MONTH]
    return Triple(day, month, year)
}


fun getTodayExact(): HijriCalendar = SystemClock.inLocalView().now(
    HijriCalendar.family(), HijriCalendar.VARIANT_UMALQURA,
    StartOfDay.EVENING // simple approximation => 18:00
).toDate()

fun Context.copyToClipboard(text: CharSequence) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied from Daily Seventy", text)
    clipboard.setPrimaryClip(clip)
}

fun String.removeArabicTashkeel() = Normalizer.normalize(this, Normalizer.Form.NFKD).replace("\\p{M}".toRegex(), "")

@OptIn(ExperimentalLayoutApi::class)
fun Modifier.clearFocusOnKeyboardDismiss(): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }
    if (isFocused) {
        val imeIsVisible = WindowInsets.isImeVisible
        val focusManager = LocalFocusManager.current
        LaunchedEffect(imeIsVisible) {
            if (imeIsVisible) {
                keyboardAppearedSinceLastFocused = true
            } else if (keyboardAppearedSinceLastFocused) {
                focusManager.clearFocus()
            }
        }
    }
    onFocusEvent {
        if (isFocused != it.isFocused) {
            isFocused = it.isFocused
            if (isFocused) {
                keyboardAppearedSinceLastFocused = false
            }
        }
    }
}
context(FlowCollector<T>)
suspend fun <T> T.emit() {
    emit(this) // This will call FlowCollector<T>.emit(this)
}
private val negative = floatArrayOf(
    -1.0f, .0f, .0f, .0f, 255.0f,
    .0f, -1.0f, .0f, .0f, 255.0f,
    .0f, .0f, -1.0f, .0f, 255.0f,
    .0f, .0f, .0f, 1.0f, .0f
)

fun ColorFilter.Companion.inverse() = colorMatrix(ColorMatrix(negative).apply { setToSaturation(1f) })


suspend fun Context.updatePrayerTimesWidget() {
    val ids = GlanceAppWidgetManager(this).getGlanceIds(PrayerTimesWidget::class.java)
    ids.forEach {
        PrayerTimesWidget.update(this, it)
    }
}