package com.elsharif.dailyseventy.presentaion.home.model

import androidx.annotation.DrawableRes
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.Screen

data class CategoriesName(
    val title: String,
    @DrawableRes val iconRes: Int, // SVG in drawable
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
        iconRes = R.drawable.hijriicon,
        route = Screen.Hijri.route
    ),
    CategoriesName(
        title = "تحديد الموقع",
        iconRes = R.drawable.mosque,
        route = Screen.PrayerTimes.route
    ),
    CategoriesName(
        title = "المسبحة",
        iconRes = R.drawable.tsbehicon,
        route = Screen.Tasbeeh.route
    ),
    CategoriesName(
        title = "القبلة",
        iconRes = R.drawable.qiblaicon,
        route = Screen.Qible.route
    )

)