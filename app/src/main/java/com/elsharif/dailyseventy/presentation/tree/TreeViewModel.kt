package com.elsharif.dailyseventy.presentation.tree

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.domain.repository.TreeRepository
import com.elsharif.dailyseventy.ui.theme.GreenStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreeViewModel @Inject constructor(
    private val treeRepository: TreeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TreeState())
    val state: StateFlow<TreeState> = _state.asStateFlow()

    private val _levelUpEvent = MutableSharedFlow<Int>()
    val levelUpEvent = _levelUpEvent.asSharedFlow()

    init {
        viewModelScope.launch { treeRepository.initIfEmpty() }
        observeTree()
    }

    private fun observeTree() {
        viewModelScope.launch {
            treeRepository.getTreeProgress().collect { entity ->
                entity ?: return@collect

                val totalPoints   = entity.totalPoints
                val level         = (totalPoints / 500) + 1
                val pointsInLevel = totalPoints % 500
                val progress      = pointsInLevel / 500f
                val pointsToNext  = 500 - pointsInLevel

                // ── 🍃 Leaves ─────────────────────────────────────────────
                // سبحان الله: كل 5  = ورقة
                // لا إله إلا الله: كل 4 = ورقتان (تأثير أقوى)
                // أستغفر الله: كل 5 = ورقة جديدة
                val leaves = 5 +
                        (entity.subhanallahCount / 5) +
                        (entity.laIlahaCount / 4) * 2 +
                        (entity.astaghfirCount / 5)

                // ── 🌸 Flowers ────────────────────────────────────────────
                // الحمد لله: كل 5 = زهرة
                // بسم الله: كل 8 = زهرة
                // ما شاء الله: كل 8 = زهرة
                val flowers = (entity.alhamdulillahCount / 5) +
                        (entity.bismillahCount / 8) +
                        (entity.mashallahCount / 8)

                // ── 🍎 Fruits ─────────────────────────────────────────────
                // الله أكبر: كل 10 = ثمرة
                // الصلاة على النبي: كل 5 = ثمرتان ذهبيتان
                val fruits = (entity.allahuakbarCount / 10) +
                        (entity.salawatCount / 5) * 2

                // ── 🌿 Branch Strength ────────────────────────────────────
                // الحوقلة: كل 3 = نقطة قوة → تزيد عمق الأغصان
                val branchStrength = entity.hawqalaCount / 3

                val previousLevel = _state.value.level
                if (level > previousLevel && previousLevel != 1) {
                    _levelUpEvent.emit(level)
                }

                _state.update {
                    it.copy(
                        totalPoints        = totalPoints,
                        level              = level,
                        progress           = progress,
                        pointsToNext       = pointsToNext,
                        leaves             = leaves,
                        flowers            = flowers,
                        fruits             = fruits,
                        branchStrength     = branchStrength,
                        subhanallahCount   = entity.subhanallahCount,
                        alhamdulillahCount = entity.alhamdulillahCount,
                        allahuakbarCount   = entity.allahuakbarCount,
                        laIlahaCount       = entity.laIlahaCount,
                        hawqalaCount       = entity.hawqalaCount,
                        astaghfirCount     = entity.astaghfirCount,
                        salawatCount       = entity.salawatCount,
                        bismillahCount     = entity.bismillahCount,
                        mashallahCount     = entity.mashallahCount
                    )
                }
            }
        }
    }

    fun addZikr(type: String) {
        viewModelScope.launch {
            treeRepository.addZikrPoints(type)

            val (title, points, color) = when (type) {
                "subhanallah"   -> Triple("سبحان الله",          10, Color(0xFF2E7D32))
                "alhamdulillah" -> Triple("الحمد لله",            15, Color(0xFF1565C0))
                "allahuakbar"   -> Triple("الله أكبر",            20, Color(0xFF6A1B9A))
                "lailaha"       -> Triple("لا إله إلا الله",      25, Color(0xFF880E4F))
                "hawqala"       -> Triple("لا حول ولا قوة",       15, Color(0xFF4E342E))
                "astaghfir"     -> Triple("أستغفر الله",          10, Color(0xFF1A237E))
                "salawat"       -> Triple("اللهم صل على النبي ﷺ", 20, Color(0xFFE65100))
                "bismillah"     -> Triple("سبحان الله وبحمده",              5, Color(0xFF2E7D32))
                else            -> Triple("سبحان الله العظيم",           5, Color(0xFF00695C))
            }

            _state.update { current ->
                current.copy(
                    activities = listOf(
                        TreeActivity(
                            title  = title,
                            time   = "الآن",
                            points = points,
                            icon   = Icons.Default.Spa,
                            color  = color
                        )
                    ) + current.activities.take(9)
                )
            }
        }
    }

    fun resetTree() {
        viewModelScope.launch {
            treeRepository.resetTree()
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  STATE
// ══════════════════════════════════════════════════════════════════════════════

data class TreeState(
    val totalPoints        : Int   = 0,
    val level              : Int   = 1,
    val progress           : Float = 0f,
    val pointsToNext       : Int   = 500,
    val leaves             : Int   = 5,
    val flowers            : Int   = 0,
    val fruits             : Int   = 0,
    val branchStrength     : Int   = 0,
    val subhanallahCount   : Int   = 0,
    val alhamdulillahCount : Int   = 0,
    val allahuakbarCount   : Int   = 0,
    val laIlahaCount       : Int   = 0,
    val hawqalaCount       : Int   = 0,
    val astaghfirCount     : Int   = 0,
    val salawatCount       : Int   = 0,
    val bismillahCount     : Int   = 0,
    val mashallahCount     : Int   = 0,
    val activities         : List<TreeActivity> = emptyList()
)