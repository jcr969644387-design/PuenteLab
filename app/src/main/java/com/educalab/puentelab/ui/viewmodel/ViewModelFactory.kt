package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.educalab.puentelab.util.AppContainer

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ProfileViewModel::class.java -> ProfileViewModel(container.profileRepository) as T
            AcademyViewModel::class.java -> AcademyViewModel(container.catalogRepository, container.profileRepository, container.database.userBadgeDao()) as T
            ScenariosViewModel::class.java -> ScenariosViewModel(container.catalogRepository, container.database.progressDao()) as T
            MaterialsViewModel::class.java -> MaterialsViewModel(container.catalogRepository) as T
            BuilderViewModel::class.java -> BuilderViewModel(container.catalogRepository, container.designRepository, container.simulationRepository) as T
            DesignsViewModel::class.java -> DesignsViewModel(container.designRepository, container.catalogRepository) as T
            ProgressViewModel::class.java -> ProgressViewModel(container.catalogRepository, container.database.progressDao(), container.database.userBadgeDao(), container.database.stampDao(), container.profileRepository) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: $modelClass")
        }
    }
}
