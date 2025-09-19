package com.elsharif.dailyseventy.presentation.thirdofthenight

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
fun getNightThird(maghrib: LocalTime, fajr: LocalTime): NightThirdPart {
    val now = LocalTime.now()
    val nightMinutes = if (fajr.isBefore(maghrib)) {
        ChronoUnit.MINUTES.between(maghrib, LocalTime.MAX) + 1 +
                ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, fajr)
    } else {
        ChronoUnit.MINUTES.between(maghrib, fajr)
    }
    val third = nightMinutes / 3
    val sinceStart = if (now.isBefore(maghrib)) {
        ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, now) +
                ChronoUnit.MINUTES.between(maghrib, LocalTime.MAX) + 1
    } else {
        ChronoUnit.MINUTES.between(maghrib, now)
    }

    return when {
        sinceStart <= third -> NightThirdPart.FIRST
        sinceStart <= third * 2 -> NightThirdPart.SECOND
        else -> NightThirdPart.THIRD
    }
}
