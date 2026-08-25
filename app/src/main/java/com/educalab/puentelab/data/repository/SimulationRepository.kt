package com.educalab.puentelab.data.repository

import com.educalab.puentelab.data.local.dao.*
import com.educalab.puentelab.data.local.entity.*
import com.educalab.puentelab.domain.logic.BadgeEngine
import com.educalab.puentelab.domain.logic.BridgeEngine
import com.educalab.puentelab.domain.logic.ProgressEngine
import com.educalab.puentelab.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

data class SimulationOutcome(
    val result: SimulationResult,
    val newlyUnlockedBadges: List<BadgeId>
)

/**
 * Orquesta una "Prueba de puente": ejecuta BridgeEngine, persiste el intento y su detalle por
 * barra, actualiza el progreso del desafío, recalcula XP/nivel a partir del historial completo
 * y desbloquea insignias/sellos nuevos. Todo queda en Room; nada se pierde al cerrar la app.
 */
class SimulationRepository(
    private val runDao: SimulationRunDao,
    private val resultDao: SimulationResultDao,
    private val progressDao: ProgressDao,
    private val userBadgeDao: UserBadgeDao,
    private val stampDao: StampDao,
    private val userProfileDao: UserProfileDao,
    private val materialDao: MaterialDao,
    private val userId: String = UserProfileEntity.LOCAL_USER_ID
) {
    fun observeHistoryForChallenge(challengeId: String): Flow<List<SimulationRunEntity>> =
        runDao.observeForChallenge(challengeId)

    fun observeAllHistory(): Flow<List<SimulationRunEntity>> = runDao.observeAll()

    suspend fun runSimulation(
        design: BridgeDesignSpec,
        challenge: BridgeChallengeSpec,
        vehicleId: String,
        vehicleWeightMultiplier: Double
    ): SimulationOutcome {
        val materials = materialDao.observeAll().first().associate { it.id to it.toDomain() }

        val effectiveChallenge = if (vehicleWeightMultiplier == 1.0) challenge else {
            val scaledDemand = DemandLevel.values().minByOrNull {
                kotlin.math.abs(it.loadUnits - challenge.demand.loadUnits * vehicleWeightMultiplier)
            } ?: challenge.demand
            challenge.copy(demand = scaledDemand)
        }
        val result = BridgeEngine.simulate(design, effectiveChallenge, materials)

        val attemptNumber = runDao.countAttemptsForChallenge(challenge.id) + 1
        val runId = UUID.randomUUID().toString()
        val structureTypesUsed = design.members.map { it.structureType }.toSet()

        runDao.insert(
            SimulationRunEntity(
                id = runId, designId = design.id, challengeId = challenge.id,
                vehicleId = vehicleId, ranAt = System.currentTimeMillis(), attemptNumber = attemptNumber,
                passed = result.passed, totalCost = result.totalCost, budget = result.budget,
                budgetRemaining = result.budgetRemaining, maxStressRatio = result.maxStressRatio,
                weakestMemberId = result.weakestMemberId, stars = result.stars,
                structureTypesUsed = structureTypesUsed, feedback = result.feedback.joinToString("|")
            )
        )
        resultDao.insertAll(
            result.memberAnalyses.map {
                SimulationResultEntity(
                    id = UUID.randomUUID().toString(), simulationRunId = runId, memberId = it.memberId,
                    length = it.length, cost = it.cost, capacity = it.capacity, demand = it.demand,
                    stressRatio = it.stressRatio, role = it.role
                )
            }
        )

        updateProgress(challenge.id, result)
        val newlyUnlocked = updateXpAndBadges()
        updateStamps(challenge.id, newlyUnlocked)

        return SimulationOutcome(result, newlyUnlocked)
    }

    private suspend fun updateProgress(challengeId: String, result: SimulationResult) {
        val existing = progressDao.getForChallenge(userId, challengeId)
        val attempts = (existing?.attemptsCount ?: 0) + 1
        val bestStars = maxOf(existing?.bestStars ?: 0, result.stars)
        val state = when {
            bestStars == 3 -> ModuleState.MASTERED
            bestStars > 0 || result.passed -> ModuleState.COMPLETED
            else -> ModuleState.STARTED
        }
        progressDao.upsert(
            ProgressEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                userProfileId = userId, challengeId = challengeId, state = state,
                bestStars = bestStars, attemptsCount = attempts,
                firstPassedAt = existing?.firstPassedAt ?: if (result.passed) System.currentTimeMillis() else null
            )
        )
    }

    private suspend fun updateXpAndBadges(): List<BadgeId> {
        val allRuns = runDao.observeAll().first()
        val attempts = allRuns.map { run ->
            ChallengeAttempt(
                challengeId = run.challengeId,
                scenario = scenarioForChallenge(run.challengeId),
                passed = run.passed, stars = run.stars, structureTypesUsed = run.structureTypesUsed,
                budgetUsedRatio = if (run.budget > 0) run.totalCost / run.budget else 0.0,
                attemptNumber = run.attemptNumber
            )
        }
        val xp = ProgressEngine.totalXp(attempts)
        val level = ProgressEngine.levelInfo(xp).level
        userProfileDao.updateXpAndLevel(xp, level)

        val alreadyUnlocked = userBadgeDao.getUnlockedIds(userId).toSet()
        val nowUnlocked = BadgeEngine.unlockedBadges(attempts)
        val newlyUnlocked = (nowUnlocked - alreadyUnlocked).toList()
        for (badgeId in newlyUnlocked) {
            userBadgeDao.insert(UserBadgeEntity(UUID.randomUUID().toString(), userId, badgeId, System.currentTimeMillis()))
        }
        return newlyUnlocked
    }

    // Cache simple en memoria del proceso para no repetir la consulta de escenario por cada intento.
    private val scenarioCache = mutableMapOf<String, ScenarioType>()
    private fun scenarioForChallenge(challengeId: String): ScenarioType {
        scenarioCache[challengeId]?.let { return it }
        // El prefijo del id de desafío codifica el escenario (ver SeedChallenges: "river_01", etc.)
        val prefix = challengeId.substringBeforeLast("_")
        val scenario = ScenarioType.values().firstOrNull { it.name.equals(prefix, ignoreCase = true) } ?: ScenarioType.RIVER
        scenarioCache[challengeId] = scenario
        return scenario
    }

    private suspend fun updateStamps(passedChallengeId: String, newlyUnlockedBadges: List<BadgeId>) {
        val allStamps = stampDao.observeAll().first()
        for (stamp in allStamps) {
            val shouldUnlock = (stamp.unlockChallengeId == passedChallengeId) ||
                (stamp.unlockBadgeId != null && stamp.unlockBadgeId in newlyUnlockedBadges)
            if (shouldUnlock) {
                stampDao.insertUnlocked(UserStampEntity(UUID.randomUUID().toString(), userId, stamp.id, System.currentTimeMillis()))
            }
        }
    }
}
