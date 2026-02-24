package com.elsharif.dailyseventy.domain.data.travel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_settings")
data class TravelSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isActive             : Boolean = false,
    val destination          : String  = "",
    val destinationLatitude  : Double  = 0.0,
    val destinationLongitude : Double  = 0.0,
    val currentLatitude      : Double  = 0.0,
    val currentLongitude     : Double  = 0.0,
    val startTime            : Long    = 0L,
    val timeDifference       : String  = "0",
    val tripDistanceKm       : Int     = 0,   // المسافة للوجهة
    val distanceToKaaba      : Int     = 0,   // المسافة للكعبة
    val qiblaDirection       : Float   = 0f
)

@Entity(tableName = "travel_checklist")
data class TravelChecklistEntity(
    @PrimaryKey val id: String,
    val title    : String,
    val isChecked: Boolean,
    val order    : Int
)