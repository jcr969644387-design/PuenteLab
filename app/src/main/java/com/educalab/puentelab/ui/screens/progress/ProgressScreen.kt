package com.educalab.puentelab.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.ui.components.BadgeCard
import com.educalab.puentelab.ui.components.LevelBadgeCircle
import com.educalab.puentelab.ui.components.StampCard
import com.educalab.puentelab.ui.components.XpProgressBar
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.ProgressViewModel

@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val stamps by viewModel.stamps.collectAsStateWithLifecycle()

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Tu Progreso", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                LevelBadgeCircle(level = state.levelInfo.level)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    XpProgressBar(progress = state.levelInfo.progressToNextLevel, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text("${state.levelInfo.currentXp} XP", style = MaterialTheme.typography.bodyMedium, color = Ink600)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${state.completedChallenges} / ${state.totalChallenges} desafíos aprobados",
                style = MaterialTheme.typography.bodyMedium, color = Ink600
            )

            Spacer(Modifier.height(24.dp))
            Text("Insignias", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(badges) { badge ->
                    BadgeCard(
                        name = badge.name, description = badge.description,
                        unlocked = badge.id in state.unlockedBadgeIds
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Sellos de Constructor", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(stamps) { stamp ->
                    StampCard(
                        name = stamp.name, unlocked = stamp.id in state.unlockedStampIds,
                        accentColor = SiteAmber
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
