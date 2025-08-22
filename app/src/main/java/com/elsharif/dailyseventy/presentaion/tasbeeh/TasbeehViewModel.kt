package com.elsharif.dailyseventy.presentaion.tasbeeh

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.usecase.GetTasbeehCountUseCase
import com.example.core.usecase.IncreaseTasbeehUseCase
import com.example.core.usecase.ResetTasbeehUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasbeehViewModel @Inject constructor(
    private val increaseTasbeehUseCase: IncreaseTasbeehUseCase,
    private val resetTasbeehUseCase: ResetTasbeehUseCase,
    private val getTasbeehCountUseCase: GetTasbeehCountUseCase
) : ViewModel() {

    fun resetTasbeeh() = viewModelScope.launch { resetTasbeehUseCase().collect {} }

    fun increaseTasbeeh() = viewModelScope.launch { increaseTasbeehUseCase().collect {} }

    fun getTasbeehCount(): Flow<Int> =
        getTasbeehCountUseCase().onEach { Log.d("TasbeehViewModel", "Count: $it") }
}
