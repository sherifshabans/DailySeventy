package com.elsharif.dailyseventy.util

sealed class Screen(val route: String) {
    object Home : Screen("الرئيسية")
    object Morning : Screen("اذكار_الصباح")
    object Hijri: Screen("التاريخ الهجري")
}
