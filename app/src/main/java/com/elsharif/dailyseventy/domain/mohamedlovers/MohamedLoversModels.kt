package com.elsharif.dailyseventy.domain.mohamedlovers

import java.time.ZonedDateTime

data class MohamedLoversPlayer(
    val uid: String = "",
    val alias: String = "",
    val totalCount: Int = 0,
    val isWinner: Boolean = false,
    val winnerCode: String = "",
    val countryCode: String = "",
    val updatedAt: Long = 0L,
)

data class MohamedLoversPendingSession(
    val roundKey: String? = null,
    val clickCount: Int = 0,
)

data class MohamedLoversCompetitionWindow(
    val networkNow: ZonedDateTime? = null,
    val isFridayInEgypt: Boolean = false,
    val roundKey: String? = null,
    val message: String? = null,
)

data class MohamedLoversBootstrap(
    val alias: String,
    val firebaseConfigured: Boolean,
    val competitionWindow: MohamedLoversCompetitionWindow,
    val pendingSession: MohamedLoversPendingSession,
)
