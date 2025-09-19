package com.elsharif.dailyseventy.presentation.azkarcategories

import com.elsharif.dailyseventy.R

fun getCategoryResId(category: String): Int {
    return when (category) {
        "أذكار الصباح" -> R.string.MorningZekr
        "أذكار المساء" -> R.string.NightZekr
        "أذكار بعد السلام من الصلاة المفروضة" -> R.string.AfterPrey
        "تسابيح" -> R.string.Tsabeeh
        "أذكار النوم" -> R.string.SleepingZekr
        "أذكار الاستيقاظ" -> R.string.AwakeZekr
        "أدعية قرآنية" -> R.string.QuranPrey
        "أدعية الأنبياء" -> R.string.ProphetsPrey
        "الرُّقية الشرعية من القرآن الكريم" -> R.string.Ruqyah
        "التسبيح، التحميد، التهليل، التكبير" -> R.string.TasbihAll
        "أذكار متنوعة" -> R.string.MiscZekr
        else -> R.string.app_name
    }
}
