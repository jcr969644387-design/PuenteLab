package com.educalab.puentelab.util

import android.content.Context
import com.educalab.puentelab.data.local.AppPreferences
import com.educalab.puentelab.data.local.PuenteLabDatabase
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.DesignRepository
import com.educalab.puentelab.data.repository.ProfileRepository
import com.educalab.puentelab.data.repository.SimulationRepository
import com.educalab.puentelab.data.seed.DatabaseSeeder

/**
 * Contenedor manual de dependencias. Este proyecto no usa Hilt/Dagger para mantener el stack
 * mínimo indicado en el encargo; con ~14 entidades y 4 repositorios, una fábrica manual es
 * suficiente y más fácil de auditar.
 */
class AppContainer(context: Context) {
    val database: PuenteLabDatabase = PuenteLabDatabase.getInstance(context)
    val seeder = DatabaseSeeder(database)
    val appPreferences = AppPreferences(context)

    val profileRepository = ProfileRepository(database.userProfileDao())
    val catalogRepository = CatalogRepository(
        database.materialDao(), database.vehicleDao(), database.challengeDao(),
        database.badgeDao(), database.stampDao()
    )
    val designRepository = DesignRepository(database.designDao())
    val simulationRepository = SimulationRepository(
        database.simulationRunDao(), database.simulationResultDao(), database.progressDao(),
        database.userBadgeDao(), database.stampDao(), database.userProfileDao(), database.materialDao()
    )
}
