package com.educalab.puentelab.data.repository

import com.educalab.puentelab.data.local.dao.UserProfileDao
import com.educalab.puentelab.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val dao: UserProfileDao) {

    fun observeProfile(): Flow<UserProfileEntity?> = dao.observe()

    suspend fun getOrCreateProfile(): UserProfileEntity {
        return dao.get() ?: UserProfileEntity(
            alias = "",
            avatarId = "avatar_casco_naranja",
            createdAt = System.currentTimeMillis(),
            onboardingCompleted = false
        ).also { dao.upsert(it) }
    }

    suspend fun completeOnboarding(alias: String, avatarId: String) {
        val current = getOrCreateProfile()
        dao.upsert(current.copy(alias = alias, avatarId = avatarId, onboardingCompleted = true))
    }

    suspend fun setSoundEnabled(enabled: Boolean) = dao.setSoundEnabled(enabled)
    suspend fun setHapticEnabled(enabled: Boolean) = dao.setHapticEnabled(enabled)

    suspend fun updateXpAndLevel(xp: Int, level: Int) = dao.updateXpAndLevel(xp, level)
}
