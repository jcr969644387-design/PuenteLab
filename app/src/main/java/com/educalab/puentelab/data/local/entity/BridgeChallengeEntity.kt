package com.educalab.puentelab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.educalab.puentelab.domain.model.DemandLevel
import com.educalab.puentelab.domain.model.GridPoint
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.domain.model.StructureType

@Entity(tableName = "bridge_challenges")
data class BridgeChallengeEntity(
    @PrimaryKey val id: String,
    val scenario: ScenarioType,
    val orderIndex: Int,
    val name: String,
    val spanUnits: Double,
    val leftBankX: Double,
    val leftBankY: Double,
    val rightBankX: Double,
    val rightBankY: Double,
    val fixedSupports: List<GridPoint>,
    val budget: Double,
    val demand: DemandLevel,
    val maxSlope: Double,
    val budgetMarginFor2Stars: Double,
    val budgetMarginFor3Stars: Double,
    val maxStressFor3Stars: Double,
    val recommendedStructure: StructureType?,
    val narrativeIntro: String,
    val narrativeSuccess: String
)
