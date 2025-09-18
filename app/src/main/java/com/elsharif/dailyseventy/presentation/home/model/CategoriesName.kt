package com.elsharif.dailyseventy.presentation.home.model

import androidx.annotation.DrawableRes
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.Screen

data class CategoriesName(
    val title: String,
    val iconRes: Int, // drawable or SVG resource
    val route :String
)

internal val listOfCategories=listOf(

    CategoriesName(
        title = "الأذكار",
        iconRes = R.drawable.azkaricon,
        route = Screen.Azkar.route
    ),
    CategoriesName(
        title = "التاريخ الهجري",
        iconRes = R.drawable.calendar,
        route = Screen.Hijri.route
    ),
    CategoriesName(
        title = "تحديد الموقع",
        iconRes = R.drawable.locationselect,
        route = Screen.PrayerTimes.route
    ),
    CategoriesName(
        title = "مواقيت الصلاة",
        iconRes = R.drawable.mosque,
        route = Screen.MonthlyPrayerTimes.route
    ),
    CategoriesName(
        title = "المسبحة",
        iconRes = R.drawable.tsbehicon,
        route = Screen.Tasbeeh.route
    ),
    CategoriesName(
        title = "القبلة",
        iconRes = R.drawable.mecca,
        route = Screen.Qible.route
    )

)