package com.educalab.puentelab.data.local.dao

import androidx.room.*
import com.educalab.puentelab.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun observe(id: String = UserProfileEntity.LOCAL_USER_ID): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun get(id: String = UserProfileEntity.LOCAL_USER_ID): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET cachedXp = :xp, cachedLevel = :level WHERE id = :id")
    suspend fun updateXpAndLevel(xp: Int, level: Int, id: String = UserProfileEntity.LOCAL_USER_ID)

    @Query("UPDATE user_profile SET soundEnabled = :enabled WHERE id = :id")
    suspend fun setSoundEnabled(enabled: Boolean, id: String = UserProfileEntity.LOCAL_USER_ID)

    @Query("UPDATE user_profile SET hapticEnabled = :enabled WHERE id = :id")
    suspend fun setHapticEnabled(enabled: Boolean, id: String = UserProfileEntity.LOCAL_USER_ID)
}
