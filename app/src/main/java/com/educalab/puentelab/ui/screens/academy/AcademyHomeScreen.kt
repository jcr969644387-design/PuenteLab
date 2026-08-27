package com.educalab.puentelab.ui.screens.academy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.data.seed.AvatarCatalog
import com.educalab.puentelab.domain.model.ScenarioEducation
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.ui.components.AvatarPortrait
import com.educalab.puentelab.ui.components.ScenarioScene
import com.educalab.puentelab.ui.components.XpProgressBar
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.AcademyViewModel
import com.educalab.puentelab.ui.viewmodel.ScenarioSummary

@Composable
fun AcademyHomeScreen(
    viewModel: AcademyViewModel,
    onOpenScenarios: () -> Unit,
    onOpenMaterials: () -> Unit,
    onOpenDesigns: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    onContinueChallenge: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val avatar = AvatarCatalog.all.firstOrNull { it.id == state.avatarId } ?: AvatarCatalog.all.first()

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // Encabezado: avatar del jugador + ajustes
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(Blueprint100).clickable(onClick = onOpenSettings)
                ) {
                    AvatarPortrait(avatar, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Estudio PuenteLab", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
                    Text(
                        if (state.alias.isNotBlank()) "Bienvenido/a, ${state.alias}" else "Bienvenido/a",
                        style = MaterialTheme.typography.bodyLarge, color = Ink600
                    )
                }
                LevelBadgeSmall(level = state.levelInfo.level)
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes", tint = Ink600)
                }
            }
            Spacer(Modifier.height(8.dp))
            XpProgressBar(progress = state.levelInfo.progressToNextLevel, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text(
                if (state.levelInfo.xpForNextLevel != null)
                    "${state.levelInfo.currentXp} XP · faltan ${(state.levelInfo.xpForNextLevel!! - state.levelInfo.currentXp)} para el nivel ${state.levelInfo.level + 1}"
                else "${state.levelInfo.currentXp} XP · ¡nivel máximo alcanzado!",
                style = MaterialTheme.typography.bodyMedium, color = Ink600
            )

            Spacer(Modifier.height(20.dp))

            // Misión actual: la primera disponible según el orden de escenarios
            val mission = state.nextMission
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blueprint700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (state.nextMissionIsNewScenario) "¡Nuevo escenario!" else "Tu misión actual",
                        style = MaterialTheme.typography.labelLarge, color = SiteAmber
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        mission?.let { "${it.scenario.displayName} · Misión ${it.orderIndex}" } ?: "¡Completaste todo el estudio!",
                        style = MaterialTheme.typography.titleLarge, color = White
                    )
                    Spacer(Modifier.height(14.dp))
                    if (mission != null) {
                        Button(
                            onClick = { onContinueChallenge(mission.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SiteOrange)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Continuar misión")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Escenarios", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(12.dp))
            state.scenarios.forEach { summary ->
                ScenarioHomeCard(summary, onClick = { if (!summary.locked) onOpenScenarios() })
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(14.dp))
            Text("Explora el estudio", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 180.dp)
            ) {
                items(moduleCards()) { module ->
                    ModuleCard(module) {
                        when (module.id) {
                            "materials" -> onOpenMaterials()
                            "designs" -> onOpenDesigns()
                            "progress" -> onOpenProgress()
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Insignias: ${state.badgeCount} / ${state.totalBadges}",
                style = MaterialTheme.typography.bodyMedium, color = Ink600
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LevelBadgeSmall(level: Int) {
    Box(
        Modifier.size(36.dp).clip(CircleShape).background(Blueprint700),
        contentAlignment = Alignment.Center
    ) {
        Text("Nv.$level", color = White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ScenarioHomeCard(summary: ScenarioSummary, onClick: () -> Unit) {
    val info = ScenarioEducation.byScenario[summary.scenario]
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(88.dp).clickable(enabled = !summary.locked, onClick = onClick)
    ) {
        Box(Modifier.fillMaxSize()) {
            ScenarioScene(summary.scenario, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Blueprint900.copy(alpha = if (summary.locked) 0.65f else 0.4f)))
            Row(
                Modifier.fillMaxSize().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(summary.scenario.displayName, style = MaterialTheme.typography.titleMedium, color = White)
                    Text(
                        if (summary.locked) "Bloqueado" else "${summary.completed} / ${summary.total} · ${info?.difficultyLabel ?: ""}",
                        style = MaterialTheme.typography.bodySmall, color = Blueprint100
                    )
                }
                Icon(
                    if (summary.locked) Icons.Filled.Lock else Icons.Filled.ChevronRight,
                    contentDescription = null, tint = White
                )
            }
        }
    }
}

private data class ModuleCardData(val id: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

private fun moduleCards() = listOf(
    ModuleCardData("materials", "Materiales", Icons.Filled.Category, SiteOrange),
    ModuleCardData("designs", "Mis Diseños", Icons.Filled.Folder, CityViolet),
    ModuleCardData("progress", "Progreso", Icons.Filled.EmojiEvents, SiteAmber)
)

@Composable
private fun ModuleCard(data: ModuleCardData, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = data.color.copy(alpha = 0.15f)),
        modifier = Modifier.aspectRatio(0.85f).clickable(onClick = onClick)
    ) {
        Column(
            Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(data.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(data.icon, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(data.title, style = MaterialTheme.typography.labelMedium, color = Ink900, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}
