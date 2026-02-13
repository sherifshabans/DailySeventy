package com.elsharif.dailyseventy.presentation.zekr

sealed class ZekkrEvent {
    object LoadAzkaar : ZekkrEvent()
    data class SelectCategory(val category: String?) : ZekkrEvent()
    data class SelectCard(val index: Int) : ZekkrEvent()
    data class IncreaseCount(val zekrCount: Int) : ZekkrEvent()
}
