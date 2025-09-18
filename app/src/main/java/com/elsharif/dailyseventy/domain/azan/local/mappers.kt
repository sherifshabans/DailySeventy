package com.elsharif.dailyseventy.domain.azan.local

import com.elsharif.dailyseventy.domain.azan.local.model.PrayerTimesEntity
import com.example.core.domain.prayertiming.DomainPrayerTiming
import com.example.core.domain.prayertiming.DomainPrayerTimingSchool

fun PrayerTimesEntity.toDomain(school: DomainPrayerTimingSchool) = DomainPrayerTiming(
    prayer = com.example.core.domain.prayertiming.DomainPrayer(
        name = name,
        imageId = imageId
    ),
    time = time,
    date = date,
    lat = lat,
    lng = lng,
    school = school
)

fun DomainPrayerTiming.toEntity() = PrayerTimesEntity(
    name = prayer.name,
    time = time,
    date = date,
    lat = lat,
    lng = lng,
    schoolId = school.id,
    imageId = prayer.imageId // ← لازم نرجع نحفظ الـ imageId
)

