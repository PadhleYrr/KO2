package com.gkk.mppsc.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gkk.mppsc.data.models.Bookmark
import com.gkk.mppsc.data.models.SrsEntry
import com.gkk.mppsc.ui.theme.GKKTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gkk_prefs")

class PrefsRepository(private val context: Context) {

    private val gson = Gson()

    // ── Keys ────────────────────────────────────────────────────────
    companion object {
        val KEY_THEME         = stringPreferencesKey("user_theme")
        val KEY_STREAK        = intPreferencesKey("streak_days")
        val KEY_STREAK_DATE   = stringPreferencesKey("streak_last_date")
        val KEY_ATTEMPTED     = intPreferencesKey("total_attempted")
        val KEY_CORRECT       = intPreferencesKey("total_correct")
        val KEY_BOOKMARKS     = stringPreferencesKey("bookmarks_json")
        val KEY_SRS           = stringPreferencesKey("srs_json")
        val KEY_HEATMAP       = stringPreferencesKey("heatmap_json")
        val KEY_CHAPTER_STATS = stringPreferencesKey("chapter_stats_json")
        val KEY_FLAGS         = stringPreferencesKey("flags_json")
        val KEY_STUDY_TIME    = longPreferencesKey("study_time_ms")
        val KEY_NOTIF_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    // ── Theme ────────────────────────────────────────────────────────
    val themeFlow: Flow<GKKTheme> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val id = prefs[KEY_THEME] ?: "default"
            GKKTheme.entries.find { it.id == id } ?: GKKTheme.DEFAULT
        }

    suspend fun setTheme(theme: GKKTheme) {
        context.dataStore.edit { it[KEY_THEME] = theme.id }
    }

    // ── Streak ───────────────────────────────────────────────────────
    val streakFlow: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_STREAK] ?: 0 }

    suspend fun updateStreak(days: Int, dateStr: String) {
        context.dataStore.edit {
            it[KEY_STREAK]      = days
            it[KEY_STREAK_DATE] = dateStr
        }
    }

    suspend fun getStreakLastDate(): String {
        var date = ""
        context.dataStore.data.collect { date = it[KEY_STREAK_DATE] ?: "" }
        return date
    }

    // ── Stats ────────────────────────────────────────────────────────
    val statsFlow: Flow<Pair<Int, Int>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { Pair(it[KEY_ATTEMPTED] ?: 0, it[KEY_CORRECT] ?: 0) }

    suspend fun recordAnswer(isCorrect: Boolean) {
        context.dataStore.edit {
            it[KEY_ATTEMPTED] = (it[KEY_ATTEMPTED] ?: 0) + 1
            if (isCorrect) it[KEY_CORRECT] = (it[KEY_CORRECT] ?: 0) + 1
        }
    }

    // ── Chapter stats (Map<chapterName, Pair<attempted,correct>>) ────
    val chapterStatsFlow: Flow<Map<String, Pair<Int, Int>>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val json = prefs[KEY_CHAPTER_STATS] ?: return@map emptyMap()
            val type = object : TypeToken<Map<String, List<Int>>>() {}.type
            val raw  = gson.fromJson<Map<String, List<Int>>>(json, type)
            raw.mapValues { (_, v) -> Pair(v[0], v[1]) }
        }

    suspend fun recordChapterAnswer(chapter: String, isCorrect: Boolean) {
        context.dataStore.edit { prefs ->
            val json  = prefs[KEY_CHAPTER_STATS] ?: "{}"
            val type  = object : TypeToken<MutableMap<String, MutableList<Int>>>() {}.type
            val stats = gson.fromJson<MutableMap<String, MutableList<Int>>>(json, type)
                ?: mutableMapOf()
            val entry = stats.getOrPut(chapter) { mutableListOf(0, 0) }
            entry[0] += 1
            if (isCorrect) entry[1] += 1
            prefs[KEY_CHAPTER_STATS] = gson.toJson(stats)
        }
    }

    // ── Heatmap (Map<dateString "yyyy-MM-dd", sessionCount>) ─────────
    val heatmapFlow: Flow<Map<String, Int>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val json = prefs[KEY_HEATMAP] ?: return@map emptyMap()
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        }

    suspend fun recordStudyDay(dateStr: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_HEATMAP] ?: "{}"
            val type = object : TypeToken<MutableMap<String, Int>>() {}.type
            val map  = gson.fromJson<MutableMap<String, Int>>(json, type) ?: mutableMapOf()
            map[dateStr] = (map[dateStr] ?: 0) + 1
            prefs[KEY_HEATMAP] = gson.toJson(map)
        }
    }

    // ── Bookmarks ────────────────────────────────────────────────────
    val bookmarksFlow: Flow<List<Bookmark>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val json = prefs[KEY_BOOKMARKS] ?: return@map emptyList()
            val type = object : TypeToken<List<Bookmark>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }

    suspend fun saveBookmarks(bookmarks: List<Bookmark>) {
        context.dataStore.edit { it[KEY_BOOKMARKS] = gson.toJson(bookmarks) }
    }

    // ── SRS data ─────────────────────────────────────────────────────
    val srsFlow: Flow<List<SrsEntry>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val json = prefs[KEY_SRS] ?: return@map emptyList()
            val type = object : TypeToken<List<SrsEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }

    suspend fun saveSrs(entries: List<SrsEntry>) {
        context.dataStore.edit { it[KEY_SRS] = gson.toJson(entries) }
    }

    // ── Notifications ────────────────────────────────────────────────
    val notifEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_NOTIF_ENABLED] ?: false }

    suspend fun setNotifEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIF_ENABLED] = enabled }
    }
}
