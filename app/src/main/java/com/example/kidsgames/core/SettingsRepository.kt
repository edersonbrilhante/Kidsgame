package com.example.kidsgames.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    companion object {
        const val DEFAULT_GRID = 2
        private val KEY_GRID = intPreferencesKey("grid_size")
    }
}
