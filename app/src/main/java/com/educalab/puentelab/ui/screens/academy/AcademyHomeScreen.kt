package com.educalab.puentelab.ui.screens.academy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import com.educalab.puentelab.domain.model.ModuleState
import com.educalab.puentelab.ui.components.LevelBadgeCircle
import com.educalab.puentelab.ui.components.PivotCharacter
import com.educalab.puentelab.ui.components.PivotMood
import com.educalab.puentelab.ui.components.XpProgressBar
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.AcademyViewModel

@Composable
fun AcademyHomeScreen(
    viewModel: AcademyViewModel,
    onOpenScenarios: () -> Unit,
    onOpenMaterials: () -> Unit,
    onOpenDesigns: () -> Unit,
    onOpenProgress: () -> Unit,
    onContinueChallenge: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    val nextChallenge = challenges.firstOrNull()

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // Encabezado del estudio
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Estudio PuenteLab", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
                    Text(
                        if (state.alias.isNotBlank()) "Bienvenido/a, ${state.alias}" else "Bienvenido/a",
                        style = MaterialTheme.typography.bodyLarge, color = Ink600
                    )
                }
                LevelBadgeCircle(level = state.levelInfo.level)
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

            // Tarjeta de siguiente actividad con PIVOT
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blueprint700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    PivotCharacter(mood = PivotMood.THINKING, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("PIVOT sugiere", style = MaterialTheme.typography.labelLarge, color = SiteAmber)
                        Text(
                            nextChallenge?.name ?: "¡Ya casi completas todo el estudio!",
                            style = MaterialTheme.typography.titleMedium, color = White
                        )
                    }
                    if (nextChallenge != null) {
                        FilledIconButton(
                            onClick = { onContinueChallenge(nextChallenge.id) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = SiteOrange)
                        ) { Icon(Icons.Filled.PlayArrow, contentDescription = "Continuar") }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Explora el estudio", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(moduleCards()) { module ->
                    ModuleCard(module) {
                        when (module.id) {
                            "scenarios" -> onOpenScenarios()
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

private data class ModuleCardData(val id: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

private fun moduleCards() = listOf(
    ModuleCardData("scenarios", "Escenarios", Icons.Filled.Map, RiverTeal),
    ModuleCardData("materials", "Materiales", Icons.Filled.Category, SiteOrange),
    ModuleCardData("designs", "Mis Diseños", Icons.Filled.Folder, CityViolet),
    ModuleCardData("progress", "Progreso", Icons.Filled.EmojiEvents, SiteAmber)
)

@Composable
private fun ModuleCard(data: ModuleCardData, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = data.color.copy(alpha = 0.15f)),
        modifier = Modifier.aspectRatio(1.3f).clickable(onClick = onClick)
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(data.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(data.icon, contentDescription = null, tint = White)
            }
            Text(data.title, style = MaterialTheme.typography.titleMedium, color = Ink900, textAlign = TextAlign.Start)
        }
    }
}

