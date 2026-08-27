package com.educalab.puentelab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.domain.model.ScenarioType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM bridge_challenges ORDER BY scenario, orderIndex ASC")
    fun observeAll(): Flow<List<BridgeChallengeEntity>>

    @Query("SELECT * FROM bridge_challenges WHERE scenario = :scenario ORDER BY orderIndex ASC")
    fun observeByScenario(scenario: ScenarioType): Flow<List<BridgeChallengeEntity>>

    @Query("SELECT * FROM bridge_challenges WHERE id = :id")
    suspend fun getById(id: String): BridgeChallengeEntity?

    @Query("SELECT * FROM bridge_challenges WHERE scenario = :scenario AND orderIndex = :orderIndex LIMIT 1")
    suspend fun getByScenarioAndOrder(scenario: ScenarioType, orderIndex: Int): BridgeChallengeEntity?

    @Query("SELECT COUNT(*) FROM bridge_challenges")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<BridgeChallengeEntity>)
}
