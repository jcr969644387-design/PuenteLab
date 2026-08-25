package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.local.dao.ProgressDao
import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.data.local.entity.UserProfileEntity
import com.educalab.puentelab.data.repository.CatalogRepository
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
        val progressByChallenge = progressList.associateBy { it.challengeId }
        val sorted = challenges.sortedWith(compareBy({ it.scenario }, { it.orderIndex }))
        val byScenarioOrder = sorted.groupBy { it.scenario }
        buildList {
            for ((_, list) in byScenarioOrder) {
                list.forEachIndexed { index, challenge ->
                    val progress = progressByChallenge[challenge.id]
                    val previousCompleted = index == 0 || run {
                        val prev = list[index - 1]
                        val prevProgress = progressByChallenge[prev.id]
                        prevProgress != null && (prevProgress.state == ModuleState.COMPLETED || prevProgress.state == ModuleState.MASTERED)
                    }
                    val state = when {
                        progress != null -> progress.state
                        previousCompleted -> ModuleState.AVAILABLE
                        else -> ModuleState.LOCKED
                    }
                    add(ChallengeUiItem(challenge, state, progress?.bestStars ?: 0))
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
