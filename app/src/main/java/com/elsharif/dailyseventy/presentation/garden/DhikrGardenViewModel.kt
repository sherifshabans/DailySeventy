package com.elsharif.dailyseventy.presentation.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DhikrGardenViewModel @Inject constructor() : ViewModel() {

    private val _state  = MutableStateFlow(GardenGameState())
    val state: StateFlow<GardenGameState> = _state.asStateFlow()

    private val _events = Channel<GardenEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Plot pixel centers — set from UI so events carry correct coordinates
    private val plotCenters = mutableMapOf<Int, Pair<Float, Float>>()

    companion object {
        private const val COMBO_WINDOW_MS = 2800L
        private const val COMBO_MAX       = 5
        private const val BOOST_PER_TAP   = 12f
    }

    init { startGrowthEngine() }

    // ── Public: called from UI ────────────────────────────────────────────────

    fun registerPlotCenter(plotId: Int, x: Float, y: Float) {
        plotCenters[plotId] = x to y
    }

    /**
     * Main entry point called from the UI buttons.
     * Handles combo, planting/boosting, wird counting, and completion.
     */
    fun onDhikr(id: String) {
        val btn = DHIKR_BUTTONS.find { it.id == id } ?: return

        val now = System.currentTimeMillis()
        updateCombo(now)

        val st       = _state.value
        val emptyPlot = st.plots.firstOrNull { it.isEmpty }

        // ── Plant seed or boost existing plant ────────────────────────────
        val newPlots = if (emptyPlot != null) {
            st.plots.map { p ->
                if (p.id == emptyPlot.id)
                    p.copy(type = btn.plant, stage = PlantStage.SEED, progressSec = 0f)
                else p
            }
        } else {
            val candidate = st.plots.filter { !it.isEmpty && !it.isBloom }.randomOrNull()
            if (candidate != null) {
                st.plots.map { p ->
                    if (p.id == candidate.id) advanceBySeconds(p, BOOST_PER_TAP) else p
                }
            } else st.plots
        }

        // ── Update wird counter ───────────────────────────────────────────
        val cur       = st.wird.counts[id] ?: 0
        val newCounts = if (cur < btn.target) {
            st.wird.counts.toMutableMap().also { it[id] = cur + 1 }
        } else {
            st.wird.counts
        }
        val newWird = st.wird.copy(counts = newCounts)

        // ── Small planting reward ─────────────────────────────────────────
        val anwarEarned = 2 * st.combo

        _state.update { s ->
            s.copy(
                plots        = newPlots,
                wird         = newWird,
                lastDhikrMs  = now,
                totalAnwar   = s.totalAnwar   + anwarEarned,
                sessionAnwar = s.sessionAnwar + anwarEarned
            )
        }

        // Emit plant event with coordinates
        emptyPlot?.let { p ->
            val (cx, cy) = plotCenters[p.id] ?: (0f to 0f)
            viewModelScope.launch { _events.send(GardenEvent.PlantedSeed(p.id, btn.plant, cx, cy)) }
        }

        // ── Wird completion check ─────────────────────────────────────────
        if (newWird.isWirdComplete) {
            viewModelScope.launch { processWirdComplete() }
        }
    }

    /** Called when user taps directly on a plot in the canvas */
    fun tapPlot(plotId: Int) {
        val plot = _state.value.plots.find { it.id == plotId } ?: return
        when {
            plot.isBloom  -> harvestPlot(plotId)
            !plot.isEmpty -> boostPlotGrowth(plotId)
        }
    }

    // ── Private: game mechanics ───────────────────────────────────────────────

    private fun boostPlotGrowth(plotId: Int) {
        _state.update { st ->
            st.copy(plots = st.plots.map { p ->
                if (p.id == plotId && !p.isEmpty && !p.isBloom) {
                    val boosted = advanceBySeconds(p, BOOST_PER_TAP)
                    if (boosted.stage == PlantStage.BLOOMING && p.stage != PlantStage.BLOOMING) {
                        viewModelScope.launch {
                            _events.send(GardenEvent.PlantBloomed(p.id, p.type!!))
                        }
                    }
                    boosted
                } else p
            })
        }
        viewModelScope.launch { _events.send(GardenEvent.TapBoosted) }
    }

    private fun harvestPlot(plotId: Int) {
        val st   = _state.value
        val plot = st.plots.find { it.id == plotId } ?: return
        val type = plot.type ?: return

        val anwar = (type.anwarBase * st.comboMultiplier).toInt()
        val (cx, cy) = plotCenters[plotId] ?: (0f to 0f)

        _state.update { s ->
            s.copy(
                plots = s.plots.map { p ->
                    if (p.id == plotId)
                        GardenPlot(p.id, p.row, p.col, harvestCount = p.harvestCount + 1)
                    else p
                },
                totalAnwar   = s.totalAnwar   + anwar,
                sessionAnwar = s.sessionAnwar + anwar
            )
        }
        viewModelScope.launch {
            _events.send(GardenEvent.Harvested(plotId, type, anwar, st.combo, cx, cy))
        }
    }

    private fun updateCombo(now: Long) {
        val st      = _state.value
        val elapsed = now - st.lastDhikrMs
        val newCombo = if (st.lastDhikrMs > 0L && elapsed <= COMBO_WINDOW_MS)
            (st.combo + 1).coerceAtMost(COMBO_MAX)
        else 1

        if (newCombo > st.combo && newCombo > 1) {
            viewModelScope.launch { _events.send(GardenEvent.ComboUp(newCombo)) }
        }
        _state.update { it.copy(combo = newCombo) }
    }

    private suspend fun processWirdComplete() {
        val current    = _state.value.wird
        val awradCount = current.completedAwrad + 1
        val bonus      = 200 + awradCount * 75

        _state.update { st ->
            st.copy(
                // Reset all counts to 0, keep completedAwrad total
                wird         = WirdProgress(completedAwrad = awradCount),
                totalAnwar   = st.totalAnwar   + bonus,
                sessionAnwar = st.sessionAnwar + bonus,
                awradToday   = st.awradToday   + 1,
                combo        = COMBO_MAX
            )
        }
        _events.send(GardenEvent.WirdComplete(awradCount))
    }

    // ── Growth Engine ─────────────────────────────────────────────────────────

    private fun startGrowthEngine() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                tickGrowth()
            }
        }
    }

    private fun tickGrowth() {
        _state.update { st ->
            st.copy(plots = st.plots.map { p ->
                if (p.isEmpty || p.isBloom) return@map p
                val grown    = p.copy(progressSec = p.progressSec + 1f)
                val advanced = tryAdvanceStage(grown)
                if (advanced.stage == PlantStage.BLOOMING && p.stage != PlantStage.BLOOMING) {
                    viewModelScope.launch {
                        _events.send(GardenEvent.PlantBloomed(p.id, p.type!!))
                    }
                }
                advanced
            })
        }
    }

    private fun tryAdvanceStage(p: GardenPlot): GardenPlot {
        if (p.stage == PlantStage.BLOOMING || p.isEmpty) return p
        val allStages = PlantStage.values()
        val nextIdx   = p.stage.ordinal + 1
        if (nextIdx >= allStages.size) return p
        val next = allStages[nextIdx]
        return if (p.progressSec >= p.stage.growDurationSec)
            p.copy(stage = next, progressSec = 0f)
        else p
    }

    private fun advanceBySeconds(p: GardenPlot, seconds: Float): GardenPlot {
        var current = p.copy(progressSec = p.progressSec + seconds)
        repeat(PlantStage.values().size) {
            val advanced = tryAdvanceStage(current)
            if (advanced.stage == current.stage) return current
            current = advanced
        }
        return current
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}