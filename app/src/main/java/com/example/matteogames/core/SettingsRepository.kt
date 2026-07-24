package com.example.matteogames.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val appContext = context.applicationContext

    /** Number of rows/columns in the jigsaw grid. Default 2 (very easy for toddlers). */
    val gridSize: Flow<Int> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_GRID] ?: DEFAULT_GRID
    }

    suspend fun setGridSize(size: Int) {
        appContext.dataStore.edit { prefs -> prefs[KEY_GRID] = size }
    }

    /** Ids of puzzle pictures the child has already completed (persisted across restarts). */
    val completedPictures: Flow<Set<String>> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_COMPLETED] ?: emptySet()
    }

    suspend fun addCompletedPicture(id: String) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_COMPLETED] = (prefs[KEY_COMPLETED] ?: emptySet()) + id
        }
    }

    // --- Per-game resume state (so a game continues where it was left) ---

    val jigsawPicture: Flow<String> = appContext.dataStore.data.map { it[KEY_JIGSAW] ?: "" }
    suspend fun setJigsawPicture(id: String) {
        appContext.dataStore.edit { it[KEY_JIGSAW] = id }
    }

    val numbersCurrent: Flow<Int> = appContext.dataStore.data.map { it[KEY_NUMBERS] ?: 1 }
    suspend fun setNumbersCurrent(n: Int) {
        appContext.dataStore.edit { it[KEY_NUMBERS] = n }
    }

    val lettersLang: Flow<Int> = appContext.dataStore.data.map { it[KEY_LETTERS_LANG] ?: 0 }
    val lettersPos: Flow<Int> = appContext.dataStore.data.map { it[KEY_LETTERS_POS] ?: 0 }
    suspend fun setLetters(lang: Int, pos: Int) {
        appContext.dataStore.edit { it[KEY_LETTERS_LANG] = lang; it[KEY_LETTERS_POS] = pos }
    }

    companion object {
        const val DEFAULT_GRID = 2
        private val KEY_GRID = intPreferencesKey("grid_size")
        private val KEY_COMPLETED = stringSetPreferencesKey("completed_pictures")
        private val KEY_JIGSAW = stringPreferencesKey("jigsaw_picture")
        private val KEY_NUMBERS = intPreferencesKey("numbers_current")
        private val KEY_LETTERS_LANG = intPreferencesKey("letters_lang")
        private val KEY_LETTERS_POS = intPreferencesKey("letters_pos")
    }
}
