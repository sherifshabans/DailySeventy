package com.elsharif.dailyseventy.presentation.home.model

import androidx.annotation.StringRes
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.Screen

data class CategoriesName(
    val title: String,
    val iconRes: Int, // drawable or SVG resource
    @StringRes val routeInt: Int,
    val route: String
)

internal val listOfCategories=listOf(

    CategoriesName(
        title = "الأذكار",
        iconRes = R.drawable.azkaricon,
        routeInt = Screen.Azkar.titleRes,
        route = Screen.Azkar.route
    ),
    CategoriesName(
        title = "التاريخ الهجري",
        iconRes = R.drawable.calendar,
        routeInt = Screen.Hijri.titleRes,
        route = Screen.Hijri.route
    ),
    CategoriesName(
        title = "تحديد الموقع",
        iconRes = R.drawable.locationselect,
        routeInt = Screen.PrayerTimes.titleRes,
        route = Screen.PrayerTimes.route
    ),
    CategoriesName(
        title = "مواقيت الصلاة",
        iconRes = R.drawable.mosque,
        routeInt = Screen.MonthlyPrayerTimes.titleRes,
        route = Screen.MonthlyPrayerTimes.route
    ),
    CategoriesName(
        title = "المسبحة",
        iconRes = R.drawable.tsbehicon,
        routeInt = Screen.Tasbeeh.titleRes,
        route = Screen.Tasbeeh.route
    ),
    CategoriesName(
        title = "القبلة",
        iconRes = R.drawable.mecca,
        routeInt = Screen.Qible.titleRes,
        route = Screen.AnimatedQibla.route
    )

)