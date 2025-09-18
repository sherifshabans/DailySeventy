package com.example.core.domain.prayertiming


data class DomainPrayerTiming(
    val prayer: DomainPrayer,
    val time: String,
    val date: String,
    val lat: Double,
    val lng: Double,
    val school: DomainPrayerTimingSchool,
)

