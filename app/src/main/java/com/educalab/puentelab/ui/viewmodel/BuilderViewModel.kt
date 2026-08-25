package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.DesignRepository
import com.educalab.puentelab.data.repository.SaveDesignResult
import com.educalab.puentelab.data.repository.SimulationRepository
import com.educalab.puentelab.data.repository.toDomain
import com.educalab.puentelab.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class BuilderUiState(
    val loading: Boolean = true,
    val challenge: BridgeChallengeSpec? = null,
    val design: BridgeDesignSpec = BridgeDesignSpec("", "", "Borrador", emptyList(), emptyList()),
    val materials: List<MaterialSpec> = emptyList(),
    val selectedMaterialId: String? = null,
    val selectedRole: MemberRole = MemberRole.DECK,
    val selectedStructureType: StructureType = StructureType.BEAM,
    val pierMode: Boolean = false,
    val pendingNodeId: String? = null,
    val liveCost: Double = 0.0,
    val lastResult: SimulationResult? = null,
    val showResult: Boolean = false,
    val transientMessage: String? = null
)

class BuilderViewModel(
    private val catalogRepository: CatalogRepository,
    private val designRepository: DesignRepository,
    private val simulationRepository: SimulationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    fun loadChallenge(challengeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val challenge = catalogRepository.getChallenge(challengeId)?.toDomain()
            val draft = designRepository.getOrCreateDraft(challengeId)
            val seededDesign = if (draft.nodes.isEmpty() && challenge != null) seedBanks(draft, challenge) else draft

            _uiState.value = _uiState.value.copy(
                loading = false,
                challenge = challenge,
                design = seededDesign,
                selectedMaterialId = _uiState.value.selectedMaterialId
            )
            recomputeCost()
        }
        viewModelScope.launch {
            catalogRepository.observeMaterials().collect { list ->
                _uiState.value = _uiState.value.copy(materials = list.map { it.toDomain() })
                if (_uiState.value.selectedMaterialId == null && list.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(selectedMaterialId = list.first().id)
                }
            }
        }
    }

    /** Coloca los nodos de orilla fijos (izquierda/derecha) y los apoyos gratuitos del nivel. */
    private fun seedBanks(draft: BridgeDesignSpec, challenge: BridgeChallengeSpec): BridgeDesignSpec {
        val leftNode = BridgeNode(UUID.randomUUID().toString(), challenge.leftBank, AnchorSide.LEFT, isFixedByLevel = true)
        val rightNode = BridgeNode(UUID.randomUUID().toString(), challenge.rightBank, AnchorSide.RIGHT, isFixedByLevel = true)
        val supportNodes = challenge.fixedSupports.map {
            BridgeNode(UUID.randomUUID().toString(), it, AnchorSide.NONE, isFixedByLevel = true)
        }
        return draft.copy(nodes = listOf(leftNode, rightNode) + supportNodes)
    }

    fun selectMaterial(id: String) {
        _uiState.value = _uiState.value.copy(selectedMaterialId = id)
    }

    fun selectRole(role: MemberRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun selectStructureType(type: StructureType) {
        _uiState.value = _uiState.value.copy(selectedStructureType = type)
    }

    fun togglePierMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pierMode = enabled)
    }

    /** Coloca un nodo libre (o un apoyo de pago si pierMode está activo) en la posición indicada. */
    fun placeFreeNode(point: GridPoint) {
        val state = _uiState.value
        val tooClose = state.design.nodes.any { it.point.distanceTo(point) < 0.4 }
        if (tooClose) return
        val newNode = BridgeNode(
            id = UUID.randomUUID().toString(), point = point,
            anchorSide = AnchorSide.NONE, isFixedByLevel = false, isUserPier = state.pierMode
        )
        val newDesign = state.design.copy(nodes = state.design.nodes + newNode)
        _uiState.value = state.copy(design = newDesign)
        persistDraft()
        recomputeCost()
    }

    /** Maneja el toque sobre un nodo existente: primer toque selecciona, segundo toque crea una barra. */
    fun tapNode(nodeId: String) {
        val state = _uiState.value
        val pending = state.pendingNodeId
        if (pending == null) {
            _uiState.value = state.copy(pendingNodeId = nodeId)
            return
        }
        if (pending == nodeId) {
            _uiState.value = state.copy(pendingNodeId = null) // deseleccionar
            return
        }
        val materialId = state.selectedMaterialId ?: return
        val alreadyExists = state.design.members.any {
            (it.nodeAId == pending && it.nodeBId == nodeId) || (it.nodeAId == nodeId && it.nodeBId == pending)
        }
        if (alreadyExists) {
            _uiState.value = state.copy(pendingNodeId = null)
            return
        }
        val newMember = BridgeMember(
            id = UUID.randomUUID().toString(), nodeAId = pending, nodeBId = nodeId,
            materialId = materialId, role = state.selectedRole, structureType = state.selectedStructureType
        )
        val newDesign = state.design.copy(members = state.design.members + newMember)
        _uiState.value = state.copy(design = newDesign, pendingNodeId = null)
        persistDraft()
        recomputeCost()
    }

    fun removeMember(memberId: String) {
        val state = _uiState.value
        val newDesign = state.design.copy(members = state.design.members.filterNot { it.id == memberId })
        _uiState.value = state.copy(design = newDesign)
        persistDraft()
        recomputeCost()
    }

    fun removeFreeNode(nodeId: String) {
        val state = _uiState.value
        val node = state.design.nodes.firstOrNull { it.id == nodeId } ?: return
        if (node.isFixedByLevel) return // no se pueden borrar orillas ni apoyos del nivel
        val newDesign = state.design.copy(
            nodes = state.design.nodes.filterNot { it.id == nodeId },
            members = state.design.members.filterNot { it.nodeAId == nodeId || it.nodeBId == nodeId }
        )
        _uiState.value = state.copy(design = newDesign, pendingNodeId = null)
        persistDraft()
        recomputeCost()
    }

    fun clearAll() {
        val state = _uiState.value
        val challenge = state.challenge ?: return
        val cleared = seedBanks(state.design.copy(members = emptyList()), challenge)
        _uiState.value = state.copy(design = cleared, pendingNodeId = null)
        persistDraft()
        recomputeCost()
    }

    private fun recomputeCost() {
        val state = _uiState.value
        val materialsMap = state.materials.associateBy { it.id }
        val nodesById = state.design.nodes.associateBy { it.id }
        var cost = 0.0
        for (m in state.design.members) {
            val a = nodesById[m.nodeAId]?.point
            val b = nodesById[m.nodeBId]?.point
            val mat = materialsMap[m.materialId]
            if (a != null && b != null && mat != null) cost += a.distanceTo(b) * mat.costPerUnit
        }
        cost += state.design.nodes.count { it.isUserPier } * 35.0
        _uiState.value = state.copy(liveCost = cost)
    }

    private fun persistDraft() {
        viewModelScope.launch { designRepository.updateStructure(_uiState.value.design) }
    }

    fun runSimulation(vehicleId: String, vehicleWeightMultiplier: Double) {
        val state = _uiState.value
        val challenge = state.challenge ?: return
        viewModelScope.launch {
            val outcome = simulationRepository.runSimulation(state.design, challenge, vehicleId, vehicleWeightMultiplier)
            _uiState.value = _uiState.value.copy(lastResult = outcome.result, showResult = true)
        }
    }

    fun dismissResult() {
        _uiState.value = _uiState.value.copy(showResult = false)
    }

    fun saveDesign(name: String, onResult: (SaveDesignResult) -> Unit) {
        viewModelScope.launch {
            val result = designRepository.saveToMyDesigns(_uiState.value.design.id, name)
            onResult(result)
        }
    }
}
