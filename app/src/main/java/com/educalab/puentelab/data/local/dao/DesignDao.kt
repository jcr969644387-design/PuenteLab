package com.educalab.puentelab.data.local.dao

import androidx.room.*
import com.educalab.puentelab.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DesignDao {

    @Query("SELECT * FROM bridge_designs WHERE id = :id")
    suspend fun getDesign(id: String): BridgeDesignEntity?

    @Transaction
    @Query("SELECT * FROM bridge_designs WHERE id = :id")
    suspend fun getDesignWithStructure(id: String): DesignWithStructure?

    @Transaction
    @Query("SELECT * FROM bridge_designs WHERE id = :id")
    fun observeDesignWithStructure(id: String): Flow<DesignWithStructure?>

    @Transaction
    @Query("SELECT * FROM bridge_designs WHERE userProfileId = :userId AND isSaved = 1 ORDER BY updatedAt DESC")
    fun observeSavedDesigns(userId: String): Flow<List<DesignWithStructure>>

    @Query("SELECT COUNT(*) FROM bridge_designs WHERE userProfileId = :userId AND isSaved = 1")
    suspend fun countSavedDesigns(userId: String): Int

    @Query("SELECT * FROM bridge_designs WHERE challengeId = :challengeId AND userProfileId = :userId AND isSaved = 0 LIMIT 1")
    suspend fun getDraftForChallenge(challengeId: String, userId: String): BridgeDesignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesign(design: BridgeDesignEntity)

    @Query("DELETE FROM bridge_nodes WHERE designId = :designId")
    suspend fun clearNodes(designId: String)

    @Query("DELETE FROM bridge_members WHERE designId = :designId")
    suspend fun clearMembers(designId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<BridgeNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<BridgeMemberEntity>)

    /** Reemplaza atómicamente nodos y barras de un diseño (usado al guardar el constructor). */
    @Transaction
    suspend fun replaceStructure(design: BridgeDesignEntity, nodes: List<BridgeNodeEntity>, members: List<BridgeMemberEntity>) {
        insertDesign(design)
        clearNodes(design.id)
        clearMembers(design.id)
        insertNodes(nodes)
        insertMembers(members)
    }

    @Query("DELETE FROM bridge_designs WHERE id = :id")
    suspend fun deleteDesign(id: String)

    @Query("UPDATE bridge_designs SET isSaved = :saved, updatedAt = :now WHERE id = :id")
    suspend fun setSaved(id: String, saved: Boolean, now: Long)

    @Query("UPDATE bridge_designs SET name = :name, updatedAt = :now WHERE id = :id")
    suspend fun rename(id: String, name: String, now: Long)
}
