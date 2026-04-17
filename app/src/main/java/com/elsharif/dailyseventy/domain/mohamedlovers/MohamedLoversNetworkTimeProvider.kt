package com.elsharif.dailyseventy.domain.mohamedlovers

import android.content.Context
import com.lyft.kronos.AndroidClockFactory
import com.lyft.kronos.KronosClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MohamedLoversNetworkTimeProvider @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val kronosClock: KronosClock = AndroidClockFactory.createKronosClock(
        context = context,
        ntpHosts = listOf(
            "time.google.com",
            "0.africa.pool.ntp.org",
            "1.africa.pool.ntp.org",
        ),
    )

    private val cairoZoneId = ZoneId.of("Africa/Cairo")

    fun prime() {
        kronosClock.syncInBackground()
    }

    fun getCompetitionWindow(): MohamedLoversCompetitionWindow {
        val networkTimeMs = kronosClock.getCurrentNtpTimeMs()
        if (networkTimeMs == null) {
            prime()
            return MohamedLoversCompetitionWindow(
                message = "جارٍ مزامنة الوقت من الشبكة للتأكد أن المسابقة في يوم الجمعة.",
            )
        }

        val networkNow = Instant.ofEpochMilli(networkTimeMs).atZone(cairoZoneId)
        return MohamedLoversCompetitionWindow(
            networkNow = networkNow,
            isFridayInEgypt = networkNow.dayOfWeek.value == 5,
            roundKey = networkNow.toLocalDate().toString(),
            message = null,
        )
    }
}
