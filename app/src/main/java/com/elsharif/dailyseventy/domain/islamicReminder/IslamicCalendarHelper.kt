package com.elsharif.dailyseventy.domain.islamicReminder

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.elsharif.dailyseventy.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class IslamicCalendarHelper {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun getCurrentHijriDate(): HijrahDate {
            return HijrahDate.now(java.time.ZoneId.systemDefault())
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatHijriDate(hijriDate: HijrahDate, locale: Locale): String {
            return hijriDate.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(locale)
            )
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun isMonday(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.MONDAY

        @RequiresApi(Build.VERSION_CODES.O)
        fun isThursday(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.THURSDAY

        fun isWhiteDay(hijriDay: Int): Boolean = hijriDay in listOf(13, 14, 15)

        fun getIslamicEvents(context: Context): List<IslamicEvent> {
            return listOf(
                // الأعياد
                IslamicEvent(
                    context.getString(R.string.eid_fitr),
                    context.getString(R.string.eid_fitr),
                    10, 1, EventType.EID
                ),
                IslamicEvent(
                    context.getString(R.string.eid_adha),
                    context.getString(R.string.eid_adha),
                    12, 10, EventType.EID
                ),

                // أيام الصيام المهمة
                IslamicEvent(
                    context.getString(R.string.day_arafat),
                    context.getString(R.string.day_arafat),
                    12, 9, EventType.FASTING
                ),
                IslamicEvent(
                    context.getString(R.string.day_ashura),
                    context.getString(R.string.day_ashura),
                    1, 10, EventType.FASTING
                ),

                // مناسبات دينية
                IslamicEvent(
                    context.getString(R.string.prophet_birthday),
                    context.getString(R.string.prophet_birthday),
                    3, 12, EventType.RELIGIOUS_OCCASION
                ),
                IslamicEvent(
                    context.getString(R.string.isra_miraj),
                    context.getString(R.string.isra_miraj),
                    7, 27, EventType.RELIGIOUS_OCCASION
                ),
                IslamicEvent(
                    context.getString(R.string.laylat_qadr),
                    context.getString(R.string.laylat_qadr),
                    9, 27, EventType.RELIGIOUS_OCCASION
                ),
                IslamicEvent(
                    context.getString(R.string.begin_ramadan),
                    context.getString(R.string.begin_ramadan),
                    9, 1, EventType.RELIGIOUS_OCCASION
                )
            )
        }
    }
}
