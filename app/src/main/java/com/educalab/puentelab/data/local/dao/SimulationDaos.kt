package com.educalab.puentelab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.puentelab.data.local.entity.SimulationResultEntity
import com.educalab.puentelab.data.local.entity.SimulationRunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SimulationRunDao {
    @Query("SELECT * FROM simulation_runs WHERE challengeId = :challengeId ORDER BY ranAt ASC")
    fun observeForChallenge(challengeId: String): Flow<List<SimulationRunEntity>>

    @Query("SELECT * FROM simulation_runs ORDER BY ranAt ASC")
    fun observeAll(): Flow<List<SimulationRunEntity>>

    @Query("SELECT * FROM simulation_runs WHERE challengeId = :challengeId ORDER BY ranAt DESC LIMIT 1")
    suspend fun getLatestForChallenge(challengeId: String): SimulationRunEntity?

    @Query("SELECT COUNT(*) FROM simulation_runs WHERE challengeId = :challengeId")
    suspend fun countAttemptsForChallenge(challengeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: SimulationRunEntity)
}

@Dao
interface SimulationResultDao {
    @Query("SELECT * FROM simulation_results WHERE simulationRunId = :runId")
    suspend fun getForRun(runId: String): List<SimulationResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<SimulationResultEntity>)
}
