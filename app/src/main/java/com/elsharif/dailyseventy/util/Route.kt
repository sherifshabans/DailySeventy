package com.elsharif.dailyseventy.util

import androidx.annotation.StringRes
import com.elsharif.dailyseventy.R

sealed class Screen(val route: String, @StringRes val titleRes: Int) {

    object Azkar : Screen("azkar", R.string.screen_azkar)
    object HomeScreen : Screen("home", R.string.screen_home)
    object PrayerTimes : Screen("prayer_times", R.string.location_icon)
    object MonthlyPrayerTimes : Screen("monthly_prayer_times", R.string.screen_monthly_prayer_times)
    object Hijri: Screen("hijri", R.string.screen_hijri)
    object Qible: Screen("qible", R.string.screen_qible)
    object Settings: Screen("settings", R.string.screen_settings)
    object Tasbeeh: Screen("tasbeeh", R.string.screen_tasbeeh)
    object TasbeehImages : Screen("tasbeeh_images", R.string.screen_tasbeeh_images)
    object TasbeehList : Screen("tasbeeh_list", R.string.screen_tasbeeh_list)
    object TasbeehCustom : Screen("tasbeeh_custom", R.string.screen_tasbeeh_custom)
    object ColorPicker: Screen("color_picker", R.string.screen_color_picker)
    object NightThirdRoute: Screen("night_third", R.string.screen_night_third)
    object AalarmRoute: Screen("step_alarm", R.string.screen_alarm)

    object AnimatedQibla: Screen("animated_qibla", R.string.screen_alarm)
    object ComingSoon: Screen("coming_soon", R.string.is_coming_soon)
    object HolyQuran: Screen("holy_quran", R.string.holy_quran)

    object FeedbackScreen: Screen("feedback_title", R.string.feedback_title)

    object PrivacyPolicyScreen: Screen("privacy_policy", R.string.privacy_policy)


    object  TreeScreenRoute :Screen("tree_screen", R.string.tree_of_good_deeds)


    object  TravelScreenRoute :Screen("travel_screen", R.string.travel_mode)


    object GardenScreenRoute :Screen("garden_screen", R.string.rawdat_al_dhakireen)

    object MohamedLoversRoute : Screen("mohamed_lovers", R.string.screen_mohamed_lovers)


}
