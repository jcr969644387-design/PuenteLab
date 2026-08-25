package com.educalab.puentelab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.educalab.puentelab.domain.model.MemberRole

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val strength: Double,
    val costPerUnit: Double,
    val weightFactor: Double,
    val allowedRoles: Set<MemberRole>,
    val colorHex: String,
    val iconKey: String,
    val unlockLevel: Int = 1 // nivel mínimo del jugador para usarlo (progresión de materiales)
)
