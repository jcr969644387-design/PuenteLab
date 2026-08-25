package com.educalab.puentelab.ui.screens.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.domain.model.MemberRole
import com.educalab.puentelab.domain.model.StructureType
import com.educalab.puentelab.ui.components.BuilderCanvasView
import com.educalab.puentelab.ui.components.ScenarioScene
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.BuilderViewModel

@Composable
fun BuilderScreen(
    challengeId: String,
    viewModel: BuilderViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(challengeId) { viewModel.loadChallenge(challengeId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val challenge = state.challenge
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PaperBg,
        topBar = {
            TopAppBar(
                title = { Text(challenge?.name ?: "Constructor", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) { Icon(Icons.Filled.RestartAlt, contentDescription = "Reiniciar diseño") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blueprint700, titleContentColor = White, navigationIconContentColor = White, actionIconContentColor = White)
            )
        },
        bottomBar = {
            BuilderToolbar(state = state, viewModel = viewModel, onTest = {
                viewModel.runSimulation(vehicleId = "van_explorer", vehicleWeightMultiplier = 1.0)
            })
        }
    ) { padding ->
        if (state.loading || challenge == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            BudgetBar(cost = state.liveCost, budget = challenge.budget)

            Box(Modifier.weight(1f).fillMaxWidth()) {
                ScenarioScene(challenge.scenario, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(White.copy(alpha = 0.35f)))
                BuilderCanvasView(
                    design = state.design,
                    materialsById = state.materials.associateBy { it.id },
                    pendingNodeId = state.pendingNodeId,
                    spanUnits = challenge.spanUnits,
                    onTapNode = viewModel::tapNode,
                    onTapEmpty = viewModel::placeFreeNode
                )
            }
        }

        if (state.showResult && state.lastResult != null) {
            SimulationResultDialog(
                result = state.lastResult!!,
                narrativeSuccess = challenge.narrativeSuccess,
                onDismiss = viewModel::dismissResult,
                onSave = { showSaveDialog = true }
            )
        }

        if (showSaveDialog) {
            SaveDesignDialog(
                onConfirm = { name ->
                    viewModel.saveDesign(name) { }
                    showSaveDialog = false
                },
                onDismiss = { showSaveDialog = false }
            )
        }
    }
}

@Composable
private fun BudgetBar(cost: Double, budget: Double) {
    val over = cost > budget
    Row(
        Modifier.fillMaxWidth().background(Blueprint100).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Presupuesto", style = MaterialTheme.typography.labelLarge, color = Ink600)
        Text(
            "$${cost.toInt()} / $${budget.toInt()}",
            style = MaterialTheme.typography.titleMedium,
            color = if (over) WarningRed else SuccessGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BuilderToolbar(state: com.educalab.puentelab.ui.viewmodel.BuilderUiState, viewModel: BuilderViewModel, onTest: () -> Unit) {
    Column(Modifier.background(White).padding(vertical = 8.dp)) {
        LazyRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.materials) { material ->
                val selected = material.id == state.selectedMaterialId
                AssistChip(
                    onClick = { viewModel.selectMaterial(material.id) },
                    label = { Text(material.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected) SiteOrange else Blueprint100,
                        labelColor = if (selected) White else Ink900
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RoleButton("Calzada", state.selectedRole == MemberRole.DECK) { viewModel.selectRole(MemberRole.DECK) }
            RoleButton("Riostra", state.selectedRole == MemberRole.BRACE) { viewModel.selectRole(MemberRole.BRACE) }
            RoleButton("Cable", state.selectedRole == MemberRole.CABLE) { viewModel.selectRole(MemberRole.CABLE) }
            RoleButton("Torre", state.selectedRole == MemberRole.TOWER) { viewModel.selectRole(MemberRole.TOWER) }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.pierMode,
                onClick = { viewModel.togglePierMode(!state.pierMode) },
                label = { Text("Apoyo nuevo (+$35)") }
            )
            Spacer(Modifier.weight(1f))
            Button(onClick = onTest, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Probar puente")
            }
        }
    }
}

@Composable
private fun RoleButton(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun SaveDesignDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("Mi puente") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guardar diseño") },
        text = {
            OutlinedTextField(value = name, onValueChange = { if (it.length <= 24) name = it }, singleLine = true)
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
