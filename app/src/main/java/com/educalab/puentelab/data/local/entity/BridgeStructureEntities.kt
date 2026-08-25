package com.educalab.puentelab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.educalab.puentelab.domain.model.AnchorSide
import com.educalab.puentelab.domain.model.MemberRole
import com.educalab.puentelab.domain.model.StructureType

/**
 * Un diseño de puente para un desafío. `isSaved` indica si cuenta contra el cupo de
 * "Mis diseños" (máx. 15, controlado por DesignRepository; ver docs/MANUAL_TECNICO.md,
 * sección de simplificaciones: no se usa una tabla SavedDesign separada, se documenta aquí
 * explícitamente en vez de ocultar la simplificación).
 */
@Entity(
    tableName = "bridge_designs",
    foreignKeys = [
        ForeignKey(
            entity = BridgeChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("challengeId"), Index("userProfileId"), Index("isSaved")]
)
data class BridgeDesignEntity(
    @PrimaryKey val id: String,
    val challengeId: String,
    val userProfileId: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSaved: Boolean = false,
    val duplicatedFromId: String? = null
)

@Entity(
    tableName = "bridge_nodes",
    foreignKeys = [
        ForeignKey(entity = BridgeDesignEntity::class, parentColumns = ["id"], childColumns = ["designId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("designId")]
)
data class BridgeNodeEntity(
    @PrimaryKey val id: String,
    val designId: String,
    val x: Double,
    val y: Double,
    val anchorSide: AnchorSide,
    val isFixedByLevel: Boolean,
    val isUserPier: Boolean
)

@Entity(
    tableName = "bridge_members",
    foreignKeys = [
        ForeignKey(entity = BridgeDesignEntity::class, parentColumns = ["id"], childColumns = ["designId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MaterialEntity::class, parentColumns = ["id"], childColumns = ["materialId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("designId"), Index("materialId")]
)
data class BridgeMemberEntity(
    @PrimaryKey val id: String,
    val designId: String,
    val nodeAId: String,
    val nodeBId: String,
    val materialId: String,
    val role: MemberRole,
    val structureType: StructureType
)
