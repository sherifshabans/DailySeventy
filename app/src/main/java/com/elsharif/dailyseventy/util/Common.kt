package com.elsharif.dailyseventy.util

import android.annotation.SuppressLint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.LocaleListCompat
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import java.util.Locale

/**
 * Adds rounded corners for the current view.
 *
 * On S+ it uses [GlanceModifier.cornerRadius]
 * on <S it creates [ShapeDrawable] and sets background
 *
 * @param cornerRadius [Int] radius set to all corners of the view.
 * @param color [Int] value of a color that will be set as background
 * @param backgroundAlpha [Float] value of an alpha that will be set to background color - defaults to 1f
 */
@SuppressLint("RestrictedApi")
fun GlanceModifier.cornerRadiusCompat(
    cornerRadius: Int,
    color: Color = Color(0xFFCDDDFF),
    @FloatRange(from = 0.0, to = 1.0) backgroundAlpha: Float = 0.5f,
): GlanceModifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // For Android 12+ use native corner radius and background support
        this.background(color.copy(alpha = backgroundAlpha))
            .cornerRadius(cornerRadius.dp)
    } else {
        // For older Android versions, create a rounded ShapeDrawable
        val radii = FloatArray(8) { cornerRadius.toFloat() }
        val shape = ShapeDrawable(RoundRectShape(radii, null, null)).apply {
            paint.color = ColorUtils.setAlphaComponent(color.toArgb(), (255 * backgroundAlpha).toInt())
        }
        val bitmap = shape.toBitmap(width = 150, height = 75)
        this.background(BitmapImageProvider(bitmap))
    }
}

fun setCurrentLanguage(language: String) {
    val locales = LocaleListCompat.forLanguageTags(language)
    AppCompatDelegate.setApplicationLocales(locales)
}

fun getCurrentLanguage(): Locale? {
    return AppCompatDelegate.getApplicationLocales()[0]
}

fun isArabic(): Boolean = getCurrentLanguage() == Locale.forLanguageTag("ar")


//MAlmahdy
/*
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

*/


fun Color.adjustBrightness(factor: Float): Color {
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun getAdaptiveGradient(baseColor: Color): Brush {
    // simple luminance check
    val luminance = 0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue
    val endColor = if (luminance < 0.5f) {
        // dark → make lighter
        baseColor.adjustBrightness(1.4f)
    } else {
        // light → make darker
        baseColor.adjustBrightness(0.7f)
    }
    return Brush.horizontalGradient(listOf(baseColor, endColor))
}
