package com.educalab.puentelab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Perfil local del jugador. Nunca contiene nombre real, correo, teléfono ni ubicación:
 * solo un apodo elegido libremente y un identificador de avatar local (ver AvatarCatalog).
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = LOCAL_USER_ID,
    val alias: String,
    val avatarId: String,
    val createdAt: Long,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    // Cache derivada (recalculada siempre a partir de SimulationRunEntity; nunca es la fuente de verdad).
    val cachedXp: Int = 0,
    val cachedLevel: Int = 1
) {
    companion object {
        const val LOCAL_USER_ID = "local_user"
    }
}
