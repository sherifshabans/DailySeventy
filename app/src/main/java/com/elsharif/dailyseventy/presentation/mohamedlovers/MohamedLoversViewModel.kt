package com.elsharif.dailyseventy.presentation.mohamedlovers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.domain.mohamedlovers.MohamedLoversCompetitionWindow
import com.elsharif.dailyseventy.domain.mohamedlovers.MohamedLoversPlayer
import com.elsharif.dailyseventy.domain.mohamedlovers.MohamedLoversRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MohamedLoversLeaderboardEntry(
    val rank: Int,
    val alias: String,
    val totalCount: Int,
    val isCurrentUser: Boolean,
)

data class MohamedLoversUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSavingSession: Boolean = false,
    val alias: String = "",
    val statusMessage: String = "",
    val bannerMessage: String = "الضغطات تُحتسب يوم الجمعة فقط بتوقيت القاهرة من وقت الشبكة.",
    val firebaseConfigured: Boolean = true,
    val isFridayInEgypt: Boolean = false,
    val roundKey: String? = null,
    val networkTimeLabel: String = "",
    val canCount: Boolean = false,
    val syncedTotal: Int = 0,
    val sessionClicks: Int = 0,
    val isWinner: Boolean = false,
    val winnerCode: String = "",
    val currentRank: Int? = null,
    val topFive: List<MohamedLoversLeaderboardEntry> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class MohamedLoversViewModel @Inject constructor(
    private val repository: MohamedLoversRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MohamedLoversUiState())
    val state: StateFlow<MohamedLoversUiState> = _state.asStateFlow()

    private val flushMutex = Mutex()
    private var leaderboardJob: Job? = null
    private var remotePlayers: List<MohamedLoversPlayer> = emptyList()
    private var authUid: String? = null
    private var currentWindow: MohamedLoversCompetitionWindow = MohamedLoversCompetitionWindow()

    init {
        refresh()
    }

    fun refresh() {
        repository.refreshNetworkTime()
        leaderboardJob?.cancel()
        remotePlayers = emptyList()
        authUid = null

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isRefreshing = true,
                    errorMessage = null,
                    topFive = emptyList(),
                    currentRank = null,
                    winnerCode = "",
                    syncedTotal = 0,
                )
            }

            val bootstrap = repository.bootstrap()
            currentWindow = bootstrap.competitionWindow

            val canCount = bootstrap.competitionWindow.networkNow != null &&
                bootstrap.competitionWindow.isFridayInEgypt

            _state.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    alias = bootstrap.alias,
                    firebaseConfigured = bootstrap.firebaseConfigured,
                    isFridayInEgypt = bootstrap.competitionWindow.isFridayInEgypt,
                    roundKey = bootstrap.competitionWindow.roundKey,
                    networkTimeLabel = bootstrap.competitionWindow.networkNow
                        ?.format(DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a"))
                        .orEmpty(),
                    statusMessage = buildStatusMessage(
                        firebaseConfigured = bootstrap.firebaseConfigured,
                        competitionWindow = bootstrap.competitionWindow,
                    ),
                    canCount = canCount,
                    sessionClicks = bootstrap.pendingSession.clickCount,
                    errorMessage = null,
                )
            }

            flushPendingSession()
            connectToLeaderboardIfPossible()
        }
    }

    fun onCountClick() {
        val roundKey = state.value.roundKey ?: return
        if (!state.value.canCount) {
            return
        }

        val pendingSession = repository.registerLocalTap(roundKey)
        _state.update {
            it.copy(
                sessionClicks = pendingSession.clickCount,
                errorMessage = null,
            )
        }
        applyLeaderboard()
    }

    fun flushPendingSession() {
        viewModelScope.launch {
            flushMutex.withLock {
                val pending = repository.getPendingSession()
                if (pending.clickCount <= 0) {
                    _state.update { it.copy(sessionClicks = 0, isSavingSession = false) }
                    return@withLock
                }

                if (!state.value.firebaseConfigured) {
                    _state.update { it.copy(isSavingSession = false) }
                    applyLeaderboard()
                    return@withLock
                }

                _state.update { it.copy(isSavingSession = true, errorMessage = null) }

                val result = repository.flushPendingSession(state.value.alias)
                val latestPending = repository.getPendingSession()

                _state.update {
                    it.copy(
                        isSavingSession = false,
                        sessionClicks = latestPending.clickCount,
                        errorMessage = result.exceptionOrNull()?.message,
                    )
                }

                if (result.isSuccess) {
                    connectToLeaderboardIfPossible()
                }

                applyLeaderboard()
            }
        }
    }

    private fun connectToLeaderboardIfPossible() {
        val roundKey = state.value.roundKey
        if (!state.value.firebaseConfigured || roundKey.isNullOrBlank()) {
            leaderboardJob?.cancel()
            remotePlayers = emptyList()
            applyLeaderboard()
            return
        }

        viewModelScope.launch {
            val uidResult = repository.ensureAnonymousUser()
            authUid = uidResult.getOrNull()
            if (authUid == null) {
                _state.update {
                    it.copy(
                        errorMessage = uidResult.exceptionOrNull()?.message
                            ?: "تعذر الاتصال بالمنافسة الآن.",
                    )
                }
                applyLeaderboard()
                return@launch
            }

            leaderboardJob?.cancel()
            leaderboardJob = launch {
                repository.observeLeaderboard(roundKey).collectLatest { result ->
                    result.onSuccess { players ->
                        remotePlayers = players
                        applyLeaderboard()
                    }.onFailure { throwable ->
                        _state.update { it.copy(errorMessage = throwable.message) }
                    }
                }
            }
        }
    }

    private fun applyLeaderboard() {
        val currentState = state.value
        val currentUid = authUid
        val currentRemotePlayer = currentUid?.let { uid -> remotePlayers.firstOrNull { it.uid == uid } }

        val effectivePlayers = remotePlayers.toMutableList()

        if (currentUid != null) {
            val effectiveCurrent = MohamedLoversPlayer(
                uid = currentUid,
                alias = currentRemotePlayer?.alias?.takeIf { it.isNotBlank() } ?: currentState.alias,
                totalCount = (currentRemotePlayer?.totalCount ?: 0) + currentState.sessionClicks,
                isWinner = currentRemotePlayer?.isWinner ?: false,
                winnerCode = currentRemotePlayer?.winnerCode.orEmpty(),
                countryCode = currentRemotePlayer?.countryCode.orEmpty(),
                updatedAt = currentRemotePlayer?.updatedAt ?: 0L,
            )

            val existingIndex = effectivePlayers.indexOfFirst { it.uid == currentUid }
            if (existingIndex >= 0) {
                effectivePlayers[existingIndex] = effectiveCurrent
            } else if (currentState.sessionClicks > 0) {
                effectivePlayers += effectiveCurrent
            }
        }

        val sorted = effectivePlayers.sortedWith(
            compareByDescending<MohamedLoversPlayer> { it.totalCount }
                .thenByDescending { it.updatedAt }
                .thenBy { it.alias.lowercase() },
        )

        val topFive = sorted.take(5).mapIndexed { index, player ->
            MohamedLoversLeaderboardEntry(
                rank = index + 1,
                alias = player.alias.ifBlank { "محب محمد" },
                totalCount = player.totalCount,
                isCurrentUser = player.uid == currentUid,
            )
        }

        val currentRank = currentUid?.let { uid ->
            sorted.indexOfFirst { it.uid == uid }
                .takeIf { it >= 0 }
                ?.plus(1)
        }

        _state.update {
            it.copy(
                syncedTotal = currentRemotePlayer?.totalCount ?: 0,
                isWinner = currentRemotePlayer?.isWinner == true,
                winnerCode = currentRemotePlayer?.winnerCode.orEmpty(),
                currentRank = currentRank,
                topFive = topFive,
            )
        }
    }

    private fun buildStatusMessage(
        firebaseConfigured: Boolean,
        competitionWindow: MohamedLoversCompetitionWindow,
    ): String {
        return when {
            competitionWindow.networkNow == null -> competitionWindow.message
                ?: "لم يصل وقت الشبكة بعد، حاول التحديث بعد لحظات."
            !competitionWindow.isFridayInEgypt -> "الاحتساب يبدأ يوم الجمعة فقط بتوقيت القاهرة."
            !firebaseConfigured -> "يمكنك العد الآن محليًا، وسيتم رفع الجلسة عند عمل Firebase."
            else -> "الاحتساب مفتوح الآن. الضغطات ستُرفع مرة واحدة عند إغلاق الشاشة."
        }
    }
}
