package com.educalab.puentelab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.educalab.puentelab.domain.model.ScenarioType

/** Vehículo que prueba el puente. weightMultiplier ajusta ligeramente la carga exigida. */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val themeScenario: ScenarioType,
    val weightMultiplier: Double,
    val unlockLevel: Int
)
