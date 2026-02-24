package com.elsharif.dailyseventy.presentation.zekr

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.domain.repository.ZekrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZekkrViewModel @Inject constructor(
    private val zekrRepository: ZekrRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SELECTED_INDEX = "selected_card_index"
        private const val KEY_COUNTER = "zekr_counter"
    }

    private val _state = MutableStateFlow(
        ZekkrState(
            selectedIndex = savedStateHandle[KEY_SELECTED_INDEX] ?: 0
        )
    )
    val state: StateFlow<ZekkrState> = _state.asStateFlow()

    private val _count = MutableStateFlow(
        savedStateHandle[KEY_COUNTER] ?: 0
    )
    val count: StateFlow<Int> = _count.asStateFlow()

    // ✅ FIX: شلنا loadAzkaar() من init —
    // كانت بتعمل collect على StateFlow وبتحدّث الـ state بـ ALL azkar
    // وبعدين selectCategory بتجيب filtered — فكانت بتحصل 2 updates متعارضين
    // دلوقتي الـ ViewModel بس بيشتغل لما selectCategory يتبعت

    fun onEvent(event: ZekkrEvent) {
        when (event) {
            is ZekkrEvent.SelectCategory -> selectCategory(event.category)

            is ZekkrEvent.SelectCard -> {
                savedStateHandle[KEY_SELECTED_INDEX] = event.index
                _state.update { it.copy(selectedIndex = event.index) }
            }

            is ZekkrEvent.IncreaseCount -> increaseCount(event.zekrCount)

            // ✅ LoadAzkaar مش محتاجينه دلوقتي — selectCategory بتعمل نفس الشغل
            ZekkrEvent.LoadAzkaar -> { /* no-op */ }
        }
    }

    private fun selectCategory(category: String?) {
        if (category.isNullOrEmpty()) {
            _state.update { it.copy(selectedCategory = null, azkaar = emptyList()) }
            return
        }

        // ✅ نحدّث الـ state إن الداتا بتتحمل (لو عايز تضيف loading indicator)
        viewModelScope.launch {
            // getZekrByCategory دلوقتي بتستخدم cache — تاني مرة فورية
            val filteredAzkaar = zekrRepository.getZekrByCategory(category)
            _state.update {
                it.copy(
                    selectedCategory = category,
                    azkaar = filteredAzkaar
                )
            }
        }
    }

    private fun increaseCount(zekrCount: Int) {
        _count.update {
            val newValue = it + zekrCount
            savedStateHandle[KEY_COUNTER] = newValue
            newValue
        }
    }
}