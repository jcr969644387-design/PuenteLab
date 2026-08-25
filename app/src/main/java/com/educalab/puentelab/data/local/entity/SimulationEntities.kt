package com.educalab.puentelab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.educalab.puentelab.domain.model.MemberRole
import com.educalab.puentelab.domain.model.StructureType

/**
 * Un intento de simulación ("Probar puente"). Es el registro de historial real que alimenta
 * ProgressEngine (XP) y BadgeEngine (insignias); nunca se generan intentos falsos ni aleatorios.
 */
@Entity(
    tableName = "simulation_runs",
    foreignKeys = [
        ForeignKey(entity = BridgeDesignEntity::class, parentColumns = ["id"], childColumns = ["designId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BridgeChallengeEntity::class, parentColumns = ["id"], childColumns = ["challengeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = VehicleEntity::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("designId"), Index("challengeId"), Index("vehicleId"), Index("ranAt")]
)
data class SimulationRunEntity(
    @PrimaryKey val id: String,
    val designId: String,
    val challengeId: String,
    val vehicleId: String,
    val ranAt: Long,
    val attemptNumber: Int,
    val passed: Boolean,
    val totalCost: Double,
    val budget: Double,
    val budgetRemaining: Double,
    val maxStressRatio: Double,
    val weakestMemberId: String?,
    val stars: Int,
    val structureTypesUsed: Set<StructureType>,
    val feedback: String // mensajes concatenados con '|' para mostrar en el historial
)

/** Detalle por barra de un intento concreto (para la pantalla de resultados/optimización). */
@Entity(
    tableName = "simulation_results",
    foreignKeys = [
        ForeignKey(entity = SimulationRunEntity::class, parentColumns = ["id"], childColumns = ["simulationRunId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("simulationRunId")]
)
data class SimulationResultEntity(
    @PrimaryKey val id: String,
    val simulationRunId: String,
    val memberId: String,
    val length: Double,
    val cost: Double,
    val capacity: Double,
    val demand: Double,
    val stressRatio: Double,
    val role: MemberRole
)
