package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.DesignRepository
import com.educalab.puentelab.data.repository.MAX_SAVED_DESIGNS
import com.educalab.puentelab.data.repository.SaveDesignResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DesignsViewModel(
    private val designRepository: DesignRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    val savedDesigns = designRepository.observeSavedDesigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maxDesigns = MAX_SAVED_DESIGNS

    fun duplicate(designId: String, newName: String, onResult: (SaveDesignResult) -> Unit) {
        viewModelScope.launch { onResult(designRepository.duplicate(designId, newName)) }
    }

    fun delete(designId: String) {
        viewModelScope.launch { designRepository.delete(designId) }
    }

    suspend fun challengeName(challengeId: String): String =
        catalogRepository.getChallenge(challengeId)?.name ?: "Desafío"
}
