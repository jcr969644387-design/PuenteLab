package com.educalab.puentelab.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "puentelab_prefs")

/**
 * Preferencias sueltas que no encajan como columnas del perfil (Room), como si el jugador ya
 * vio el tutorial de "Cómo construir tu puente" alguna vez, para no repetirlo en cada desafío.
 */
class AppPreferences(private val context: Context) {
    private val keyHasSeenBuilderInstructions = booleanPreferencesKey("has_seen_builder_instructions")

    val hasSeenBuilderInstructions: Flow<Boolean> =
        context.dataStore.data.map { it[keyHasSeenBuilderInstructions] ?: false }

    suspend fun hasSeenBuilderInstructionsOnce(): Boolean = hasSeenBuilderInstructions.first()

    suspend fun markBuilderInstructionsSeen() {
        context.dataStore.edit { it[keyHasSeenBuilderInstructions] = true }
    }
}
