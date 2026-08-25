package com.educalab.puentelab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.puentelab.data.repository.ProfileRepository
import com.educalab.puentelab.domain.logic.ProgressEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    val profile = repository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeOnboarding(alias: String, avatarId: String) {
        viewModelScope.launch { repository.completeOnboarding(alias.ifBlank { "Ingeniero/a Nuevo/a" }, avatarId) }
    }

    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { repository.setSoundEnabled(enabled) }
    fun setHapticEnabled(enabled: Boolean) = viewModelScope.launch { repository.setHapticEnabled(enabled) }

    fun levelInfo(xp: Int) = ProgressEngine.levelInfo(xp)
}
