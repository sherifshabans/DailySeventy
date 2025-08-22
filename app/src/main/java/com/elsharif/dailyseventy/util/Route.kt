package com.elsharif.dailyseventy.util

sealed class  Screen(val route: String) {

    object Azkar : Screen("الأذكار")

    object HomeScreen : Screen("الصفحة الرئيسية")
    object PrayerTimes : Screen("مواقيت الصلاة")
    object Hijri: Screen("التاريخ الهجري")
    object Qible: Screen("القبلة")

    object Settings: Screen("الإعدادات العامة")

    object Tasbeeh: Screen("تسبيح")

    object ColorPicker: Screen("سمات البرنامج")

}
