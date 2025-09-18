package com.elsharif.dailyseventy.domain.azan.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_times")
data class PrayerTimesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,         // اسم الصلاة (Fajr, Dhuhr..)
    val time: String,         // وقت الصلاة hh:mm a
    val date: String,         // تاريخ اليوم yyyy-MM-dd
    val lat: Double,          // خط العرض
    val lng: Double,          // خط الطول
    val schoolId: Int,        // طريقة الحساب
    val imageId: String       // مفتاح الصورة (fajr, dhuhr..)
)
