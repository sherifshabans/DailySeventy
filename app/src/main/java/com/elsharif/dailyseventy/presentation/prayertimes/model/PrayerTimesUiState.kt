package com.elsharif.dailyseventy.presentation.prayertimes.model

sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(val prayers: List<UiPrayerTime>) : PrayerUiState()
    data class Error(val message: String? = null) : PrayerUiState()
}
