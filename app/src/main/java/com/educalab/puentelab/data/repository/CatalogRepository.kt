package com.educalab.puentelab.data.repository

import com.educalab.puentelab.data.local.dao.*
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

    fun observeChallenges() = challengeDao.observeAll()
    fun observeChallengesByScenario(scenario: ScenarioType) = challengeDao.observeByScenario(scenario)
    suspend fun getChallenge(id: String) = challengeDao.getById(id)

    fun observeBadges() = badgeDao.observeAll()
    fun observeStamps() = stampDao.observeAll()

    fun scenarioCount(): Int = ScenarioType.values().size
}
