package com.elsharif.dailyseventy.presentation.language

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.domain.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val appPreferences = AppPreferences(context)

    private val _currentLanguage = MutableStateFlow(AppPreferences.SupportedLanguage.ARABIC)
    val currentLanguage: StateFlow<AppPreferences.SupportedLanguage> = _currentLanguage.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    // Event to trigger activity recreation with delay
    private val _shouldRecreateActivity = MutableStateFlow(false)
    val shouldRecreateActivity: StateFlow<Boolean> = _shouldRecreateActivity.asStateFlow()

    init {
        Log.d("LanguageViewModel", "ViewModel initialized")
        loadCurrentLanguage()
        observeCurrentLanguage()
    }

    private fun loadCurrentLanguage() {
        // Load current language synchronously from SharedPreferences
        val currentCode = appPreferences.getSavedLanguageCode()
        val language = AppPreferences.SupportedLanguage.values()
            .find { it.code == currentCode } ?: AppPreferences.SupportedLanguage.ARABIC
        _currentLanguage.value = language
        Log.d("LanguageViewModel", "Loaded current language: ${language.displayName}")
    }

    private fun observeCurrentLanguage() {
        viewModelScope.launch {
            appPreferences.currentLanguage.collect { language ->
                Log.d("LanguageViewModel", "Language changed to: ${language.displayName}")
                _currentLanguage.value = language
            }
        }
    }

    fun showLanguageSelectionDialog() {
        Log.d("LanguageViewModel", "Showing language dialog")
        _showLanguageDialog.value = true
    }

    fun hideLanguageSelectionDialog() {
        Log.d("LanguageViewModel", "Hiding language dialog")
        _showLanguageDialog.value = false
    }

    // Loading state for better UX
    private val _isChangingLanguage = MutableStateFlow(false)
    val isChangingLanguage: StateFlow<Boolean> = _isChangingLanguage.asStateFlow()

    private val _languageChangeRequested = MutableStateFlow(false)
    val languageChangeRequested: StateFlow<Boolean> = _languageChangeRequested.asStateFlow()

    fun changeLanguage(newLanguage: AppPreferences.SupportedLanguage) {
        Log.d("LanguageViewModel", "Changing language to: ${newLanguage.displayName}")
        if (newLanguage != _currentLanguage.value) {
            viewModelScope.launch {
                try {
                    _isChangingLanguage.value = true

                    // Close dialog first
                    _showLanguageDialog.value = false

                    // Apply language change
                    appPreferences.setLanguage(newLanguage)

                    // Update current language immediately
                    _currentLanguage.value = newLanguage
                    _languageChangeRequested.value = true

                    // Wait a bit to ensure the preference is saved
                    delay(200)

                    // Trigger activity recreation
                    _shouldRecreateActivity.value = true

                    Log.d("LanguageViewModel", "Language changed successfully")
                } catch (e: Exception) {
                    Log.e("LanguageViewModel", "Error changing language: ${e.message}")
                    _isChangingLanguage.value = false
                }
            }
        } else {
            Log.d("LanguageViewModel", "Same language selected, closing dialog")
            _showLanguageDialog.value = false
        }
    }

    fun acknowledgeLanguageChange() {
        Log.d("LanguageViewModel", "Language change acknowledged")
        _languageChangeRequested.value = false
    }

    fun acknowledgeActivityRecreation() {
        Log.d("LanguageViewModel", "Activity recreation acknowledged")
        _shouldRecreateActivity.value = false
        _isChangingLanguage.value = false
    }
}