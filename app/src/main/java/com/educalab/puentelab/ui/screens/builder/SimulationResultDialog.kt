package com.educalab.puentelab.ui.screens.builder

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }
    val progress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1400, easing = LinearEasing),
        label = "vehicle_crossing"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (result.passed) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (result.passed) SuccessGreen else WarningRed
                )
                Spacer(Modifier.width(8.dp))
                Text(if (result.passed) "¡Puente aprobado!" else "El puente no resistió")
            }
        },
        text = {
            Column {
                CrossingAnimation(progress = progress, passed = result.passed)
                Spacer(Modifier.height(12.dp))
                if (result.passed) {
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
private fun CrossingAnimation(progress: Float, passed: Boolean) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Blueprint100, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            // calzada
            drawLine(SiteAmber, Offset(w * 0.1f, h * 0.7f), Offset(w * 0.9f, h * 0.7f), strokeWidth = 10f)
            // vehículo
            val collapse = !passed && progress > 0.55f
            val x = w * (0.1f + 0.8f * progress.coerceAtMost(if (collapse) 0.55f else 1f))
            val y = if (collapse) h * (0.7f + (progress - 0.55f) * 1.6f) else h * 0.6f
            drawRoundRect(
                color = if (collapse) WarningRed else SiteOrange,
                topLeft = Offset(x - 18f, y - 14f),
                size = androidx.compose.ui.geometry.Size(36f, 22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
        if (progress >= 0.98f && passed) {
            PivotCharacter(mood = PivotMood.HAPPY, modifier = Modifier.align(Alignment.CenterEnd).size(48.dp).padding(end = 8.dp))
        }
    }
}
