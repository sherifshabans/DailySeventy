package com.elsharif.dailyseventy.domain.repository

import com.elsharif.dailyseventy.domain.data.travel.TravelChecklistEntity
import com.elsharif.dailyseventy.domain.data.travel.TravelDao
import com.elsharif.dailyseventy.domain.data.travel.TravelSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class TravelRepository @Inject constructor(
    private val travelDao: TravelDao
) {

    // ── Queries ──────────────────────────────────────────────────────────────

    fun getSettings(): Flow<TravelSettingsEntity?> = travelDao.getSettings()

    fun getChecklist(): Flow<List<TravelChecklistEntity>> = travelDao.getChecklist()

    // ── Travel Mode ───────────────────────────────────────────────────────────

    /**
     * تفعيل وضع السفر
     * - يحسب المسافة واتجاه القبلة تلقائياً
     * - يحافظ على الـ checklist لو موجودة (ما يمسحش البروجريس)
     */
    suspend fun activateTravelMode(
        destination: String,
        destinationLat: Double,
        destinationLng: Double,
        userLat: Double,
        userLng: Double
    ) {
        val distanceToKaaba = calculateDistanceToKaaba(userLat, userLng)
        val qiblaDirection  = calculateQiblaDirection(userLat, userLng)
        val timeDiff        = calculateTimeDifference(userLng, destinationLng)
        val tripDistance    = calculateDistance(userLat, userLng, destinationLat, destinationLng)

        val settings = TravelSettingsEntity(
            isActive              = true,
            destination           = destination,
            destinationLatitude   = destinationLat,
            destinationLongitude  = destinationLng,
            currentLatitude       = userLat,
            currentLongitude      = userLng,
            startTime             = System.currentTimeMillis(),
            timeDifference        = timeDiff,
            tripDistanceKm        = tripDistance,
            distanceToKaaba       = distanceToKaaba,
            qiblaDirection        = qiblaDirection
        )
        travelDao.saveSettings(settings)

        // Checklist: يضيفها فقط لو فاضية
        val existingChecklist = travelDao.getChecklist().first()
        if (existingChecklist.isEmpty()) {
            travelDao.saveChecklist(buildDefaultChecklist())
        }
    }

    suspend fun deactivateTravelMode() {
        travelDao.setActive(false)
        // لا نمسح الـ checklist - الشخص ممكن يرجع يكملها
    }

    /**
     * تحديث موقع المستخدم الحالي وإعادة حساب القبلة والمسافة
     */
    suspend fun updateUserLocation(lat: Double, lng: Double) {
        val distanceToKaaba = calculateDistanceToKaaba(lat, lng)
        val qiblaDirection  = calculateQiblaDirection(lat, lng)

        travelDao.updateLocation(
            lat             = lat,
            lng             = lng,
            distance = distanceToKaaba,
            qibla  = qiblaDirection
        )
    }

    suspend fun updateQiblaDirection(direction: Float) {
        travelDao.updateQibla(direction)
    }

    // ── Checklist ─────────────────────────────────────────────────────────────

    suspend fun updateChecklistItem(itemId: String, isChecked: Boolean) {
        travelDao.updateChecklistItem(itemId, isChecked)
    }

    suspend fun resetChecklist() {
        travelDao.clearChecklist()
        travelDao.saveChecklist(buildDefaultChecklist())
    }

    // ── Calculations ──────────────────────────────────────────────────────────

    /**
     * حساب المسافة بين نقطتين باستخدام Haversine Formula
     * الناتج بالكيلومتر
     */
    fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Int {
        val r = 6371.0 // نصف قطر الأرض (كم)
        val dLat = toRad(lat2 - lat1)
        val dLng = toRad(lng2 - lng1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(toRad(lat1)) * cos(toRad(lat2)) * sin(dLng / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toInt()
    }

    fun calculateDistanceToKaaba(lat: Double, lng: Double): Int =
        calculateDistance(lat, lng, KAABA_LAT, KAABA_LNG)

    /**
     * حساب اتجاه القبلة بالدرجات (0-360)
     * 0 = شمال، 90 = شرق، 180 = جنوب، 270 = غرب
     */
    fun calculateQiblaDirection(userLat: Double, userLng: Double): Float {
        val userLatRad  = toRad(userLat)
        val kaabaLatRad = toRad(KAABA_LAT)
        val dLng        = toRad(KAABA_LNG - userLng)

        val y = sin(dLng)
        val x = cos(userLatRad) * tan(kaabaLatRad) - sin(userLatRad) * cos(dLng)

        var qibla = Math.toDegrees(atan2(y, x))
        if (qibla < 0) qibla += 360.0

        return qibla.toFloat()
    }

    /**
     * تقدير فرق التوقيت بين موقعين بناءً على الطول الجغرافي
     * كل 15 درجة = ساعة
     */
    fun calculateTimeDifference(userLng: Double, destinationLng: Double): String {
        val diff = ((destinationLng - userLng) / 15.0).toInt()
        return when {
            diff > 0  -> "+$diff"
            diff < 0  -> "$diff"
            else      -> "0"
        }
    }
/*
    *//**
     * حساب أوقات الصلاة المقصورة بناءً على وقت معياري
     * المسافر يقصر الصلوات الرباعية (الظهر/العصر/العشاء)
     *//*
    fun getShortenedPrayers(): List<ShortenedPrayer> = listOf(
        ShortenedPrayer("الفجر",   rakaat = 2, isShortened = false, isMerged = false),
        ShortenedPrayer("الظهر",   rakaat = 2, isShortened = true,  isMerged = true),
        ShortenedPrayer("العصر",   rakaat = 2, isShortened = true,  isMerged = true),
        ShortenedPrayer("المغرب",  rakaat = 3, isShortened = false, isMerged = true),
        ShortenedPrayer("العشاء",  rakaat = 2, isShortened = true,  isMerged = true)
    )*/

    // ── Private Helpers ───────────────────────────────────────────────────────

    private fun toRad(degrees: Double): Double = Math.toRadians(degrees)

    private fun buildDefaultChecklist(): List<TravelChecklistEntity> = listOf(
        TravelChecklistEntity("cl_1", "سجادة الصلاة في الحقيبة",        isChecked = false, order = 1),
        TravelChecklistEntity("cl_2", "تحميل الخرائط بدون إنترنت",      isChecked = false, order = 2),
        TravelChecklistEntity("cl_3", "تعيين منبهات الصلاة",             isChecked = false, order = 3),
        TravelChecklistEntity("cl_4", "معرفة اتجاه القبلة في وجهتك",    isChecked = false, order = 4),
        TravelChecklistEntity("cl_5", "تطبيق الأذكار يعمل بدون نت",    isChecked = false, order = 5),
        TravelChecklistEntity("cl_6", "مصحف صغير في الحقيبة",           isChecked = false, order = 6),
        TravelChecklistEntity("cl_7", "حفظ رقم المسجد القريب من وجهتك", isChecked = false, order = 7),
        TravelChecklistEntity("cl_8", "طهارة الملابس في الحقيبة",        isChecked = false, order = 8)
    )

    companion object {
        private const val KAABA_LAT = 21.4225
        private const val KAABA_LNG = 39.8262
    }
}
/*

// ── Support Data ──────────────────────────────────────────────────────────────

data class ShortenedPrayer(
    val name       : String,
    val rakaat     : Int,     // عدد الركعات المقصورة
    val isShortened: Boolean, // صلاة رباعية تُقصر
    val isMerged   : Boolean  // يجوز جمعها
)*/
