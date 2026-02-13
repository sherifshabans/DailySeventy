package com.elsharif.dailyseventy.presentation.zekr

import com.elsharif.dailyseventy.domain.data.model.Zakker


data class ZekkrState(
    val azkaar: List<Zakker> = emptyList(),
    val selectedCategory: String? = null,
    val selectedIndex: Int = 0
)
