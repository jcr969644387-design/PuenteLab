package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.local.dao.ProgressDao
import com.educalab.puentelab.data.local.dao.UserBadgeDao
import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.data.local.entity.UserProfileEntity
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.ProfileRepository
import com.educalab.puentelab.domain.logic.ChallengeProgress
import com.educalab.puentelab.domain.logic.ProgressEngine
import com.educalab.puentelab.domain.model.LevelInfo
import com.educalab.puentelab.domain.model.ModuleState
import com.educalab.puentelab.domain.model.ScenarioType
import kotlinx.coroutines.flow.*

data class ScenarioSummary(
    val scenario: ScenarioType,
    val locked: Boolean,
    val completed: Int,
    val total: Int,
    val missions: List<ChallengeUiItem> = emptyList()
)

data class AcademyUiState(
    val alias: String = "",
    val avatarId: String = "avatar_casco_naranja",
    val levelInfo: LevelInfo = ProgressEngine.levelInfo(0),
    val badgeCount: Int = 0,
    val totalBadges: Int = 9,
    val nextMission: BridgeChallengeEntity? = null,
    val nextMissionIsNewScenario: Boolean = false,
    val scenarios: List<ScenarioSummary> = emptyList()
)

class AcademyViewModel(
    catalogRepository: CatalogRepository,
    profileRepository: ProfileRepository,
    userBadgeDao: UserBadgeDao,
    progressDao: ProgressDao
) : ViewModel() {

    val uiState: StateFlow<AcademyUiState> = combine(
        profileRepository.observeProfile(),
        catalogRepository.observeBadges(),
        userBadgeDao.observeForUser(UserProfileEntity.LOCAL_USER_ID),
        catalogRepository.observeChallenges(),
        progressDao.observeAll(UserProfileEntity.LOCAL_USER_ID)
    ) { profile, allBadges, unlocked, challenges, progressList ->
        val items = ChallengeProgress.compute(challenges, progressList)
        val byScenario = items.groupBy { it.challenge.scenario }

        val scenarios = byScenario.map { (scenario, list) ->
            val sortedList = list.sortedBy { it.challenge.orderIndex }
            ScenarioSummary(
                scenario = scenario,
                locked = list.isNotEmpty() && list.all { it.state == ModuleState.LOCKED },
                completed = list.count { it.state == ModuleState.COMPLETED || it.state == ModuleState.MASTERED },
                total = list.size,
                missions = sortedList.map { ChallengeUiItem(it.challenge, it.state, it.bestStars) }
            )
        }.sortedBy { com.educalab.puentelab.domain.model.ScenarioProgression.ORDER.indexOf(it.scenario) }

        // Próxima misión: el primer desafío jugable (disponible o ya empezado) siguiendo el
        // orden de escenarios y de nivel; si no queda ninguno, el estudio está al día.
        val playable = items
            .sortedWith(compareBy(
                { com.educalab.puentelab.domain.model.ScenarioProgression.ORDER.indexOf(it.challenge.scenario) },
                { it.challenge.orderIndex }
            ))
            .firstOrNull { it.state == ModuleState.AVAILABLE || it.state == ModuleState.STARTED }
        val isNewScenario = playable != null && playable.challenge.orderIndex == 1

        AcademyUiState(
            alias = profile?.alias ?: "",
            avatarId = profile?.avatarId ?: "avatar_casco_naranja",
            levelInfo = ProgressEngine.levelInfo(profile?.cachedXp ?: 0),
            badgeCount = unlocked.size,
            totalBadges = allBadges.size.coerceAtLeast(1),
            nextMission = playable?.challenge,
            nextMissionIsNewScenario = isNewScenario,
            scenarios = scenarios
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AcademyUiState())
}
