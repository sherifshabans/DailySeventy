package com.elsharif.dailyseventy.domain.mohamedlovers

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MohamedLoversFirebaseClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @Volatile
    private var persistenceEnabled = false

    fun isConfigured(): Boolean = ensureFirebaseApp() != null

    suspend fun ensureSignedInAnonymously(): Result<String> {
        val firebaseApp = ensureFirebaseApp()
            ?: return Result.failure(IllegalStateException("Firebase is not configured for this build."))

        return runCatching {
            val auth = FirebaseAuth.getInstance(firebaseApp)
            auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid
                ?: error("Firebase anonymous sign-in failed.")
        }
    }

    fun observePlayers(roundKey: String): Flow<Result<List<MohamedLoversPlayer>>> = callbackFlow {
        val query = playersQuery(roundKey)
        if (query == null) {
            trySend(Result.failure(IllegalStateException("Firebase is not configured for this build.")))
            close()
            return@callbackFlow
        }

        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Result.success(snapshot.children.mapNotNull { it.toMohamedLoversPlayer() }))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun incrementSession(
        roundKey: String,
        uid: String,
        alias: String,
        delta: Int,
    ): Result<Unit> {
        val database = database() ?: return Result.failure(
            IllegalStateException("Firebase is not configured for this build."),
        )

        val playerRef = database.reference
            .child(ROOT_PATH)
            .child(roundKey)
            .child(PLAYERS_PATH)
            .child(uid)

        return suspendCancellableCoroutine { continuation ->
            playerRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): Transaction.Result {
                    val currentValue = currentData.value as? Map<*, *> ?: emptyMap<String, Any?>()
                    val existingTotal = (currentValue[TOTAL_COUNT_KEY] as? Number)?.toInt() ?: 0
                    val isWinner = currentValue[IS_WINNER_KEY] as? Boolean ?: false
                    val winnerCode = currentValue[WINNER_CODE_KEY] as? String ?: ""

                    currentData.child(UID_KEY).value = uid
                    currentData.child(ALIAS_KEY).value = alias
                    currentData.child(COUNTRY_CODE_KEY).value = COUNTRY_CODE_EGYPT
                    currentData.child(TOTAL_COUNT_KEY).value = existingTotal + delta
                    currentData.child(IS_WINNER_KEY).value = isWinner
                    currentData.child(WINNER_CODE_KEY).value = winnerCode
                    currentData.child(UPDATED_AT_KEY).value = ServerValue.TIMESTAMP

                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?,
                ) {
                    if (!continuation.isActive) {
                        return
                    }

                    when {
                        error != null -> continuation.resume(Result.failure(error.toException()))
                        !committed -> continuation.resume(
                            Result.failure(IllegalStateException("Competition update was not committed.")),
                        )
                        else -> continuation.resume(Result.success(Unit))
                    }
                }
            })
        }
    }

    private fun playersQuery(roundKey: String): Query? {
        val database = database() ?: return null
        return database.reference
            .child(ROOT_PATH)
            .child(roundKey)
            .child(PLAYERS_PATH)
            .orderByChild(TOTAL_COUNT_KEY)
    }

    private fun database(): FirebaseDatabase? {
        val firebaseApp = ensureFirebaseApp() ?: return null
        val database = FirebaseDatabase.getInstance(firebaseApp)

        if (!persistenceEnabled) {
            synchronized(this) {
                if (!persistenceEnabled) {
                    runCatching { database.setPersistenceEnabled(true) }
                    persistenceEnabled = true
                }
            }
        }

        return database
    }

    private fun ensureFirebaseApp(): FirebaseApp? {
        return FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
    }

    private fun DataSnapshot.toMohamedLoversPlayer(): MohamedLoversPlayer? {
        val uid = child(UID_KEY).getValue(String::class.java)
            ?: key
            ?: return null

        return MohamedLoversPlayer(
            uid = uid,
            alias = child(ALIAS_KEY).getValue(String::class.java).orEmpty(),
            totalCount = child(TOTAL_COUNT_KEY).getValue(Int::class.java)
                ?: child(TOTAL_COUNT_KEY).getValue(Long::class.java)?.toInt()
                ?: 0,
            isWinner = child(IS_WINNER_KEY).getValue(Boolean::class.java) ?: false,
            winnerCode = child(WINNER_CODE_KEY).getValue(String::class.java).orEmpty(),
            countryCode = child(COUNTRY_CODE_KEY).getValue(String::class.java).orEmpty(),
            updatedAt = child(UPDATED_AT_KEY).getValue(Long::class.java) ?: 0L,
        )
    }

    private companion object {
        const val ROOT_PATH = "mohamed_lovers"
        const val PLAYERS_PATH = "players"
        const val UID_KEY = "uid"
        const val ALIAS_KEY = "alias"
        const val TOTAL_COUNT_KEY = "totalCount"
        const val IS_WINNER_KEY = "isWinner"
        const val WINNER_CODE_KEY = "winnerCode"
        const val COUNTRY_CODE_KEY = "countryCode"
        const val UPDATED_AT_KEY = "updatedAt"
        const val COUNTRY_CODE_EGYPT = "EG"
    }
}
