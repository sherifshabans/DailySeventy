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

    /* ---------------- UI STATE ---------------- */

    private val _state = MutableStateFlow(
        ZekkrState(
            selectedIndex = savedStateHandle[KEY_SELECTED_INDEX] ?: 0
        )
    )
    val state: StateFlow<ZekkrState> = _state.asStateFlow()

    /* ---------------- COUNTER ---------------- */

    private val _count = MutableStateFlow(
        savedStateHandle[KEY_COUNTER] ?: 0
    )
    val count: StateFlow<Int> = _count.asStateFlow()

    init {
        loadAzkaar()
    }

    /* ---------------- EVENTS ---------------- */

    fun onEvent(event: ZekkrEvent) {
        when (event) {
            is ZekkrEvent.SelectCategory -> selectCategory(event.category)

            is ZekkrEvent.SelectCard -> {
                savedStateHandle[KEY_SELECTED_INDEX] = event.index
                _state.update { it.copy(selectedIndex = event.index) }
            }

            is ZekkrEvent.IncreaseCount -> increaseCount(event.zekrCount)

            ZekkrEvent.LoadAzkaar -> loadAzkaar()
        }
    }

    /* ---------------- DATA ---------------- */

    private fun loadAzkaar() {
        viewModelScope.launch {
            zekrRepository.azkaarList.collect { azkaar ->
                _state.update { it.copy(azkaar = azkaar) }
            }
        }
    }

    private fun selectCategory(category: String?) {
        viewModelScope.launch {
            val filteredAzkaar = if (category.isNullOrEmpty()) {
                emptyList()
            } else {
                zekrRepository.getZekrByCategory(category)
            }

            _state.update {
                it.copy(
                    selectedCategory = category,
                    azkaar = filteredAzkaar
                )
            }
        }
    }

    /* ---------------- COUNTER LOGIC ---------------- */

    private fun increaseCount(zekrCount: Int) {
        _count.update {
            val newValue = it + zekrCount
            savedStateHandle[KEY_COUNTER] = newValue
            newValue
        }
    }
}
