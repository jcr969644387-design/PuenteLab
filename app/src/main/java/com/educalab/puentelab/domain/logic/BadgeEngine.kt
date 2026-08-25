package com.educalab.puentelab.domain.logic

import com.educalab.puentelab.domain.model.*

object BadgeEngine {

    val catalog: List<BadgeSpec> = listOf(
        BadgeSpec(BadgeId.PRIMER_PUENTE, "Primer Puente", "Completa tu primer desafío."),
        BadgeSpec(BadgeId.EXPLORADOR, "Explorador/a", "Completa al menos un desafío en cada escenario."),
        BadgeSpec(BadgeId.MAESTRO_ARCO, "Maestro/a del Arco", "Aprueba 5 desafíos usando un arco."),
        BadgeSpec(BadgeId.INGENIERO_CERCHA, "Ingeniero/a de Cercha", "Aprueba 5 desafíos usando una cercha."),
        BadgeSpec(BadgeId.MAESTRO_SUSPENSION, "Maestro/a de Suspensión", "Aprueba 3 desafíos con puente colgante."),
        BadgeSpec(BadgeId.PRESUPUESTO_DE_ORO, "Presupuesto de Oro", "Aprueba 5 desafíos usando el 70% o menos del presupuesto."),
        BadgeSpec(BadgeId.SIN_FALLOS, "Sin Fallos", "Aprueba 10 desafíos a la primera."),
        BadgeSpec(BadgeId.COLECCIONISTA, "Coleccionista", "Consigue 10 desafíos con 3 estrellas."),
        BadgeSpec(BadgeId.VETERANO, "Veterano/a del Estudio", "Aprueba 25 desafíos en total.")
    )

    private const val TOTAL_SCENARIOS = 5

    /** Devuelve el mejor intento aprobado por desafío (o null si nunca se aprobó). */
    private fun bestPassedPerChallenge(attempts: List<ChallengeAttempt>): List<ChallengeAttempt> =
        attempts.filter { it.passed }
            .groupBy { it.challengeId }
            .values
            .map { list -> list.maxByOrNull { it.stars } ?: list.first() }

    fun unlockedBadges(attempts: List<ChallengeAttempt>): Set<BadgeId> {
        val best = bestPassedPerChallenge(attempts)
        val passedCount = best.size
        val scenarios = best.map { it.scenario }.toSet()
        val structureCounts = mutableMapOf<StructureType, Int>()
        for (a in best) for (t in a.structureTypesUsed) structureCounts[t] = (structureCounts[t] ?: 0) + 1
        val goldBudgetCount = best.count { it.budgetUsedRatio <= 0.7 }
        val firstTryCount = attempts.filter { it.passed && it.attemptNumber == 1 }
            .distinctBy { it.challengeId }.size
        val threeStarCount = best.count { it.stars == 3 }

        val unlocked = mutableSetOf<BadgeId>()
        if (passedCount >= 1) unlocked += BadgeId.PRIMER_PUENTE
        if (scenarios.size >= TOTAL_SCENARIOS) unlocked += BadgeId.EXPLORADOR
        if ((structureCounts[StructureType.ARCH] ?: 0) >= 5) unlocked += BadgeId.MAESTRO_ARCO
        if ((structureCounts[StructureType.TRUSS] ?: 0) >= 5) unlocked += BadgeId.INGENIERO_CERCHA
        if ((structureCounts[StructureType.SUSPENSION] ?: 0) >= 3) unlocked += BadgeId.MAESTRO_SUSPENSION
        if (goldBudgetCount >= 5) unlocked += BadgeId.PRESUPUESTO_DE_ORO
        if (firstTryCount >= 10) unlocked += BadgeId.SIN_FALLOS
        if (threeStarCount >= 10) unlocked += BadgeId.COLECCIONISTA
        if (passedCount >= 25) unlocked += BadgeId.VETERANO
        return unlocked
    }
}
