package com.elsharif.dailyseventy.domain.data.travel


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {
    // Settings
    @Query("SELECT * FROM travel_settings WHERE id = 1")
    fun getSettings(): Flow<TravelSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: TravelSettingsEntity)

    @Query("UPDATE travel_settings SET isActive = :isActive WHERE id = 1")
    suspend fun setActive(isActive: Boolean)

    @Query("UPDATE travel_settings SET qiblaDirection = :direction WHERE id = 1")
    suspend fun updateQibla(direction: Float)

    @Query("UPDATE travel_settings SET currentLatitude = :lat, currentLongitude = :lng, distanceToKaaba = :distance, qiblaDirection = :qibla WHERE id = 1")
    suspend fun updateLocation(lat: Double, lng: Double, distance: Int, qibla: Float)
    // Checklist
    @Query("SELECT * FROM travel_checklist ORDER BY `order` ASC")
    fun getChecklist(): Flow<List<TravelChecklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChecklistItem(item: TravelChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChecklist(items: List<TravelChecklistEntity>)

    @Query("UPDATE travel_checklist SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateChecklistItem(itemId: String, isChecked: Boolean)

    @Query("DELETE FROM travel_checklist")
    suspend fun clearChecklist()
}