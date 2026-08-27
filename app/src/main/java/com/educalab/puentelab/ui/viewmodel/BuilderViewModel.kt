package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.local.AppPreferences
import com.educalab.puentelab.data.local.entity.VehicleEntity
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.DesignRepository
import com.educalab.puentelab.data.repository.ProfileRepository
import com.educalab.puentelab.data.repository.SaveDesignResult
import com.educalab.puentelab.data.repository.SimulationRepository
import com.educalab.puentelab.data.repository.toDomain
import com.educalab.puentelab.domain.logic.ProgressEngine
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
    val transientMessage: String? = null,
    val playerLevel: Int = 1,
    val testVehicle: VehicleEntity? = null,
    val autoShowInstructions: Boolean = false,
    val scenarioInfo: ScenarioEducationInfo? = null,
    val missingRequiredRoles: Set<MemberRole> = emptySet(),
    val missionConstraint: MissionConstraint? = null,
    val vehicleCount: Int = 1,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val nextChallengeId: String? = null,
    val nextChallengeIsNewScenario: Boolean = false
)

class BuilderViewModel(
    private val catalogRepository: CatalogRepository,
    private val designRepository: DesignRepository,
    private val simulationRepository: SimulationRepository,
    private val profileRepository: ProfileRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    fun loadChallenge(challengeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val challenge = catalogRepository.getChallenge(challengeId)?.toDomain()
            val draft = designRepository.getOrCreateDraft(challengeId)
            val seededDesign = if (draft.nodes.isEmpty() && challenge != null) seedBanks(draft, challenge) else draft
            val alreadySeenInstructions = appPreferences.hasSeenBuilderInstructionsOnce()
            applyLoadedState(challenge, seededDesign, autoShowInstructions = !alreadySeenInstructions)
        }
        observeMaterials()
    }

    /**
     * Abre un diseño ya guardado desde "Mis Diseños" con su estructura exacta (nodos, barras,
     * materiales de cada pieza) tal como quedó al guardarlo, en vez de crear un borrador nuevo
     * del desafío. El escenario y la misión se resuelven a partir de design.challengeId.
     */
    fun loadSavedDesign(designId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val design = designRepository.getDesign(designId) ?: return@launch
            val challenge = catalogRepository.getChallenge(design.challengeId)?.toDomain()
            applyLoadedState(challenge, design, autoShowInstructions = false)
        }
        observeMaterials()
    }

    private fun observeMaterials() {
        viewModelScope.launch {
            catalogRepository.observeMaterials().collect { list ->
                _uiState.value = _uiState.value.copy(materials = list.map { it.toDomain() })
                if (_uiState.value.selectedMaterialId == null && list.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(selectedMaterialId = list.first().id)
                }
            }
        }
    }

    private suspend fun applyLoadedState(challenge: BridgeChallengeSpec?, design: BridgeDesignSpec, autoShowInstructions: Boolean) {
        val profile = profileRepository.getOrCreateProfile()
        val testVehicle = challenge?.let { catalogRepository.getDefaultVehicleForScenario(it.scenario) }

        _uiState.value = _uiState.value.copy(
            loading = false,
            challenge = challenge,
            design = design,
            selectedMaterialId = _uiState.value.selectedMaterialId,
            playerLevel = ProgressEngine.levelInfo(profile.cachedXp).level,
            testVehicle = testVehicle,
            autoShowInstructions = autoShowInstructions,
            scenarioInfo = challenge?.let { ScenarioEducation.byScenario[it.scenario] },
            missionConstraint = challenge?.let { MissionConstraints.byChallengeId[it.id] },
            vehicleCount = challenge?.let { MissionVehicles.countFor(it.orderIndex) } ?: 1,
            soundEnabled = profile.soundEnabled,
            hapticEnabled = profile.hapticEnabled,
            nextChallengeId = null
        )
        recomputeCost()
    }

    /** Marca el tutorial como visto para que no se vuelva a abrir solo en el próximo desafío. */
    fun markInstructionsSeen() {
        if (!_uiState.value.autoShowInstructions) return
        _uiState.value = _uiState.value.copy(autoShowInstructions = false)
        viewModelScope.launch { appPreferences.markBuilderInstructionsSeen() }
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

        val scenario = state.challenge?.scenario
        val required = scenario?.let { ScenarioRequirements.requiredRoles[it] }.orEmpty()
        val present = state.design.members.map { it.role }.toSet()
        val missing = required - present

        _uiState.value = state.copy(liveCost = cost, missingRequiredRoles = missing)
    }

    private fun persistDraft() {
        viewModelScope.launch { designRepository.updateStructure(_uiState.value.design) }
    }

    /** Prueba el diseño con el vehículo propio del escenario del desafío (no uno genérico). */
    fun runSimulation() {
        val state = _uiState.value
        val challenge = state.challenge ?: return
        val vehicle = state.testVehicle
        viewModelScope.launch {
            val outcome = simulationRepository.runSimulation(
                state.design, challenge,
                vehicleId = vehicle?.id ?: "van_explorer",
                vehicleWeightMultiplier = vehicle?.weightMultiplier ?: 1.0
            )
            val next = if (outcome.result.passed) {
                catalogRepository.getNextChallenge(challenge.scenario, challenge.orderIndex)
            } else null
            _uiState.value = _uiState.value.copy(
                lastResult = outcome.result, showResult = true,
                nextChallengeId = next?.id,
                nextChallengeIsNewScenario = next != null && next.scenario != challenge.scenario
            )
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

    suspend fun suggestedDesignName(): String = designRepository.suggestedDesignName()
}
