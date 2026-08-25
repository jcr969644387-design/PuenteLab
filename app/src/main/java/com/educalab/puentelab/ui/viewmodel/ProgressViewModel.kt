package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.local.dao.ProgressDao
import com.educalab.puentelab.data.local.dao.StampDao
import com.educalab.puentelab.data.local.dao.UserBadgeDao
import com.educalab.puentelab.data.local.entity.UserProfileEntity
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.ProfileRepository
import com.educalab.puentelab.domain.logic.ProgressEngine
import com.educalab.puentelab.domain.model.BadgeId
import com.educalab.puentelab.domain.model.LevelInfo
import kotlinx.coroutines.flow.*

data class ProgressUiState(
    val levelInfo: LevelInfo = ProgressEngine.levelInfo(0),
    val unlockedBadgeIds: Set<BadgeId> = emptySet(),
    val unlockedStampIds: Set<String> = emptySet(),
    val completedChallenges: Int = 0,
    val totalChallenges: Int = 0
)

class ProgressViewModel(
    catalogRepository: CatalogRepository,
    progressDao: ProgressDao,
    userBadgeDao: UserBadgeDao,
    stampDao: StampDao,
    profileRepository: ProfileRepository
) : ViewModel() {

    val badges = catalogRepository.observeBadges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stamps = catalogRepository.observeStamps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ProgressUiState> = combine(
        profileRepository.observeProfile(),
        userBadgeDao.observeForUser(UserProfileEntity.LOCAL_USER_ID),
        stampDao.observeUnlockedForUser(UserProfileEntity.LOCAL_USER_ID),
        progressDao.observeAll(UserProfileEntity.LOCAL_USER_ID),
        catalogRepository.observeChallenges()
    ) { profile, badgesUnlocked, stampsUnlocked, progressList, allChallenges ->
        ProgressUiState(
            levelInfo = ProgressEngine.levelInfo(profile?.cachedXp ?: 0),
            unlockedBadgeIds = badgesUnlocked.map { it.badgeId }.toSet(),
            unlockedStampIds = stampsUnlocked.map { it.stampId }.toSet(),
            completedChallenges = progressList.count { it.bestStars > 0 },
            totalChallenges = allChallenges.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState())
}
