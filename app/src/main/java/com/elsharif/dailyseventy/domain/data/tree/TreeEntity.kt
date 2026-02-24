package com.elsharif.dailyseventy.domain.data.tree

import androidx.room.Entity
import androidx.room.PrimaryKey

// File: domain/data/tree/model/TreeEntity.kt
@Entity(tableName = "tree_progress")
data class TreeEntity(
    @PrimaryKey val id: Int = 1,
    val totalPoints: Int = 0,
    val subhanallahCount: Int = 0,
    val alhamdulillahCount: Int = 0,
    val allahuakbarCount: Int = 0,
    // ── الأذكار الجديدة ──────────────────────────
    val laIlahaCount: Int = 0,        // لا إله إلا الله
    val hawqalaCount: Int = 0,        // لا حول ولا قوة إلا بالله
    val astaghfirCount: Int = 0,      // أستغفر الله
    val salawatCount: Int = 0,        // الصلاة على النبي
    val bismillahCount: Int = 0,      // بسم الله
    val mashallahCount: Int = 0       // ما شاء الله
)