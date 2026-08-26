package com.educalab.puentelab.ui.screens.scenarios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun ScenariosScreen(viewModel: ScenariosViewModel, onOpenChallenge: (String) -> Unit) {
    val items by viewModel.challengeItems.collectAsStateWithLifecycle()
    val byScenario = items.groupBy { it.challenge.scenario }
    var expanded by remember { mutableStateOf<ScenarioType?>(null) }

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Escenarios", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
                Spacer(Modifier.height(4.dp))
                Text("Cada escenario tiene 9 desafíos de dificultad creciente.", style = MaterialTheme.typography.bodyMedium, color = Ink600)
            }
            items(ScenarioType.values().toList()) { scenario ->
                val list = byScenario[scenario].orEmpty().sortedBy { it.challenge.orderIndex }
                val completed = list.count { it.state == ModuleState.COMPLETED || it.state == ModuleState.MASTERED }
                ScenarioCard(
                    scenario = scenario,
                    completed = completed,
                    total = list.size,
                    isExpanded = expanded == scenario,
                    onToggle = { expanded = if (expanded == scenario) null else scenario }
                ) {
                    Column {
                        list.forEach { item ->
                            ChallengeRow(item, onClick = { if (item.state != ModuleState.LOCKED) onOpenChallenge(item.challenge.id) })
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: ScenarioType,
    completed: Int,
    total: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    val info = ScenarioEducation.byScenario[scenario]
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))) {
                ScenarioScene(scenario, modifier = Modifier.fillMaxSize())
                Box(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth()
                        .background(Blueprint900.copy(alpha = 0.55f)).padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(scenario.displayName, style = MaterialTheme.typography.titleLarge, color = White)
                        if (info != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "· ${info.difficultyLabel}",
                                style = MaterialTheme.typography.labelLarge, color = SiteAmber
                            )
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("$completed / $total desafíos completados", style = MaterialTheme.typography.bodyMedium, color = Ink600)
                    if (info != null) {
                        Text("🎯 ${info.educationalGoal}", style = MaterialTheme.typography.bodySmall, color = Ink600)
                    }
                }
                Icon(
                    Icons.Filled.ChevronRight, contentDescription = if (isExpanded) "Ocultar niveles" else "Ver niveles",
                    tint = Ink600, modifier = Modifier.size(24.dp)
                )
            }
            if (isExpanded) {
                Divider()
                expandedContent()
            }
        }
    }
}

@Composable
private fun ChallengeRow(item: ChallengeUiItem, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = item.state != ModuleState.LOCKED, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Nivel ${item.challenge.orderIndex} · ${item.challenge.demand.displayName}",
                style = MaterialTheme.typography.titleMedium,
                color = if (item.state == ModuleState.LOCKED) Ink600.copy(alpha = 0.5f) else Ink900
            )
            Spacer(Modifier.height(4.dp))
            ModuleStateChip(item.state)
        }
        if (item.bestStars > 0) StarRow(item.bestStars)
    }
}
