package com.elsharif.dailyseventy.domain.repository

import android.content.Context
import android.util.Log
import com.elsharif.dailyseventy.domain.data.model.Zakker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ZekrRepository(private val context: Context) {

    // ✅ scope خاص بالـ repository — بيشتغل على IO من الأول
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _azkaarList = MutableStateFlow<List<Zakker>>(emptyList())
    val azkaarList: StateFlow<List<Zakker>> get() = _azkaarList

    // ✅ cache للفئات المفلترة عشان مانقراش الـ list كل مرة
    private val categoryCache = mutableMapOf<String, List<Zakker>>()

    init {
        repositoryScope.launch {
            loadAzkar()
        }
    }

    private suspend fun loadAzkar() {
        try {
            // ✅ FIX 1: IO dispatcher — الـ JSON بيتقرأ على background thread
            val allAzkar = withContext(Dispatchers.IO) {
                val jsonText = context.assets.open("Azkar.json")
                    .bufferedReader()
                    .use { it.readText() }

                // ✅ FIX 2: شلنا الـ Log.d اللي كان بيطبع الـ JSON كله —
                // ده كان بيسبب lag كبير خصوصاً لو الملف أكبر من 1MB
                // Log.d("JsonDebug", jsonText)  ← المشكلة كانت هنا

                val azkarMap: Map<String, List<Zakker>> = Json {
                    ignoreUnknownKeys = true  // ✅ أكثر robustness
                }.decodeFromString(jsonText)

                azkarMap.values.flatten()
            }

            _azkaarList.value = allAzkar

        } catch (e: Exception) {
            Log.e("ZekrRepository", "Error loading azkar: ${e.message}")
        }
    }

    // ✅ FIX 3: cache النتائج — مانعيدش الـ filter في كل navigation
    suspend fun getZekrByCategory(name: String): List<Zakker> {
        // لو عندنا cache نرجعه فوراً
        categoryCache[name]?.let { return it }

        // لو الـ list لسه فاضية (تحميل لسه شغّال) ننتظر
        val list = if (_azkaarList.value.isEmpty()) {
            withContext(Dispatchers.IO) {
                // ننتظر حتى يتحمل
                var attempts = 0
                while (_azkaarList.value.isEmpty() && attempts < 50) {
                    kotlinx.coroutines.delay(100)
                    attempts++
                }
                _azkaarList.value.filter { it.category == name }
            }
        } else {
            withContext(Dispatchers.Default) {
                _azkaarList.value.filter { it.category == name }
            }
        }

        categoryCache[name] = list
        return list
    }

    // ✅ تنظيف الـ cache لو الداتا اتحدّثت
    fun clearCache() {
        categoryCache.clear()
    }
}