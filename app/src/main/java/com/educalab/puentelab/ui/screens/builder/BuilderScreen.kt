package com.educalab.puentelab.ui.screens.builder

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.audio.GameFeedback
import com.educalab.puentelab.audio.rememberGameFeedback
import com.educalab.puentelab.data.local.entity.VehicleEntity
import com.educalab.puentelab.domain.model.BridgeChallengeSpec
import com.educalab.puentelab.domain.model.MaterialSpec
import com.educalab.puentelab.domain.model.MemberRole
import com.educalab.puentelab.domain.model.ScenarioEducationInfo
import com.educalab.puentelab.domain.model.StructureType
import com.educalab.puentelab.ui.components.BuilderCanvasView
import com.educalab.puentelab.ui.components.ScenarioScene
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.BuilderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    challengeId: String,
    viewModel: BuilderViewModel,
    onBack: () -> Unit,
    onNextMission: (String) -> Unit
) {
    LaunchedEffect(challengeId) { viewModel.loadChallenge(challengeId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val challenge = state.challenge
    var showSaveDialog by remember { mutableStateOf(false) }
    var suggestedName by remember { mutableStateOf("Mi Puente") }
    var showInstructions by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(false) }
    val feedback = rememberGameFeedback()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SideEffect {
        feedback.soundEnabled = state.soundEnabled
        feedback.hapticEnabled = state.hapticEnabled
    }

    // Estado de la breve animación de desvanecido al borrar una sola pieza (nodo o barra).
    var fadingNodeId by remember { mutableStateOf<String?>(null) }
    var fadingMemberId by remember { mutableStateOf<String?>(null) }
    val fadeAlpha = remember { Animatable(1f) }

    fun deleteNodeWithFade(id: String) {
        if (fadingNodeId != null || fadingMemberId != null) return
        feedback.deletePiece()
        fadingNodeId = id
        scope.launch {
            fadeAlpha.snapTo(1f)
            fadeAlpha.animateTo(0f, animationSpec = tween(220))
            viewModel.removeFreeNode(id)
            fadingNodeId = null
            fadeAlpha.snapTo(1f)
        }
    }

    fun deleteMemberWithFade(id: String) {
        if (fadingNodeId != null || fadingMemberId != null) return
        feedback.deletePiece()
        fadingMemberId = id
        scope.launch {
            fadeAlpha.snapTo(1f)
            fadeAlpha.animateTo(0f, animationSpec = tween(220))
            viewModel.removeMember(id)
            fadingMemberId = null
            fadeAlpha.snapTo(1f)
        }
    }

    // La primera vez que se abre CUALQUIER desafío, se muestra sola. En las siguientes ya no
    // se abre sola (queda solo la info al tocar el "?"), y se marca como vista una sola vez.
    LaunchedEffect(state.autoShowInstructions) {
        if (state.autoShowInstructions) {
            showInstructions = true
            viewModel.markInstructionsSeen()
        }
    }

    // "Construir correctamente": suena una sola vez, justo cuando las partes obligatorias del
    // escenario pasan de faltar a estar completas (no en la primera carga del desafío).
    var wasMissingRequired by remember(challengeId) { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(state.missingRequiredRoles) {
        val nowComplete = state.missingRequiredRoles.isEmpty()
        if (wasMissingRequired == true && nowComplete) feedback.buildCorrect()
        wasMissingRequired = !nowComplete
    }

    // Sonido y vibración distintos si el puente aprobó o no. Si aprobó: fanfarria de escenario
    // nuevo (la más festiva), o de misión completa; si además queda una misión siguiente en el
    // mismo escenario, se agrega un aviso de "misión desbloqueada"; y por cada estrella ganada
    // se suma un "ding" en cadena.
    LaunchedEffect(state.showResult) {
        val result = state.lastResult
        if (state.showResult && result != null) {
            if (result.passed) {
                feedback.bridgeSuccess()
                delay(140)
                if (state.nextChallengeIsNewScenario) {
                    feedback.scenarioUnlock()
                } else {
                    feedback.missionComplete()
                    if (state.nextChallengeId != null) {
                        delay(180)
                        feedback.missionUnlock()
                    }
                }
                repeat(result.stars.coerceIn(0, 3)) { i ->
                    delay(if (i == 0) 220 else 160)
                    feedback.starEarned()
                }
            } else {
                feedback.bridgeFail()
            }
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
                    IconButton(onClick = { feedback.tap(); deleteMode = !deleteMode }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = if (deleteMode) "Salir del modo borrar" else "Borrar una pieza",
                            tint = if (deleteMode) SiteAmber else White
                        )
                    }
                    IconButton(onClick = { feedback.tap(); deleteMode = false; viewModel.clearAll() }) { Icon(Icons.Filled.RestartAlt, contentDescription = "Reiniciar diseño") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blueprint700, titleContentColor = White, navigationIconContentColor = White, actionIconContentColor = White)
            )
        },
        bottomBar = {
            BuilderToolbar(
                state = state,
                viewModel = viewModel,
                feedback = feedback,
                onTest = { feedback.testStart(); viewModel.runSimulation() }
            )
        }
    ) { padding ->
        if (state.loading || challenge == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        val materialsById = state.materials.associateBy { it.id }
        Column(Modifier.fillMaxSize().padding(padding)) {
            MissionBanner(challenge, vehicle = state.testVehicle, scenarioInfo = state.scenarioInfo, materialsById = materialsById)
            BudgetBar(cost = state.liveCost, budget = challenge.budget)
            if (deleteMode) {
                Row(
                    Modifier.fillMaxWidth().background(WarningRed.copy(alpha = 0.12f)).padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = WarningRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Modo borrar: toca una barra o un punto libre para eliminarlo.", style = MaterialTheme.typography.labelMedium, color = WarningRed)
                }
            } else if (state.missingRequiredRoles.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().background(SiteAmber.copy(alpha = 0.18f)).padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Aún falta: ", style = MaterialTheme.typography.labelMedium, color = Ink900)
                    Text(
                        state.missingRequiredRoles.joinToString("  ") { "${it.emoji} ${it.displayName}" },
                        style = MaterialTheme.typography.labelMedium, color = Ink900, fontWeight = FontWeight.Bold
                    )
                }
            }
            if (state.missionConstraint != null) {
                Row(
                    Modifier.fillMaxWidth().background(CityViolet.copy(alpha = 0.15f)).padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(state.missionConstraint!!.label, style = MaterialTheme.typography.labelMedium, color = CityViolet)
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                ScenarioScene(challenge.scenario, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(White.copy(alpha = 0.35f)))
                BuilderCanvasView(
                    design = state.design,
                    materialsById = materialsById,
                    pendingNodeId = state.pendingNodeId,
                    spanUnits = challenge.spanUnits,
                    deleteMode = deleteMode,
                    fadingNodeId = fadingNodeId,
                    fadingMemberId = fadingMemberId,
                    fadingAlpha = fadeAlpha.value,
                    onTapNode = { nodeId ->
                        val before = viewModel.uiState.value.design.members.size
                        viewModel.tapNode(nodeId)
                        val after = viewModel.uiState.value.design.members.size
                        if (after > before) feedback.connectNode() else feedback.tap()
                    },
                    onTapEmpty = { point -> feedback.placePiece(); viewModel.placeFreeNode(point) },
                    onDeleteNode = ::deleteNodeWithFade,
                    onDeleteMember = ::deleteMemberWithFade,
                    onDeleteBlocked = {
                        Toast.makeText(context, "Ese punto es fijo del nivel: no se puede borrar", Toast.LENGTH_SHORT).show()
                    }
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
                vehicleCount = state.vehicleCount,
                onDismiss = viewModel::dismissResult,
                onSave = {
                    scope.launch { suggestedName = viewModel.suggestedDesignName() }
                    showSaveDialog = true
                },
                onNextMission = state.nextChallengeId?.let { id -> { onNextMission(id) } },
                nextMissionLabel = if (state.nextChallengeIsNewScenario) "Siguiente nivel" else "Siguiente misión"
            )
        }

        if (showSaveDialog) {
            SaveDesignDialog(
                initialName = suggestedName,
                onConfirm = { name ->
                    viewModel.saveDesign(name) { }
                    showSaveDialog = false
                },
                onDismiss = { showSaveDialog = false }
            )
        }
    }
}

/** Objetivo del desafío, siempre visible arriba del lienzo para no perder de vista la misión. */
@Composable
private fun MissionBanner(
    challenge: BridgeChallengeSpec,
    vehicle: VehicleEntity?,
    scenarioInfo: ScenarioEducationInfo?,
    materialsById: Map<String, MaterialSpec>,
    modifier: Modifier = Modifier
) {
    var showHint by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxWidth()
            .background(Blueprint700)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("MISIÓN", style = MaterialTheme.typography.labelMedium, color = SiteAmber)
            if (scenarioInfo != null) {
                Spacer(Modifier.width(6.dp))
                Text("· ${scenarioInfo.difficultyLabel}", style = MaterialTheme.typography.labelMedium, color = Blueprint100)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(challenge.narrativeIntro, style = MaterialTheme.typography.bodyMedium, color = White)
        if (vehicle != null) {
            Spacer(Modifier.height(4.dp))
            Text("Lo va a probar: ${vehicle.name}", style = MaterialTheme.typography.labelMedium, color = Blueprint100)
        }
        if (scenarioInfo != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                if (showHint) "💡 Ocultar pista" else "💡 Ver pista de materiales",
                modifier = Modifier.clickable { showHint = !showHint },
                style = MaterialTheme.typography.labelMedium, color = SiteAmber
            )
            if (showHint) {
                Spacer(Modifier.height(6.dp))
                Text(scenarioInfo.educationalGoal, style = MaterialTheme.typography.bodySmall, color = Blueprint100)
                Spacer(Modifier.height(6.dp))
                MemberRole.values().forEach { role ->
                    val matName = scenarioInfo.recommendedMaterialByRole[role]?.let { materialsById[it]?.name } ?: return@forEach
                    Text("${role.emoji} ${role.displayName}: $matName", style = MaterialTheme.typography.bodySmall, color = White)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Es solo una pista: puedes probar otros materiales. Si no aguanta, el juego te lo dice y puedes intentar de nuevo.",
                    style = MaterialTheme.typography.bodySmall, color = Blueprint100
                )
            }
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
            Column(Modifier.verticalScroll(rememberScrollState())) {
                InstructionStep("1", "Abajo elige QUÉ material vas a usar (por ejemplo Madera) y PARA QUÉ parte del puente.")
                InstructionStep("2", "Toca un espacio vacío del dibujo para poner un punto nuevo.")
                InstructionStep("3", "Toca dos puntos, uno y después el otro, para unirlos con una barra.")
                InstructionStep("4", "¿Necesitas un punto extra en el suelo? Activa \"Apoyo nuevo\" (cuesta $35).")
                InstructionStep("5", "Cuando creas que tu puente está listo, toca \"Probar puente\" para ver si el vehículo puede cruzar.")
                InstructionStep("6", "Ojo con el presupuesto de arriba: si gastas más de lo que tienes, no vale aunque el puente aguante.")

                Spacer(Modifier.height(14.dp))
                Text("Las 4 partes del puente", style = MaterialTheme.typography.titleMedium, color = Blueprint900)
                Spacer(Modifier.height(6.dp))
                MemberRole.values().forEach { role -> RoleExplanation(role) }

                Spacer(Modifier.height(10.dp))
                Text(
                    "No hay una única forma correcta: puedes probar materiales distintos a los de la pista. " +
                        "Si no aguanta, prueba algo más resistente. ¡Aprender probando es parte del juego!",
                    style = MaterialTheme.typography.bodySmall, color = Ink600
                )
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
private fun RoleExplanation(role: MemberRole) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text(role.emoji, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(28.dp))
        Column {
            Text(role.displayName, style = MaterialTheme.typography.labelLarge, color = Blueprint900)
            Text(role.shortDescription, style = MaterialTheme.typography.bodySmall, color = Ink600)
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
private fun BuilderToolbar(
    state: com.educalab.puentelab.ui.viewmodel.BuilderUiState,
    viewModel: BuilderViewModel,
    feedback: GameFeedback,
    onTest: () -> Unit
) {
    val recommendedMaterialId = state.scenarioInfo?.recommendedMaterialByRole?.get(state.selectedRole)
    Column(Modifier.background(White).padding(vertical = 8.dp)) {
        LazyRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.materials) { material ->
                val selected = material.id == state.selectedMaterialId
                val unlocked = material.unlockLevel <= state.playerLevel
                val recommended = material.id == recommendedMaterialId
                SelectorItem(
                    glyph = materialEmoji(material.id),
                    label = if (unlocked) material.name else "Nv.${material.unlockLevel} 🔒",
                    selected = selected,
                    enabled = unlocked,
                    badge = if (recommended && unlocked) "⭐" else null,
                    onClick = { feedback.tap(); viewModel.selectMaterial(material.id) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MemberRole.values().toList()) { role ->
                SelectorItem(
                    glyph = role.emoji,
                    label = role.displayName,
                    selected = state.selectedRole == role,
                    onClick = { feedback.tap(); viewModel.selectRole(role) }
                )
            }
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

/**
 * Tarjeta compacta de selección: ícono grande arriba, texto abajo (máx. 1 línea, sin partirse
 * letra por letra), borde de color cuando está seleccionada. Se usa tanto para materiales como
 * para las 4 partes del puente, siempre dentro de un LazyRow para no comprimir el texto.
 */
@Composable
private fun SelectorItem(
    glyph: String,
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    badge: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) SiteOrange.copy(alpha = 0.16f) else Blueprint100)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) SiteOrange else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Text(glyph, fontSize = 24.sp)
            if (badge != null) {
                Text(badge, fontSize = 12.sp, modifier = Modifier.offset(x = 6.dp, y = (-4).dp))
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) Ink900 else Ink600.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun materialEmoji(materialId: String): String = when (materialId) {
    "rope" -> "🧵"
    "wood" -> "🪵"
    "stone" -> "🪨"
    "steel" -> "🔩"
    "steel_cable" -> "🪢"
    "concrete" -> "🧱"
    "aluminum" -> "✈️"
    "carbon_fiber" -> "⚫"
    else -> "▪️"
}

@Composable
private fun SaveDesignDialog(initialName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
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
