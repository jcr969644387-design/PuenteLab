package com.educalab.puentelab.domain.model

/** Registro histórico de un intento de desafío, usado para calcular XP e insignias. */
data class ChallengeAttempt(
    val challengeId: String,
    val scenario: ScenarioType,
    val passed: Boolean,
    val stars: Int,
    val structureTypesUsed: Set<StructureType>,
    val budgetUsedRatio: Double,
    val attemptNumber: Int
)

data class LevelInfo(
    val level: Int,
    val currentXp: Int,
    val xpForCurrentLevel: Int,
    val xpForNextLevel: Int?
) {
    val progressToNextLevel: Float
        get() {
            val next = xpForNextLevel ?: return 1f
            val span = (next - xpForCurrentLevel).coerceAtLeast(1)
            return ((currentXp - xpForCurrentLevel).toFloat() / span).coerceIn(0f, 1f)
        }
}

enum class BadgeId {
    PRIMER_PUENTE,
    EXPLORADOR,
    MAESTRO_ARCO,
    INGENIERO_CERCHA,
    MAESTRO_SUSPENSION,
    PRESUPUESTO_DE_ORO,
    SIN_FALLOS,
    COLECCIONISTA,
    VETERANO
}

data class BadgeSpec(
    val id: BadgeId,
    val name: String,
    val description: String
)
