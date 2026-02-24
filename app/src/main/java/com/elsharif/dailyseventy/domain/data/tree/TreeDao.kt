package com.elsharif.dailyseventy.domain.data.tree

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elsharif.dailyseventy.domain.data.tree.TreeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Query("SELECT * FROM tree_progress WHERE id = 1")
    fun getProgress(): Flow<TreeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(tree: TreeEntity)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, subhanallahCount = subhanallahCount + :count WHERE id = 1")
    suspend fun addSubhanallah(points: Int, count: Int)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, alhamdulillahCount = alhamdulillahCount + :count WHERE id = 1")
    suspend fun addAlhamdulillah(points: Int, count: Int)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, allahuakbarCount = allahuakbarCount + :count WHERE id = 1")
    suspend fun addAllahuakbar(points: Int, count: Int)

    // ── الجديدة ──────────────────────────────────────────────────────────
    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, laIlahaCount = laIlahaCount + :count WHERE id = 1")
    suspend fun addLaIlaha(points: Int, count: Int)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, hawqalaCount = hawqalaCount + :count WHERE id = 1")
    suspend fun addHawqala(points: Int, count: Int)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, astaghfirCount = astaghfirCount + :count WHERE id = 1")
    suspend fun addAstaghfir(points: Int, count: Int)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, salawatCount = salawatCount + :count WHERE id = 1")
    suspend fun addSalawat(points: Int, count: Int)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, bismillahCount = bismillahCount + :count WHERE id = 1")
    suspend fun addBismillah(points: Int, count: Int)

    @Query("UPDATE tree_progress SET totalPoints = totalPoints + :points, mashallahCount = mashallahCount + :count WHERE id = 1")
    suspend fun addMashallah(points: Int, count: Int)

    @Query("DELETE FROM tree_progress")
    suspend fun clearAll()
}