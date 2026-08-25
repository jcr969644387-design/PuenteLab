package com.educalab.puentelab.data.seed

import com.educalab.puentelab.data.local.PuenteLabDatabase

/**
 * Puebla la base de datos en el primer arranque. Es idempotente: cada tabla revisa su propio
 * conteo antes de insertar, así que ejecutar el seeder varias veces (p. ej. tras un proceso
 * interrumpido) nunca duplica filas.
 */
class DatabaseSeeder(private val db: PuenteLabDatabase) {

    suspend fun seedIfNeeded() {
        if (db.materialDao().count() == 0) db.materialDao().insertAll(SeedMaterials.all)
        if (db.vehicleDao().count() == 0) db.vehicleDao().insertAll(SeedVehicles.all)
        if (db.challengeDao().count() == 0) db.challengeDao().insertAll(SeedChallenges.buildAll())
        if (db.badgeDao().count() == 0) db.badgeDao().insertAll(SeedBadges.all)
        if (db.stampDao().count() == 0) db.stampDao().insertAll(SeedStamps.all)
    }
}
