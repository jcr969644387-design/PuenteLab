package com.educalab.puentelab.ui.screens.materials

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.data.local.entity.MaterialEntity
import com.educalab.puentelab.data.local.entity.VehicleEntity
import com.educalab.puentelab.domain.model.MemberRole
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.MaterialsViewModel

@Composable
fun MaterialsScreen(viewModel: MaterialsViewModel) {
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Materiales", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
                Text("Cada material tiene un balance distinto de resistencia, costo y peso.", style = MaterialTheme.typography.bodyMedium, color = Ink600)
                Spacer(Modifier.height(8.dp))
            }
            items(materials) { MaterialCard(it) }
            item {
                Spacer(Modifier.height(16.dp))
                Text("Vehículos de prueba", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
                Spacer(Modifier.height(8.dp))
            }
            items(vehicles) { vehicle ->
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        VehicleIcon(vehicle, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(vehicle.name, style = MaterialTheme.typography.titleMedium)
                            Text(vehicle.description, style = MaterialTheme.typography.bodyMedium, color = Ink600)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MaterialCard(material: MaterialEntity) {
    val stars = starsFor(material.id)
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaterialIcon(material, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(material.name, style = MaterialTheme.typography.titleMedium)
                    Text(material.description, style = MaterialTheme.typography.bodyMedium, color = Ink600)
                    if (material.allowedRoles.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Bueno para: " + material.allowedRoles.joinToString(" ") { "${it.emoji} ${it.displayName}" },
                            style = MaterialTheme.typography.labelMedium, color = Blueprint500
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                StarStat("💪", "Resistencia", stars.resistance, Modifier.weight(1f))
                StarStat("⚖️", "Peso", stars.weight, Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                StarStat("💰", "Costo", stars.cost, Modifier.weight(1f))
                StarStat("🔄", "Flexibilidad", stars.flexibility, Modifier.weight(1f))
            }
        }
    }
}

/** Estadística simple mostrada con estrellas (1 a 5), sin números ni fórmulas. */
@Composable
private fun StarStat(emoji: String, label: String, value: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Ink600)
            Text("★".repeat(value) + "☆".repeat(5 - value), color = SiteAmber, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private data class MaterialStars(val resistance: Int, val weight: Int, val cost: Int, val flexibility: Int)

/** Valores de 1 a 5 pensados para que un chico entienda el material de un vistazo. */
private fun starsFor(materialId: String): MaterialStars = when (materialId) {
    "rope" -> MaterialStars(resistance = 2, weight = 1, cost = 1, flexibility = 5)
    "wood" -> MaterialStars(resistance = 3, weight = 2, cost = 1, flexibility = 3)
    "stone" -> MaterialStars(resistance = 5, weight = 5, cost = 2, flexibility = 1)
    "steel" -> MaterialStars(resistance = 4, weight = 3, cost = 3, flexibility = 3)
    "steel_cable" -> MaterialStars(resistance = 4, weight = 2, cost = 3, flexibility = 4)
    "concrete" -> MaterialStars(resistance = 5, weight = 5, cost = 3, flexibility = 1)
    "aluminum" -> MaterialStars(resistance = 3, weight = 2, cost = 4, flexibility = 3)
    "carbon_fiber" -> MaterialStars(resistance = 5, weight = 1, cost = 5, flexibility = 3)
    else -> MaterialStars(3, 3, 3, 3)
}

/** Silueta simple de vehículo (carrocería + cabina + ruedas), coloreada según su escenario. */
@Composable
private fun VehicleIcon(vehicle: VehicleEntity, modifier: Modifier = Modifier) {
    val accent = colorForScenario(vehicle.themeScenario)
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(6.dp)) {
            val w = size.width; val h = size.height
            val bodyTop = h * 0.32f
            val bodyBottom = h * 0.62f
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.08f, bodyTop),
                size = Size(w * 0.84f, bodyBottom - bodyTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, w * 0.12f)
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.32f, h * 0.14f),
                size = Size(w * 0.4f, bodyTop - h * 0.14f + 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f, w * 0.08f)
            )
            drawCircle(Blueprint900, radius = w * 0.11f, center = Offset(w * 0.28f, bodyBottom))
            drawCircle(Blueprint900, radius = w * 0.11f, center = Offset(w * 0.72f, bodyBottom))
        }
    }
}

private fun colorForScenario(scenario: ScenarioType): Color = when (scenario) {
    ScenarioType.RIVER -> RiverTeal
    ScenarioType.CANYON -> CanyonTerracotta
    ScenarioType.FOREST -> ForestGreen
    ScenarioType.CITY -> CityViolet
    ScenarioType.MOUNTAIN -> MountainSlate
}

/**
 * Muestra de material con una textura propia (no un cuadrado de color plano), para que se
 * puedan reconocer a simple vista aunque dos materiales compartan un color parecido.
 */
@Composable
private fun MaterialIcon(material: MaterialEntity, modifier: Modifier = Modifier) {
    val base = runCatching { Color(android.graphics.Color.parseColor(material.colorHex)) }.getOrDefault(Blueprint500)
    val dark = base.copy(alpha = 1f).let { Color(it.red * 0.6f, it.green * 0.6f, it.blue * 0.6f, 1f) }
    val light = Color.White.copy(alpha = 0.55f)

    // Animaciones sutiles: un brillo que recorre los metales, y un ligero balanceo en la cuerda.
    val infinite = rememberInfiniteTransition(label = "materialIcon")
    val shine by infinite.animateFloat(
        initialValue = -0.3f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "shine"
    )
    val sway by infinite.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )

    Box(modifier.clip(RoundedCornerShape(12.dp)).background(base)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            when (material.id) {
                "wood" -> { // vetas de madera: líneas horizontales
                    var yy = h * 0.2f
                    while (yy < h) {
                        drawLine(dark, Offset(0f, yy), Offset(w, yy), strokeWidth = 2f)
                        yy += h * 0.22f
                    }
                }
                "rope" -> { // trenzado con un ligero balanceo, como una cuerda colgando
                    val skew = sway * w * 0.06f
                    var t = -h
                    while (t < w) {
                        drawLine(dark, Offset(t + skew, h), Offset(t + h - skew, 0f), strokeWidth = 2.5f)
                        t += w * 0.28f
                    }
                }
                "stone" -> { // piedra con pequeñas grietas
                    drawCircle(dark, radius = w * 0.16f, center = Offset(w * 0.3f, h * 0.35f))
                    drawCircle(dark, radius = w * 0.13f, center = Offset(w * 0.68f, h * 0.6f))
                    drawCircle(dark, radius = w * 0.1f, center = Offset(w * 0.32f, h * 0.75f))
                    drawLine(dark, Offset(w * 0.45f, h * 0.15f), Offset(w * 0.6f, h * 0.5f), strokeWidth = 1.5f)
                    drawLine(dark, Offset(w * 0.6f, h * 0.5f), Offset(w * 0.5f, h * 0.85f), strokeWidth = 1.5f)
                }
                "steel" -> { // viga en "I" con brillo que la recorre
                    drawLine(dark, Offset(w * 0.22f, h * 0.18f), Offset(w * 0.78f, h * 0.18f), strokeWidth = 5f)
                    drawLine(dark, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.82f), strokeWidth = 5f)
                    drawLine(dark, Offset(w * 0.22f, h * 0.82f), Offset(w * 0.78f, h * 0.82f), strokeWidth = 5f)
                    drawShine(shine, w, h, light)
                }
                "steel_cable" -> { // hebras trenzadas con reflejo metálico en movimiento
                    var t = 0f
                    while (t < w + h) {
                        drawLine(light, Offset(t, 0f), Offset(t - h, h), strokeWidth = 1.5f)
                        t += w * 0.2f
                    }
                    drawShine(shine, w, h, light)
                }
                "concrete" -> { // hormigón con barras de acero visibles
                    val dots = listOf(0.2f to 0.25f, 0.5f to 0.4f, 0.75f to 0.2f, 0.3f to 0.7f, 0.65f to 0.75f, 0.85f to 0.55f)
                    dots.forEach { (dx, dy) -> drawCircle(dark, radius = w * 0.045f, center = Offset(w * dx, h * dy)) }
                    drawLine(Color.White.copy(alpha = 0.7f), Offset(w * 0.15f, h * 0.85f), Offset(w * 0.85f, h * 0.85f), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = 0.7f), Offset(w * 0.15f, h * 0.92f), Offset(w * 0.85f, h * 0.92f), strokeWidth = 2f)
                }
                "aluminum" -> { // pieza plateada ligera con brillo en movimiento
                    drawLine(light, Offset(w * 0.1f, h * 0.85f), Offset(w * 0.9f, h * 0.15f), strokeWidth = w * 0.16f)
                    drawShine(shine, w, h, Color.White.copy(alpha = 0.4f))
                }
                "carbon_fiber" -> { // trama de fibra de carbono con efecto brillante
                    var t = 0f
                    while (t < w + h) {
                        drawLine(light, Offset(t, 0f), Offset(t - h, h), strokeWidth = 1.5f)
                        drawLine(light, Offset(t, h), Offset(t - h, 0f), strokeWidth = 1.5f)
                        t += w * 0.22f
                    }
                    drawShine(shine, w, h, light)
                }
            }
        }
    }
}

/** Franja diagonal clara que recorre el ícono, para dar sensación de brillo metálico. */
private fun DrawScope.drawShine(progress: Float, w: Float, h: Float, color: Color) {
    val x = w * progress
    drawLine(color, Offset(x - h * 0.4f, h), Offset(x + h * 0.4f, 0f), strokeWidth = w * 0.12f)
}
