package com.educalab.puentelab.ui.screens.builder

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.data.local.entity.VehicleEntity
import com.educalab.puentelab.domain.model.BridgeChallengeSpec
import com.educalab.puentelab.domain.model.MemberRole
import com.educalab.puentelab.domain.model.StructureType
import com.educalab.puentelab.ui.components.BuilderCanvasView
import com.educalab.puentelab.ui.components.ScenarioScene
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.BuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    var showInstructions by remember { mutableStateOf(false) }
    val feedback = rememberGameFeedback()

    // La primera vez que se abre CUALQUIER desafío, se muestra sola. En las siguientes ya no
    // se abre sola (queda solo la info al tocar el "?"), y se marca como vista una sola vez.
    LaunchedEffect(state.autoShowInstructions) {
        if (state.autoShowInstructions) {
            showInstructions = true
            viewModel.markInstructionsSeen()
        }
    }
    // Avisa con sonido y vibración distintos si el puente aprobó o no.
    LaunchedEffect(state.showResult) {
        val result = state.lastResult
        if (state.showResult && result != null) {
            if (result.passed) feedback.success() else feedback.failure()
        }
    }

    Scaffold(
        containerColor = PaperBg,
        topBar = {
            TopAppBar(
                title = { Text(challenge?.name ?: "Constructor", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = { feedback.tap(); showInstructions = true }) { Icon(Icons.Filled.HelpOutline, contentDescription = "Cómo jugar") }
                    IconButton(onClick = { feedback.tap(); viewModel.clearAll() }) { Icon(Icons.Filled.RestartAlt, contentDescription = "Reiniciar diseño") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blueprint700, titleContentColor = White, navigationIconContentColor = White, actionIconContentColor = White)
            )
        },
        bottomBar = {
            BuilderToolbar(
                state = state,
                viewModel = viewModel,
                feedback = feedback,
                onTest = { feedback.tap(); viewModel.runSimulation() }
            )
        }
    ) { padding ->
        if (state.loading || challenge == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            MissionBanner(challenge, vehicle = state.testVehicle)
            BudgetBar(cost = state.liveCost, budget = challenge.budget)

            Box(Modifier.weight(1f).fillMaxWidth()) {
                ScenarioScene(challenge.scenario, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(White.copy(alpha = 0.35f)))
                BuilderCanvasView(
                    design = state.design,
                    materialsById = state.materials.associateBy { it.id },
                    pendingNodeId = state.pendingNodeId,
                    spanUnits = challenge.spanUnits,
                    onTapNode = { feedback.tap(); viewModel.tapNode(it) },
                    onTapEmpty = { feedback.tap(); viewModel.placeFreeNode(it) }
                )
            }
        }

        if (showInstructions) {
            InstructionsDialog(onDismiss = { showInstructions = false })
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

/** Sonido y vibración cortos para que tocar el lienzo y probar el puente se sientan "vivos". */
@Composable
private fun rememberGameFeedback(): GameFeedback {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    return remember { GameFeedback(context, haptic) }
}

private class GameFeedback(context: Context, private val haptic: HapticFeedback) {
    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val toneGenerator = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 75) }.getOrNull()

    fun tap() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        runCatching { audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK) }
    }

    fun success() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 500) }
    }

    fun failure() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 500) }
    }
}

/** Objetivo del desafío, siempre visible arriba del lienzo para no perder de vista la misión. */
@Composable
private fun MissionBanner(challenge: BridgeChallengeSpec, vehicle: VehicleEntity?, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Blueprint700)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text("MISIÓN", style = MaterialTheme.typography.labelMedium, color = SiteAmber)
        Spacer(Modifier.height(2.dp))
        Text(challenge.narrativeIntro, style = MaterialTheme.typography.bodyMedium, color = White)
        if (vehicle != null) {
            Spacer(Modifier.height(4.dp))
            Text("Lo va a probar: ${vehicle.name}", style = MaterialTheme.typography.labelMedium, color = Blueprint100)
        }
    }
}

/** Explica cómo se juega, con palabras simples y directas (pensado para chicos de 10 a 15 años). */
@Composable
private fun InstructionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cómo se juega?") },
        text = {
            Column {
                InstructionStep("1", "Abajo elige QUÉ material vas a usar (por ejemplo Madera) y PARA QUÉ (Calzada, Riostra, Cable o Torre).")
                InstructionStep("2", "Toca un espacio vacío del dibujo para poner un punto nuevo.")
                InstructionStep("3", "Toca dos puntos, uno y después el otro, para unirlos con una barra.")
                InstructionStep("4", "¿Necesitas un punto extra en el suelo? Activa \"Apoyo nuevo\" (cuesta $35).")
                InstructionStep("5", "Cuando creas que tu puente está listo, toca \"Probar puente\" para ver si el vehículo puede cruzar.")
                InstructionStep("6", "Ojo con el presupuesto de arriba: si gastas más de lo que tienes, no vale aunque el puente aguante.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("¡Listo!") } }
    )
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text("$number.", color = SiteOrange, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(22.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink900)
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
private fun BuilderToolbar(
    state: com.educalab.puentelab.ui.viewmodel.BuilderUiState,
    viewModel: BuilderViewModel,
    feedback: GameFeedback,
    onTest: () -> Unit
) {
    Column(Modifier.background(White).padding(vertical = 8.dp)) {
        LazyRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.materials) { material ->
                val selected = material.id == state.selectedMaterialId
                val unlocked = material.unlockLevel <= state.playerLevel
                AssistChip(
                    onClick = { feedback.tap(); viewModel.selectMaterial(material.id) },
                    enabled = unlocked,
                    label = { Text(if (unlocked) material.name else "${material.name} 🔒 Nv.${material.unlockLevel}") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected) SiteOrange else Blueprint100,
                        labelColor = if (selected) White else Ink900,
                        disabledContainerColor = Blueprint100.copy(alpha = 0.4f),
                        disabledLabelColor = Ink600.copy(alpha = 0.6f)
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RoleButton("Calzada", state.selectedRole == MemberRole.DECK) { feedback.tap(); viewModel.selectRole(MemberRole.DECK) }
            RoleButton("Riostra", state.selectedRole == MemberRole.BRACE) { feedback.tap(); viewModel.selectRole(MemberRole.BRACE) }
            RoleButton("Cable", state.selectedRole == MemberRole.CABLE) { feedback.tap(); viewModel.selectRole(MemberRole.CABLE) }
            RoleButton("Torre", state.selectedRole == MemberRole.TOWER) { feedback.tap(); viewModel.selectRole(MemberRole.TOWER) }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.pierMode,
                onClick = { feedback.tap(); viewModel.togglePierMode(!state.pierMode) },
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
