package com.elsharif.dailyseventy.util

sealed class  Screen(val route: String) {

    object Azkar : Screen("الأذكار")

    object HomeScreen : Screen("الصفحة الرئيسية")
    object PrayerTimes : Screen("مواقيت الصلاة")

    object MonthlyPrayerTimes : Screen("مواقيت الصلاة الشهرية")
    object Hijri: Screen("التاريخ الهجري")
    object Qible: Screen("القبلة")

    object Settings: Screen("الإعدادات العامة")

    object Tasbeeh: Screen("تسبيح")

    object TasbeehImages : Screen("تسبيح بعد الصلاة")

    object TasbeehList : Screen("تسبيح من اختيارك")

    object TasbeehCustom : Screen("تسبيح مخصص")

    object ColorPicker: Screen("سمات البرنامج")

    object NightThirdRoute: Screen("إعدادات حساب الوقت")

    object AalarmRoute: Screen("منبه القيام/الفجر")


}
