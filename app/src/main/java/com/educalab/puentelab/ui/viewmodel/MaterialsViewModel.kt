package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.repository.CatalogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MaterialsViewModel(catalogRepository: CatalogRepository) : ViewModel() {
    val materials = catalogRepository.observeMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicles = catalogRepository.observeVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
