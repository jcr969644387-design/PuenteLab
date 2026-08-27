package com.educalab.puentelab.data.repository

import com.educalab.puentelab.data.local.dao.*
import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.domain.model.ScenarioProgression
import com.educalab.puentelab.domain.model.ScenarioType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CatalogRepository(
    private val materialDao: MaterialDao,
    private val vehicleDao: VehicleDao,
    private val challengeDao: ChallengeDao,
    private val badgeDao: BadgeDao,
    private val stampDao: StampDao
) {
    fun observeMaterials() = materialDao.observeAll()
    suspend fun getMaterialsMap() = materialDao.observeAll()

    fun observeVehicles() = vehicleDao.observeAll()
    suspend fun getVehicle(id: String) = vehicleDao.getById(id)
    suspend fun getDefaultVehicleForScenario(scenario: ScenarioType) = vehicleDao.getDefaultForScenario(scenario)

    fun observeChallenges() = challengeDao.observeAll()
    fun observeChallengesByScenario(scenario: ScenarioType) = challengeDao.observeByScenario(scenario)
    suspend fun getChallenge(id: String) = challengeDao.getById(id)

    /** Siguiente misión tras aprobar una: mismo escenario +1 nivel, o el nivel 1 del siguiente escenario. */
    suspend fun getNextChallenge(scenario: ScenarioType, orderIndex: Int): BridgeChallengeEntity? {
        challengeDao.getByScenarioAndOrder(scenario, orderIndex + 1)?.let { return it }
        val nextScenario = ScenarioProgression.next(scenario) ?: return null
        return challengeDao.getByScenarioAndOrder(nextScenario, 1)
    }

    fun observeBadges() = badgeDao.observeAll()
    fun observeStamps() = stampDao.observeAll()

    fun scenarioCount(): Int = ScenarioType.values().size
}
