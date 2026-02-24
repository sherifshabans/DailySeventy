package com.elsharif.dailyseventy.presentation.garden

import androidx.compose.ui.graphics.Color
import kotlin.random.Random


// ══════════════════════════════════════════════════════════════════════════════
//  GROWTH STAGES
// ══════════════════════════════════════════════════════════════════════════════

enum class PlantStage(val label: String, val growDurationSec: Float) {
    EMPTY   ("فارغة",   0f),
    SEED    ("بذرة",   20f),
    SPROUT  ("شتلة",   28f),
    YOUNG   ("ناشئة",  35f),
    MATURE  ("ناضجة",  40f),
    BLOOMING("مزهرة",   0f)  // ready to harvest — no auto-advance
}

// ══════════════════════════════════════════════════════════════════════════════
//  GARDEN PLOT
// ══════════════════════════════════════════════════════════════════════════════

data class GardenPlot(
    val id            : Int,
    val row           : Int,
    val col           : Int,
    val type          : PlantType? = null,
    val stage         : PlantStage = PlantStage.EMPTY,
    val progressSec   : Float      = 0f,
    val harvestCount  : Int        = 0
) {
    val isEmpty  get() = type == null || stage == PlantStage.EMPTY
    val isBloom  get() = stage == PlantStage.BLOOMING
    val stageProgress: Float get() {
        val d = stage.growDurationSec
        return if (d <= 0f) 1f else (progressSec / d).coerceIn(0f, 1f)
    }
}


// ══════════════════════════════════════════════════════════════════════════════
//  FULL GAME STATE
// ══════════════════════════════════════════════════════════════════════════════

data class GardenGameState(
    val plots        : List<GardenPlot> = List(12) { i -> GardenPlot(i, i / 4, i % 4) },
    val wird         : WirdProgress     = WirdProgress(),
    val totalAnwar   : Int              = 0,
    val sessionAnwar : Int              = 0,
    val combo        : Int              = 1,
    val lastDhikrMs  : Long             = 0L,
    val awradToday   : Int              = 0,
    val dayStreak    : Int              = 1
) {
    val comboMultiplier: Float get() = when (combo) {
        1 -> 1.0f; 2 -> 1.5f; 3 -> 2.0f; 4 -> 3.0f; else -> 5.0f
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GAME EVENTS  (ViewModel → UI)
// ══════════════════════════════════════════════════════════════════════════════

sealed class GardenEvent {
    data class PlantedSeed (val plotId: Int, val type: PlantType, val x: Float, val y: Float) : GardenEvent()
    data class PlantBloomed(val plotId: Int, val type: PlantType)                              : GardenEvent()
    data class Harvested   (val plotId: Int, val type: PlantType, val anwar: Int, val combo: Int, val x: Float, val y: Float) : GardenEvent()
    data class WirdComplete(val awradTotal: Int)                                               : GardenEvent()
    data class ComboUp     (val level: Int)                                                    : GardenEvent()
    object     TapBoosted                                                                       : GardenEvent()
}

// ══════════════════════════════════════════════════════════════════════════════
//  PARTICLE  (purely visual, managed in UI layer)
// ══════════════════════════════════════════════════════════════════════════════

enum class PType { PETAL, STAR, ARABIC, LIGHT, SEED, SPARKLE }

data class FloatingParticle(
    val id       : Long   = System.nanoTime() + Random.nextLong(0, 99999),
    val originX  : Float,
    val originY  : Float,
    val text     : String? = null,
    val color    : Color,
    val size     : Float  = 7f,
    var life     : Float  = 1f,
    val vx       : Float  = Random.nextFloat() * 2.6f - 1.3f,
    val vy       : Float  = -(Random.nextFloat() * 2.2f + 1.0f),
    val rotSpeed : Float  = Random.nextFloat() * 4f - 2f,
    var rot      : Float  = Random.nextFloat() * 360f,
    val type     : PType  = PType.PETAL,
    val decay    : Float  = 0.016f
) {
    val x get() = originX + vx * (1f - life) * 80f
    val y get() = originY + vy * (1f - life) * 80f + (1f - life) * (1f - life) * 35f
    val alpha: Float get() = (life * 1.5f).coerceIn(0f, 1f)
}

// ══════════════════════════════════════════════════════════════════════════════
//  PLANT POSITION HELPER
// ══════════════════════════════════════════════════════════════════════════════

data class PlotRect(val left: Float, val top: Float, val width: Float, val height: Float) {
    val cx get() = left + width  / 2f
    val cy get() = top  + height / 2f
}

fun plotRect(row: Int, col: Int, W: Float, H: Float): PlotRect {
    // الحديقة تبدأ من 54% — تحت الجدار مباشرة
    val gardenTop    = H * 0.54f
    val gardenBottom = H * 0.93f
    val gardenH      = gardenBottom - gardenTop

    // 4 أعمدة بمسافات متساوية
    val gapX  = W * 0.024f
    val plotW = (W - gapX * 5f) / 4f

    // كل صف مختلف الارتفاع — perspective: الأمامي أطول (أقرب = أكبر)
    val rowH0 = gardenH * 0.27f   // الصف الخلفي  (row 0)
    val rowH1 = gardenH * 0.31f   // الصف الأوسط  (row 1)
    val rowH2 = gardenH * 0.36f   // الصف الأمامي (row 2)
    val totalRowH = rowH0 + rowH1 + rowH2
    val gapY = (gardenH - totalRowH) / 4f

    val ri   = row.coerceIn(0, 2)
    val left = gapX + col * (plotW + gapX)
    val top  = gardenTop + gapY + when (ri) {
        0    -> 0f
        1    -> rowH0 + gapY
        else -> rowH0 + rowH1 + gapY * 2f
    }
    val rowHeights = floatArrayOf(rowH0, rowH1, rowH2)
    return PlotRect(left, top, plotW, rowHeights[ri])
}


// ══════════════════════════════════════════════════════════════════════════════
//  1. PLANT TYPES — 8 نباتات
// ══════════════════════════════════════════════════════════════════════════════

enum class PlantType(
    val arabicName : String,
    val emoji      : String,
    val stemColor  : Color,
    val petalColor : Color,
    val glowColor  : Color,
    val anwarBase  : Int       // ← renamed from anwar to anwarBase for harvest calc
) {
    JASMINE(
        "ياسمين",     "🌸",
        Color(0xFF2A5828), Color(0xFFF5F0E8), Color(0xFFE8F5E0), anwarBase = 10
    ),
    SUNFLOWER(
        "عباد الشمس", "🌻",
        Color(0xFF2A5010), Color(0xFFD49010), Color(0xFFFFF080), anwarBase = 15
    ),
    ROSE(
        "وردة",       "🌹",
        Color(0xFF1E4818), Color(0xFFAA1830), Color(0xFFFF8090), anwarBase = 20
    ),
    LOTUS(
        "لوتس",       "🪷",
        Color(0xFF1A4A38), Color(0xFFE070A0), Color(0xFFFFB0D0), anwarBase = 12
    ),
    LAVENDER(
        "خزامى",      "💜",
        Color(0xFF2A3050), Color(0xFF9070D0), Color(0xFFD0B8F8), anwarBase = 10
    ),
    MINT(
        "نعناع",      "🌿",
        Color(0xFF1A4828), Color(0xFF40C870), Color(0xFF90F0B0), anwarBase = 8
    ),
    TULIP(
        "زنبق",       "🌷",
        Color(0xFF204818), Color(0xFFE04040), Color(0xFFFF9090), anwarBase = 18
    ),
    CHAMOMILE(
        "بابونج",     "🌼",
        Color(0xFF2A5018), Color(0xFFF8E060), Color(0xFFFFF4A0), anwarBase = 14
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  2. DHIKR BUTTONS
// ══════════════════════════════════════════════════════════════════════════════

data class DhikrButton(
    val id      : String,
    val arabic  : String,
    val fullText: String,
    val target  : Int,
    val plant   : PlantType,
    val darkBg  : Color,
    val litBg   : Color,
    val arcColor: Color
)

val DHIKR_BUTTONS = listOf(
    DhikrButton(
        "subhan", "سبحان الله",
        "سُبْحَانَ اللَّهِ",
        target = 33, plant = PlantType.JASMINE,
        darkBg = Color(0xFF12280E), litBg = Color(0xFF1C3C16),
        arcColor = Color(0xFF3A7030)
    ),
    DhikrButton(
        "hamd", "الحمد لله",
        "الْحَمْدُ لِلَّهِ",
        target = 33, plant = PlantType.SUNFLOWER,
        darkBg = Color(0xFF2A1A04), litBg = Color(0xFF3E2A08),
        arcColor = Color(0xFFB06C10)
    ),
    DhikrButton(
        "akbar", "الله أكبر",
        "اللَّهُ أَكْبَرُ",
        target = 34, plant = PlantType.ROSE,
        darkBg = Color(0xFF220A12), litBg = Color(0xFF34101C),
        arcColor = Color(0xFF882040)
    ),
    DhikrButton(
        "istighfar", "أستغفر الله",
        "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
        target = 100, plant = PlantType.LOTUS,
        darkBg = Color(0xFF0E1828), litBg = Color(0xFF162238),
        arcColor = Color(0xFF5060B0)
    ),
    DhikrButton(
        "salah", "صلِّ على النبي",
        "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ ﷺ",
        target = 100, // ✅ تم التعديل من 10 إلى 100
        plant = PlantType.LAVENDER,
        darkBg = Color(0xFF160A28), litBg = Color(0xFF221438),
        arcColor = Color(0xFF8060C8)
    ),
    DhikrButton(
        "tahleel", "لا إله إلا الله",
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        target = 100, plant = PlantType.MINT,
        darkBg = Color(0xFF0A2010), litBg = Color(0xFF123018),
        arcColor = Color(0xFF308850)
    ),
    DhikrButton(
        "hawqala", "لا حول ولا قوة إلا بالله",
        "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
        target = 33, plant = PlantType.TULIP,
        darkBg = Color(0xFF200808), litBg = Color(0xFF300E0E),
        arcColor = Color(0xFFB03030)
    ),
    DhikrButton(
        "hasbiyallah", "حسبي الله",
        "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
        target = 7, plant = PlantType.CHAMOMILE,
        darkBg = Color(0xFF201A04), litBg = Color(0xFF302808),
        arcColor = Color(0xFFB09020)
    )
)

// ══════════════════════════════════════════════════════════════════════════════
//  3. WIRD PROGRESS — 8 عدّادات
// ══════════════════════════════════════════════════════════════════════════════

data class WirdProgress(
    val counts        : Map<String, Int> = DHIKR_BUTTONS.associate { it.id to 0 },
    val completedAwrad: Int = 0
) {
    // Convenience accessors used by the legacy drawing code (wird arc)
    val subProg  : Float get() = progressFor("subhan")
    val hamdProg : Float get() = progressFor("hamd")
    val akbarProg: Float get() = progressFor("akbar")

    // Legacy scalar counters (used by old plantDhikr logic — kept for compatibility)
    val subhanallah  : Int get() = counts["subhan"]  ?: 0
    val alhamdulillah: Int get() = counts["hamd"]    ?: 0
    val allahuakbar  : Int get() = counts["akbar"]   ?: 0

    fun progressFor(id: String): Float {
        val btn = DHIKR_BUTTONS.find { it.id == id } ?: return 0f
        return (counts[id] ?: 0).toFloat() / btn.target
    }

    val isWirdComplete: Boolean get() =
        DHIKR_BUTTONS.all { (counts[it.id] ?: 0) >= it.target }

    // Alias kept for any remaining references
    val isDone: Boolean get() = isWirdComplete
}