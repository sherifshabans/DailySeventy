package com.elsharif.dailyseventy.domain

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.domain.prayertiming.DomainPrayerTiming
import com.example.core.domain.prayertiming.DomainPrayerTimingSchool
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "PRAYERS_PREF")

class AppPreferences(private val context: Context) {

    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        Log.d("AppPreferences", "Preferences initialized")
    }

    // Language Support
    enum class SupportedLanguage(val code: String, val displayName: String) {
        ARABIC("ar", "العربية"),
        ENGLISH("en", "English"),
        FRENCH("fr", "Français"),
        SPANISH("es", "Español"),
        GERMAN("de", "Deutsch"),
        TURKISH("tr", "Türkçe"),
        URDU("ur", "اردو"),
        MALAY("ms", "Bahasa Melayu"),
        INDONESIAN("in", "Bahasa Indonesia"),
        BENGALI("bn", "বাংলা")
    }

    val currentLanguage: Flow<SupportedLanguage>
        get() = context.dataStore.data.map {
            val languageCode = it[LANGUAGE_STORE_KEY] ?: SupportedLanguage.ARABIC.code
            Log.d("AppPreferences", "Current language code: $languageCode")
            SupportedLanguage.values().find { lang -> lang.code == languageCode }
                ?: SupportedLanguage.ARABIC
        }

    suspend fun setLanguage(language: SupportedLanguage) {
        Log.d("AppPreferences", "Setting language to: ${language.code}")

        // Save to DataStore
        context.dataStore.edit {
            it[LANGUAGE_STORE_KEY] = language.code
        }

        // Save to SharedPreferences for immediate access
        preferences.edit {
            putString(LANGUAGE_KEY, language.code)
        }

        // Apply language using both methods for maximum compatibility
        try {
            // Method 1: AppCompatDelegate (for newer Android versions)
            val locales = LocaleListCompat.forLanguageTags(language.code)
            AppCompatDelegate.setApplicationLocales(locales)

            // Method 2: Manual locale setting (for immediate effect)
            setLocale(context, language.code)

            Log.d("AppPreferences", "Language applied: ${language.displayName}")
        } catch (e: Exception) {
            Log.e("AppPreferences", "Error applying language: ${e.message}")
        }
    }

    // Manual locale setting method
    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    // Get saved language immediately (synchronous)
    fun getSavedLanguageCode(): String {
        val code = preferences.getString(LANGUAGE_KEY, SupportedLanguage.ARABIC.code)
            ?: SupportedLanguage.ARABIC.code
        // "in" هو نفس "id" في Android القديم
        return if (code == "in") "id" else code
    }
    // Initialize app language on startup
    fun initializeLanguage() {
        try {
            val languageCode = getSavedLanguageCode()
            Log.d("AppPreferences", "Initializing with language code: $languageCode")

            // Apply using both methods
            val locales = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(locales)

            // Also set manually for immediate effect
            setLocale(context, languageCode)

            Log.d("AppPreferences", "Applied language: $languageCode")

        } catch (e: Exception) {
            Log.e("AppPreferences", "Error initializing language: ${e.message}")
            // Fallback to Arabic
            val locales = LocaleListCompat.forLanguageTags(SupportedLanguage.ARABIC.code)
            AppCompatDelegate.setApplicationLocales(locales)
            setLocale(context, SupportedLanguage.ARABIC.code)
        }
    }

    val isFirstTime: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[IS_FIRST_TIME_STORE_KEY] ?: true
        }

    val method: Flow<DomainPrayerTimingSchool>
        get() = context.dataStore.data.map {
            DomainPrayerTimingSchool(it[METHOD_ID_STORE_KEY] ?: 5, it[METHOD_NAME_STORE_KEY] ?: "")
        }

    val tasbeehCounter: Flow<Int> get() = context.dataStore.data.map { it[TASBEEH_STORE_KEY] ?: 0 }

    val currentLocation: Flow<Pair<Double, Double>>
        get() = context.dataStore.data.map {
            Pair(
                it[LAT_STORE_KEY] ?: 21.422487,
                it[LNG_STORE_KEY] ?: 39.826206
            )
        }

    suspend fun setMethod(method: DomainPrayerTimingSchool) {
        context.dataStore.edit {
            it[METHOD_ID_STORE_KEY] = method.id
            it[METHOD_NAME_STORE_KEY] = method.name
        }
    }

    suspend fun setLocation(location: Pair<Double, Double>) {
        context.dataStore.edit {
            it[LAT_STORE_KEY] = location.first
            it[LNG_STORE_KEY] = location.second
        }
    }

    fun toggleDarkMode() {
        val isDarkMode = isDarkModeEnabled()
        AppCompatDelegate.setDefaultNightMode(if (!isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        preferences.edit { putBoolean(DARK_MODE_ENABLED, !isDarkMode) }
    }

    suspend fun setTasbeeh(i: Int) {
        context.dataStore.edit {
            it[TASBEEH_STORE_KEY] = i
        }
    }

    suspend fun increaseTasbeeh() {
        context.dataStore.edit {
            Log.d("TAG", "increaseTasbeeh: ${it[TASBEEH_STORE_KEY]}")
            it[TASBEEH_STORE_KEY] = (it[TASBEEH_STORE_KEY] ?: 1) + 1
        }
    }

    suspend fun setIsFirstTime() {
        context.dataStore.edit {
            it[IS_FIRST_TIME_STORE_KEY] = false
        }
    }

    suspend fun updatePrayerTimes(prayers: List<DomainPrayerTiming>) {
        val serialized = Gson().toJson(prayers)
        context.dataStore.edit {
            it[PRAYERS_STORE_KEY] = serialized
        }
    }

    fun getPrayerTimes(): Flow<List<DomainPrayerTiming>> {
        return context.dataStore.data.map {
            it[PRAYERS_STORE_KEY]
        }.map { jsonString ->
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                val typeToken = object : TypeToken<List<DomainPrayerTiming>>() {}.type
                Gson().fromJson<List<DomainPrayerTiming>>(jsonString, typeToken)
            }
        }
    }

    fun isDarkModeEnabled(): Boolean = preferences.getBoolean(DARK_MODE_ENABLED, false)

    suspend fun nextQuranPage() {
        context.dataStore.edit {
            it[KHATMA_PAGE] = ((it[KHATMA_PAGE] ?: 1) + 1) % 605
        }
    }

    suspend fun previousQuranPage() {
        context.dataStore.edit {
            it[KHATMA_PAGE] = ((it[KHATMA_PAGE] ?: 1) - 1).coerceAtLeast(1)
        }
    }

    suspend fun setCurrentQuranPage(i: Int) {
        context.dataStore.edit { it[KHATMA_PAGE] = i }
    }

    val currentQuranPage: Flow<Int> = context.dataStore.data.map {
        it[KHATMA_PAGE] ?: 1
    }

    companion object {
        private const val FILE_NAME = "PRAYERS_PREF"
        private const val CITY_KEY = "CITY_PREF"
        private const val COUNTRY_KEY = "COUNTRY_PREF"
        private const val METHOD_KEY = "METHOD_PREF"
        private const val LAT_KEY = "LAT_PREF"
        private const val LNG_KEY = "LNG_PREF"

        private const val IS_FIRST_TIME = "IS_FIRST_TIME"
        private const val METHOD_ID_KEY = "METHOD_ID_PREF"
        private const val METHOD_NAME_KEY = "METHOD_NAME_PREF"

        private const val TASBEEH_KEY = "TASBEEH"
        private const val DARK_MODE_ENABLED = "DARK_MODE_ENABLED"
        private const val PRAYER_TIMES = "PRAYER_TIMES"
        private const val LANGUAGE_KEY = "LANGUAGE_PREF"

        // DataStore Keys
        private val METHOD_ID_STORE_KEY = intPreferencesKey(METHOD_ID_KEY)
        private val METHOD_NAME_STORE_KEY = stringPreferencesKey(METHOD_NAME_KEY)
        private val PRAYERS_STORE_KEY = stringPreferencesKey(PRAYER_TIMES)
        private val LAT_STORE_KEY = doublePreferencesKey(LAT_KEY)
        private val LNG_STORE_KEY = doublePreferencesKey(LNG_KEY)
        private val TASBEEH_STORE_KEY = intPreferencesKey(TASBEEH_KEY)
        private val DARK_MODE_STORE_KEY = booleanPreferencesKey(DARK_MODE_ENABLED)
        private val IS_FIRST_TIME_STORE_KEY = booleanPreferencesKey(IS_FIRST_TIME)
        private val KHATMA_PAGE = intPreferencesKey("KHATMA_PAGE")
        private val LANGUAGE_STORE_KEY = stringPreferencesKey(LANGUAGE_KEY)
    }
}