package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.local.dao.UserBadgeDao
import com.educalab.puentelab.data.repository.CatalogRepository
import com.educalab.puentelab.data.repository.ProfileRepository
import com.educalab.puentelab.domain.logic.ProgressEngine
import com.educalab.puentelab.domain.model.LevelInfo
import kotlinx.coroutines.flow.*

data class AcademyUiState(
    val alias: String = "",
    val avatarId: String = "avatar_casco_naranja",
    val levelInfo: LevelInfo = ProgressEngine.levelInfo(0),
    val badgeCount: Int = 0,
    val totalBadges: Int = 9
)

class AcademyViewModel(
    catalogRepository: CatalogRepository,
    profileRepository: ProfileRepository,
    userBadgeDao: UserBadgeDao
) : ViewModel() {

    val uiState: StateFlow<AcademyUiState> = combine(
        profileRepository.observeProfile(),
        catalogRepository.observeBadges(),
        userBadgeDao.observeForUser()
    ) { profile, allBadges, unlocked ->
        AcademyUiState(
            alias = profile?.alias ?: "",
            avatarId = profile?.avatarId ?: "avatar_casco_naranja",
            levelInfo = ProgressEngine.levelInfo(profile?.cachedXp ?: 0),
            badgeCount = unlocked.size,
            totalBadges = allBadges.size.coerceAtLeast(1)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AcademyUiState())

    val challenges = catalogRepository.observeChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
