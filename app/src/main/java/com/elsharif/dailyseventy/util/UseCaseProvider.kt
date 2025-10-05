package com.elsharif.dailyseventy.util

import com.example.core.usecase.GetQuranPageAyaWithTafseerUseCase
import com.example.core.usecase.GetSoraByPageNumberUseCase

object UseCaseProvider {
    lateinit var getSoraByPageNumberUseCase: GetSoraByPageNumberUseCase
    lateinit var getQuranPageAyaWithTafseerUseCase: GetQuranPageAyaWithTafseerUseCase

    fun init(
        getSora: GetSoraByPageNumberUseCase,
        getQuran: GetQuranPageAyaWithTafseerUseCase
    ) {
        getSoraByPageNumberUseCase = getSora
        getQuranPageAyaWithTafseerUseCase = getQuran
    }
}