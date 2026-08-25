package com.educalab.puentelab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.puentelab.data.local.entity.BadgeEntity
import com.educalab.puentelab.data.local.entity.BuilderStampEntity
import com.educalab.puentelab.data.local.entity.ProgressEntity
import com.educalab.puentelab.data.local.entity.UserBadgeEntity
import com.educalab.puentelab.data.local.entity.UserStampEntity
import com.educalab.puentelab.domain.model.BadgeId
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE userProfileId = :userId")
    fun observeAll(userId: String): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE userProfileId = :userId AND challengeId = :challengeId LIMIT 1")
    suspend fun getForChallenge(userId: String, challengeId: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun observeAll(): Flow<List<BadgeEntity>>

    @Query("SELECT COUNT(*) FROM badges")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(badges: List<BadgeEntity>)
}

@Dao
interface UserBadgeDao {
    @Query("SELECT * FROM user_badges WHERE userProfileId = :userId")
    fun observeForUser(userId: String): Flow<List<UserBadgeEntity>>

    @Query("SELECT badgeId FROM user_badges WHERE userProfileId = :userId")
    suspend fun getUnlockedIds(userId: String): List<BadgeId>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userBadge: UserBadgeEntity)
}

@Dao
interface StampDao {
    @Query("SELECT * FROM builder_stamps")
    fun observeAll(): Flow<List<BuilderStampEntity>>

    @Query("SELECT COUNT(*) FROM builder_stamps")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stamps: List<BuilderStampEntity>)

    @Query("SELECT * FROM user_stamps WHERE userProfileId = :userId")
    fun observeUnlockedForUser(userId: String): Flow<List<UserStampEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlocked(userStamp: UserStampEntity)
}
