package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.DesignRepository
import com.educalab.puentelab.data.repository.MAX_SAVED_DESIGNS
import com.educalab.puentelab.data.repository.SaveDesignResult
import com.educalab.puentelab.data.repository.toDomain
import com.educalab.puentelab.domain.model.BridgeDesignSpec
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Un diseño guardado listo para mostrar en "Mis Diseños": estructura completa + a qué desafío pertenece. */
data class SavedDesignUiItem(
    val design: BridgeDesignSpec,
    val challenge: BridgeChallengeEntity?,
    val updatedAt: Long
)

class DesignsViewModel(
    private val designRepository: DesignRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    val savedDesigns: StateFlow<List<SavedDesignUiItem>> = combine(
        designRepository.observeSavedDesigns(),
        catalogRepository.observeChallenges()
    ) { designs, challenges ->
        val challengesById = challenges.associateBy { it.id }
        designs
            .map { entry ->
                SavedDesignUiItem(
                    design = entry.toDomain(),
                    challenge = challengesById[entry.design.challengeId],
                    updatedAt = entry.design.updatedAt
                )
            }
            .sortedByDescending { it.updatedAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maxDesigns = MAX_SAVED_DESIGNS

    fun duplicate(designId: String, newName: String, onResult: (SaveDesignResult) -> Unit) {
        viewModelScope.launch { onResult(designRepository.duplicate(designId, newName)) }
    }

    fun rename(designId: String, newName: String, onResult: (SaveDesignResult) -> Unit) {
        viewModelScope.launch { onResult(designRepository.rename(designId, newName)) }
    }

    fun delete(designId: String) {
        viewModelScope.launch { designRepository.delete(designId) }
    }
}
