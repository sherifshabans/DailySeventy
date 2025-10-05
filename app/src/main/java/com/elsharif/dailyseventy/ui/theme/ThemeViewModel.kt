// ThemeViewModel.kt
package com.elsharif.dailyseventy.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.domain.data.preferences.ThemePreferences
import kotlinx.coroutines.launch

class ThemeViewModel(private val prefs: ThemePreferences) : ViewModel() {
    var userColor = mutableStateOf(Color(prefs.getCachedColor()))
        private set

    init {
        viewModelScope.launch {
            prefs.userColorFlow.collect { colorInt ->
                userColor.value = Color(colorInt)
            }
        }
    }

    fun updateColor(newColor: Color) {
        userColor.value = newColor
        viewModelScope.launch {
            prefs.saveUserColor(newColor.toArgb())
        }
    }
}
