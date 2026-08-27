package com.educalab.puentelab.ui.screens.builder

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.domain.model.SimulationResult
import com.educalab.puentelab.ui.components.PivotCharacter
import com.educalab.puentelab.ui.components.PivotMood
import com.educalab.puentelab.ui.components.StarRow
import com.educalab.puentelab.ui.theme.*

@Composable
fun SimulationResultDialog(
    result: SimulationResult,
    narrativeSuccess: String,
    vehicleCount: Int,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onNextMission: (() -> Unit)? = null,
    nextMissionLabel: String = "Siguiente misión"
) {
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }
    val progress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1400 + (vehicleCount - 1) * 300, easing = LinearEasing),
        label = "vehicle_crossing"
    )

    // Tres niveles simples de resultado: verde (aguantó bien), amarillo (aguantó justo),
    // rojo (colapsó). Así el chico ve de un vistazo qué tan bien le fue, sin números raros.
    val tierColor = when {
        !result.passed -> WarningRed
        result.stars == 1 -> SiteAmber
        else -> SuccessGreen
    }
    val tierTitle = when {
        !result.passed -> "🔴 El puente no resistió"
        result.stars == 1 -> "🟡 ¡Casi! Aguantó justo"
        else -> "🟢 ¡Excelente! Tu puente aguantó"
    }
    // Para un puente que falló, las razones específicas ya se explican abajo (result.feedback);
    // aquí solo agregamos un mensaje corto cuando SÍ aprobó, para distinguir "justo" de "sobrado".
    val tierMessage = when {
        result.stars == 1 -> "Algunas partes se movieron demasiado. Con materiales más resistentes te iría mejor la próxima vez."
        result.passed -> "¡Tu puente soportó la carga sin problemas!"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (result.passed) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = tierColor
                )
                Spacer(Modifier.width(8.dp))
                Text(tierTitle)
            }
        },
        text = {
            Column {
                CrossingAnimation(progress = progress, passed = result.passed, vehicleCount = vehicleCount)
                Spacer(Modifier.height(12.dp))
                if (tierMessage != null) {
                    Text(tierMessage, style = MaterialTheme.typography.bodyMedium, color = tierColor)
                }
                if (result.passed) {
                    Spacer(Modifier.height(8.dp))
                    StarRow(result.stars)
                    Spacer(Modifier.height(8.dp))
                    Text(narrativeSuccess, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                result.feedback.forEach { msg ->
                    Text("• $msg", style = MaterialTheme.typography.bodyMedium, color = Ink600)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Costo: $${result.totalCost.toInt()} / $${result.budget.toInt()} · Esfuerzo máximo: ${(result.maxStressRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium, color = Ink600
                )
                if (result.passed && onNextMission != null) {
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onNextMission,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SiteOrange)
                    ) {
                        Text(nextMissionLabel)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        },
        confirmButton = {
            if (result.passed) {
                TextButton(onClick = onSave) { Text("Guardar diseño") }
            } else {
                TextButton(onClick = onDismiss) { Text("Reintentar") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun CrossingAnimation(progress: Float, passed: Boolean, vehicleCount: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Blueprint100, RoundedCornerShape(12.dp))
            // Recorta cualquier dibujo que se salga de esta caja: sin esto, un vehículo que sigue
            // cayendo después de fallar quedaba visible por encima del texto del resultado.
            .clip(RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            // calzada
            drawLine(SiteAmber, Offset(w * 0.1f, h * 0.7f), Offset(w * 0.9f, h * 0.7f), strokeWidth = 10f)

            // Convoy: en niveles más exigentes cruza más de un vehículo, uno detrás del otro.
            for (i in 0 until vehicleCount) {
                val vehicleProgress = (progress - i * 0.22f).coerceIn(0f, 1f)
                if (vehicleProgress <= 0f) continue
                drawVehicle(vehicleProgress, passed, w, h)
            }
        }
        if (progress >= 0.98f && passed) {
            PivotCharacter(mood = PivotMood.HAPPY, modifier = Modifier.align(Alignment.TopEnd).size(40.dp).padding(top = 2.dp, end = 4.dp))
        }
    }
}

/** Vehículo (carrocería + cabina + ruedas) que cruza y, si el puente falla, sigue cayendo hasta
 * salir del recuadro (que ahora recorta el dibujo) en vez de quedar flotando sobre el texto. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVehicle(progress: Float, passed: Boolean, w: Float, h: Float) {
    val collapse = !passed && progress > 0.55f
    val x = w * (0.1f + 0.8f * progress.coerceAtMost(if (collapse) 0.55f else 1f))
    val y = if (collapse) h * (0.7f + (progress - 0.55f) * 3.2f) else h * 0.6f
    val bodyColor = if (collapse) WarningRed else SiteOrange
    val rotation = if (collapse) (progress - 0.55f) * 420f else 0f
    rotate(degrees = rotation, pivot = Offset(x, y)) {
        // carrocería
        drawRoundRect(
            color = bodyColor,
            topLeft = Offset(x - 22f, y - 16f),
            size = androidx.compose.ui.geometry.Size(44f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // cabina
        drawRoundRect(
            color = Blueprint900,
            topLeft = Offset(x - 8f, y - 26f),
            size = androidx.compose.ui.geometry.Size(20f, 14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // ruedas
        drawCircle(Blueprint900, radius = 7f, center = Offset(x - 13f, y + 8f))
        drawCircle(Blueprint900, radius = 7f, center = Offset(x + 13f, y + 8f))
    }
}
