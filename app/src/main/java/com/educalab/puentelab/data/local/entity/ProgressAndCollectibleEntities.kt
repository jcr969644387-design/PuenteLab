package com.educalab.puentelab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.educalab.puentelab.domain.model.BadgeId
import com.educalab.puentelab.domain.model.ModuleState
import com.educalab.puentelab.domain.model.ScenarioType

/** Sello coleccionable de constructor (la colección local de PuenteLab, ver sección 9 del spec). */
@Entity(tableName = "builder_stamps")
data class BuilderStampEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val scenario: ScenarioType,
    val iconKey: String,
    val unlockChallengeId: String? = null,
    val unlockBadgeId: BadgeId? = null
)

@Entity(
    tableName = "user_stamps",
    foreignKeys = [
        ForeignKey(entity = UserProfileEntity::class, parentColumns = ["id"], childColumns = ["userProfileId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BuilderStampEntity::class, parentColumns = ["id"], childColumns = ["stampId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userProfileId"), Index("stampId", unique = false)]
)
data class UserStampEntity(
    @PrimaryKey val id: String,
    val userProfileId: String,
    val stampId: String,
    val unlockedAt: Long
)

/** Estado de progreso de un desafío concreto para el usuario local (calculado, no aleatorio). */
@Entity(
    tableName = "progress",
    foreignKeys = [
        ForeignKey(entity = UserProfileEntity::class, parentColumns = ["id"], childColumns = ["userProfileId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BridgeChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userProfileId"), Index("challengeId", unique = false)]
)
data class ProgressEntity(
    @PrimaryKey val id: String,
    val userProfileId: String,
    val challengeId: String,
    val state: ModuleState,
    val bestStars: Int,
    val attemptsCount: Int,
    val firstPassedAt: Long?
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: BadgeId,
    val name: String,
    val description: String,
    val iconKey: String
)

@Entity(
    tableName = "user_badges",
    foreignKeys = [
        ForeignKey(entity = UserProfileEntity::class, parentColumns = ["id"], childColumns = ["userProfileId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BadgeEntity::class, parentColumns = ["id"], childColumns = ["badgeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userProfileId"), Index("badgeId")]
)
data class UserBadgeEntity(
    @PrimaryKey val id: String,
    val userProfileId: String,
    val badgeId: BadgeId,
    val unlockedAt: Long
)
