package com.elsharif.dailyseventy.domain.islamicReminder

data class IslamicEvent(
    val name: String,
    val nameAr: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val type: EventType,
    val reminderDaysBefore: Int = 1,
    val isRecurring: Boolean = true
)

enum class EventType {
    EID, // العيد
    FASTING, // صيام
    RELIGIOUS_OCCASION // مناسبة دينية
}
data class FastingDay(
    val type: FastingType,
    val nameAr: String,
    val reminderMessage: String
)
enum class FastingType {
    MONDAY_THURSDAY, // الإثنين والخميس
    WHITE_DAYS, // الأيام البيض
    ARAFAT, // عرفة
    ASHURA, // عاشوراء
    RAMADAN // رمضان
}