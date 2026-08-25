package com.educalab.puentelab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.educalab.puentelab.data.local.converters.Converters
import com.educalab.puentelab.data.local.dao.*
import com.educalab.puentelab.data.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        MaterialEntity::class,
        BridgeChallengeEntity::class,
        BridgeDesignEntity::class,
        BridgeNodeEntity::class,
        BridgeMemberEntity::class,
        VehicleEntity::class,
        SimulationRunEntity::class,
        SimulationResultEntity::class,
        BuilderStampEntity::class,
        UserStampEntity::class,
        ProgressEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PuenteLabDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun materialDao(): MaterialDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun designDao(): DesignDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun simulationRunDao(): SimulationRunDao
    abstract fun simulationResultDao(): SimulationResultDao
    abstract fun progressDao(): ProgressDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userBadgeDao(): UserBadgeDao
    abstract fun stampDao(): StampDao

    companion object {
        @Volatile private var INSTANCE: PuenteLabDatabase? = null

        fun getInstance(context: Context): PuenteLabDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PuenteLabDatabase::class.java,
                    "puentelab.db"
                ).build().also { INSTANCE = it }
            }
    }
}
