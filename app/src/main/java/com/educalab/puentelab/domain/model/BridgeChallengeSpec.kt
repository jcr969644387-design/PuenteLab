package com.educalab.puentelab.domain.model

data class StarThresholds(
    val budgetMarginFor2Stars: Double = 0.10,  // 10% del presupuesto sin usar
    val budgetMarginFor3Stars: Double = 0.25,  // 25% del presupuesto sin usar
    val maxStressFor3Stars: Double = 0.75      // ninguna barra por encima del 75% de su capacidad
)

data class BridgeChallengeSpec(
    val id: String,
    val scenario: ScenarioType,
    val orderIndex: Int,
    val name: String,
    val spanUnits: Double,
    val leftBank: GridPoint,
    val rightBank: GridPoint,
    val fixedSupports: List<GridPoint> = emptyList(),
    val budget: Double,
    val demand: DemandLevel,
    val maxSlope: Double = 0.6,
    val starThresholds: StarThresholds = StarThresholds(),
    val recommendedStructure: StructureType? = null,
    val narrativeIntro: String,
    val narrativeSuccess: String
)
