package com.elsharif.dailyseventy.presentation.onboarding

import com.elsharif.dailyseventy.R

// ─────────────────────────────────────────────────────────────────────────────
// بيانات كل خطوة في الـ Onboarding
// ─────────────────────────────────────────────────────────────────────────────
data class OnboardingStep(
    val iconRes: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val descriptionRes: Int,
    val tipRes: Int
)

val onboardingSteps = listOf(

    // ── شاشة الترحيب ─────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.pray,
        titleRes = R.string.onboarding_welcome_title,
        subtitleRes = R.string.onboarding_welcome_subtitle,
        descriptionRes = R.string.onboarding_welcome_description,
        tipRes = R.string.onboarding_welcome_tip
    ),

    // ── الأذكار ───────────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.azkaricon,
        titleRes = R.string.onboarding_azkar_title,
        subtitleRes = R.string.onboarding_azkar_subtitle,
        descriptionRes = R.string.onboarding_azkar_description,
        tipRes = R.string.onboarding_azkar_tip
    ),

    // ── التاريخ الهجري ────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.calendar,
        titleRes = R.string.onboarding_hijri_title,
        subtitleRes = R.string.onboarding_hijri_subtitle,
        descriptionRes = R.string.onboarding_hijri_description,
        tipRes = R.string.onboarding_hijri_tip
    ),

    // ── تحديد الموقع ─────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.locationselect,
        titleRes = R.string.onboarding_location_title,
        subtitleRes = R.string.onboarding_location_subtitle,
        descriptionRes = R.string.onboarding_location_description,
        tipRes = R.string.onboarding_location_tip
    ),

    // ── مواقيت الصلاة ─────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.mosque,
        titleRes = R.string.onboarding_prayer_times_title,
        subtitleRes = R.string.onboarding_prayer_times_subtitle,
        descriptionRes = R.string.onboarding_prayer_times_description,
        tipRes = R.string.onboarding_prayer_times_tip
    ),

    // ── المسبحة ───────────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.tsbehicon,
        titleRes = R.string.onboarding_tasbih_title,
        subtitleRes = R.string.onboarding_tasbih_subtitle,
        descriptionRes = R.string.onboarding_tasbih_description,
        tipRes = R.string.onboarding_tasbih_tip
    ),

    // ── القبلة ────────────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.mecca,
        titleRes = R.string.onboarding_qibla_title,
        subtitleRes = R.string.onboarding_qibla_subtitle,
        descriptionRes = R.string.onboarding_qibla_description,
        tipRes = R.string.onboarding_qibla_tip
    ),

    // ── شجرة الأجر ───────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.tree,
        titleRes = R.string.onboarding_tree_title,
        subtitleRes = R.string.onboarding_tree_subtitle,
        descriptionRes = R.string.onboarding_tree_description,
        tipRes = R.string.onboarding_tree_tip
    ),

    // ── دليل السفر ───────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.travel,
        titleRes = R.string.onboarding_travel_title,
        subtitleRes = R.string.onboarding_travel_subtitle,
        descriptionRes = R.string.onboarding_travel_description,
        tipRes = R.string.onboarding_travel_tip
    ),

    // ── غراس الجنة ───────────────────────────────────────────────────────────
    OnboardingStep(
        iconRes = R.drawable.garden,
        titleRes = R.string.onboarding_garden_title,
        subtitleRes = R.string.onboarding_garden_subtitle,
        descriptionRes = R.string.onboarding_garden_description,
        tipRes = R.string.onboarding_garden_tip
    )
)