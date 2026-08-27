package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.local.dao.ProgressDao
import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.data.local.entity.UserProfileEntity
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.domain.logic.ChallengeProgress
import com.educalab.puentelab.domain.model.ModuleState
import kotlinx.coroutines.flow.*

data class ChallengeUiItem(val challenge: BridgeChallengeEntity, val state: ModuleState, val bestStars: Int)

class ScenariosViewModel(
    private val catalogRepository: CatalogRepository,
    private val progressDao: ProgressDao
) : ViewModel() {

    val challengeItems: StateFlow<List<ChallengeUiItem>> = combine(
        catalogRepository.observeChallenges(),
        progressDao.observeAll(UserProfileEntity.LOCAL_USER_ID)
    ) { challenges, progressList ->
        ChallengeProgress.compute(challenges, progressList)
            .sortedWith(compareBy({ it.challenge.scenario }, { it.challenge.orderIndex }))
            .map { ChallengeUiItem(it.challenge, it.state, it.bestStars) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
