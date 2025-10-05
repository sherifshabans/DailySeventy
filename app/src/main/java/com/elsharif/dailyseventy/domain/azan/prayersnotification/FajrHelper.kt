package com.elsharif.dailyseventy.domain.azan.prayersnotification


/**
 * التشيك على نوع الصلاة
 */
fun isFajrPrayer(prayerName: String?): Boolean {
    return prayerName?.let {
        it.contains("فجر", ignoreCase = true) ||
                it.contains("fajr", ignoreCase = true) ||
                it.equals("FAJR", ignoreCase = true)
    } ?: false
}

fun isFajrPrayerByType(prayerType: String?): Boolean {
    return prayerType?.let {
        it.equals("FAJR", ignoreCase = true) ||
                it.equals("FAJR_MAIN", ignoreCase = true)
    } ?: false
}
