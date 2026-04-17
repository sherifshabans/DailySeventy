package com.elsharif.dailyseventy.domain.mohamedlovers

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MohamedLoversRepository @Inject constructor(
    private val firebaseClient: MohamedLoversFirebaseClient,
    private val networkTimeProvider: MohamedLoversNetworkTimeProvider,
    private val sessionStore: MohamedLoversSessionStore,
) {
    suspend fun bootstrap(): MohamedLoversBootstrap {
        return MohamedLoversBootstrap(
            alias = sessionStore.getOrCreateAlias(),
            firebaseConfigured = firebaseClient.isConfigured(),
            competitionWindow = networkTimeProvider.getCompetitionWindow(),
            pendingSession = sessionStore.getPendingSession(),
        )
    }

    suspend fun ensureAnonymousUser(): Result<String> = firebaseClient.ensureSignedInAnonymously()

    fun observeLeaderboard(roundKey: String): Flow<Result<List<MohamedLoversPlayer>>> {
        return firebaseClient.observePlayers(roundKey)
    }

    fun registerLocalTap(roundKey: String): MohamedLoversPendingSession {
        return sessionStore.incrementPendingClick(roundKey)
    }

    fun getPendingSession(): MohamedLoversPendingSession = sessionStore.getPendingSession()

    suspend fun flushPendingSession(alias: String): Result<Unit> {
        val pending = sessionStore.getPendingSession()
        if (pending.clickCount <= 0 || pending.roundKey.isNullOrBlank()) {
            return Result.success(Unit)
        }

        val safeAlias = alias.ifBlank { sessionStore.getOrCreateAlias() }
        val uid = ensureAnonymousUser().getOrElse { return Result.failure(it) }
        val result = firebaseClient.incrementSession(
            roundKey = pending.roundKey,
            uid = uid,
            alias = safeAlias,
            delta = pending.clickCount,
        )

        if (result.isSuccess) {
            sessionStore.clearPendingSession()
        }

        return result
    }

    fun refreshNetworkTime() {
        networkTimeProvider.prime()
    }
}
