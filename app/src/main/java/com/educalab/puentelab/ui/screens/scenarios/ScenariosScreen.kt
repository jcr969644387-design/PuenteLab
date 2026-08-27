package com.educalab.puentelab.ui.screens.scenarios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.domain.model.ModuleState
import com.educalab.puentelab.domain.model.ScenarioEducation
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.ui.components.ModuleStateChip
import com.educalab.puentelab.ui.components.ScenarioScene
import com.educalab.puentelab.ui.components.StarRow
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.ChallengeUiItem
import com.educalab.puentelab.ui.viewmodel.ScenariosViewModel

/**
 * Pantalla exclusiva de un escenario: Inicio → Escenario → Misiones → Construcción. Solo se
 * llega aquí tocando un escenario desbloqueado en la pantalla principal, y aquí sí se listan
 * sus misiones (antes aparecían directamente en el Inicio, lo cual confundía la navegación).
 */
@Composable
fun ScenarioMissionsScreen(scenario: ScenarioType, viewModel: ScenariosViewModel, onBack: () -> Unit, onOpenChallenge: (String) -> Unit) {
    val items by viewModel.challengeItems.collectAsStateWithLifecycle()
    val missions = items.filter { it.challenge.scenario == scenario }.sortedBy { it.challenge.orderIndex }
    val info = ScenarioEducation.byScenario[scenario]
    val completed = missions.count { it.state == ModuleState.COMPLETED || it.state == ModuleState.MASTERED }

    Scaffold(
        containerColor = PaperBg,
        topBar = {
            TopAppBar(
                title = { Text(scenario.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blueprint700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Box(Modifier.fillMaxWidth().height(140.dp)) {
                    ScenarioScene(scenario, modifier = Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(Blueprint900.copy(alpha = 0.45f)))
                    Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(scenario.displayName, style = MaterialTheme.typography.headlineSmall, color = White)
                            if (info != null) {
                                Spacer(Modifier.width(8.dp))
                                Text("· ${info.difficultyLabel}", style = MaterialTheme.typography.labelLarge, color = SiteAmber)
                            }
                        }
                        Text("$completed / ${missions.size} misiones completadas", style = MaterialTheme.typography.bodyMedium, color = Blueprint100)
                    }
                }
                if (info != null) {
                    Text(
                        "🎯 ${info.educationalGoal}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium, color = Ink600
                    )
                }
            }
            items(missions, key = { it.challenge.id }) { mission ->
                ChallengeRow(mission, onClick = { if (mission.state != ModuleState.LOCKED) onOpenChallenge(mission.challenge.id) })
                Divider(color = Blueprint100)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ChallengeRow(item: ChallengeUiItem, onClick: () -> Unit) {
    val locked = item.state == ModuleState.LOCKED
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = !locked, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (locked) "🔒" else if (item.state == ModuleState.COMPLETED || item.state == ModuleState.MASTERED) "✅" else "🌟",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Misión ${item.challenge.orderIndex} · ${item.challenge.name}",
                style = MaterialTheme.typography.titleMedium,
                color = if (locked) Ink600.copy(alpha = 0.5f) else Ink900
            )
            Spacer(Modifier.height(4.dp))
            ModuleStateChip(item.state)
        }
        if (item.bestStars > 0) StarRow(item.bestStars)
    }
}
