package com.educalab.puentelab.data.repository

import com.educalab.puentelab.data.local.dao.DesignDao
import com.educalab.puentelab.data.local.entity.BridgeDesignEntity
import com.educalab.puentelab.data.local.entity.UserProfileEntity
import com.educalab.puentelab.domain.model.BridgeDesignSpec
import com.educalab.puentelab.domain.model.BridgeMember
import com.educalab.puentelab.domain.model.BridgeNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/** Un espacio de diseño guardado por cada nivel del juego (9 niveles x 5 escenarios). */
const val MAX_SAVED_DESIGNS = 45

sealed class SaveDesignResult {
    data class Success(val designId: String) : SaveDesignResult()
    object LimitReached : SaveDesignResult()
}

class DesignRepository(
    private val dao: DesignDao,
    private val userId: String = UserProfileEntity.LOCAL_USER_ID
) {
    fun observeSavedDesigns(): Flow<List<BridgeDesignSpec>> =
        dao.observeSavedDesigns(userId).map { list -> list.map { it.toDomain() } }

    suspend fun getOrCreateDraft(challengeId: String): BridgeDesignSpec {
        val existing = dao.getDraftForChallenge(challengeId, userId)
        val designId = existing?.id ?: UUID.randomUUID().toString()
        if (existing == null) {
            val now = System.currentTimeMillis()
            dao.insertDesign(
                BridgeDesignEntity(
                    id = designId, challengeId = challengeId, userProfileId = userId,
                    name = "Borrador", createdAt = now, updatedAt = now, isSaved = false
                )
            )
        }
        return dao.getDesignWithStructure(designId)?.toDomain()
            ?: BridgeDesignSpec(designId, challengeId, "Borrador", emptyList(), emptyList())
    }

    /** Reemplaza nodos/barras del borrador actual (autoguardado mientras el jugador construye). */
    suspend fun updateStructure(design: BridgeDesignSpec) {
        val existing = dao.getDesign(design.id) ?: return
        val now = System.currentTimeMillis()
        dao.replaceStructure(
            existing.copy(updatedAt = now),
            design.nodes.map { it.toEntity(design.id) },
            design.members.map { it.toEntity(design.id) }
        )
    }

    /** "Mi Puente", "Mi Puente 2", "Mi Puente 3"... el primer nombre libre, sin repetir. */
    suspend fun suggestedDesignName(): String {
        val existingNames = dao.observeSavedDesigns(userId).first().map { it.design.name }.toSet()
        if ("Mi Puente" !in existingNames) return "Mi Puente"
        var n = 2
        while ("Mi Puente $n" in existingNames) n++
        return "Mi Puente $n"
    }

    suspend fun saveToMyDesigns(designId: String, name: String): SaveDesignResult {
        val currentCount = dao.countSavedDesigns(userId)
        val design = dao.getDesign(designId)
        val alreadySaved = design?.isSaved == true
        if (!alreadySaved && currentCount >= MAX_SAVED_DESIGNS) return SaveDesignResult.LimitReached
        dao.rename(designId, name, System.currentTimeMillis())
        dao.setSaved(designId, true, System.currentTimeMillis())
        return SaveDesignResult.Success(designId)
    }

    suspend fun duplicate(designId: String, newName: String): SaveDesignResult {
        val source = dao.getDesignWithStructure(designId) ?: return SaveDesignResult.LimitReached
        if (dao.countSavedDesigns(userId) >= MAX_SAVED_DESIGNS) return SaveDesignResult.LimitReached
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val idMap = source.nodes.associate { it.id to UUID.randomUUID().toString() }
        val newNodes = source.nodes.map { it.copy(id = idMap.getValue(it.id), designId = newId) }
        val newMembers = source.members.map {
            it.copy(
                id = UUID.randomUUID().toString(), designId = newId,
                nodeAId = idMap.getValue(it.nodeAId), nodeBId = idMap.getValue(it.nodeBId)
            )
        }
        dao.replaceStructure(
            BridgeDesignEntity(newId, source.design.challengeId, userId, newName, now, now, isSaved = true, duplicatedFromId = designId),
            newNodes, newMembers
        )
        return SaveDesignResult.Success(newId)
    }

    suspend fun delete(designId: String) = dao.deleteDesign(designId)

    suspend fun getDesign(designId: String): BridgeDesignSpec? = dao.getDesignWithStructure(designId)?.toDomain()
}
