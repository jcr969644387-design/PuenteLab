package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.domain.model.BadgeId
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/** Insignia de logro: medalla con cinta e icono propio (ver [BadgeIcon]), más el nivel (bronce/plata/oro). */
@Composable
fun BadgeCard(badgeId: BadgeId, name: String, description: String, unlocked: Boolean, modifier: Modifier = Modifier) {
    val tier = tierFor(badgeId)
    Card(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) Blueprint100 else Ink600.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BadgeIcon(badgeId, modifier = Modifier.size(72.dp), locked = !unlocked)
            Spacer(Modifier.height(6.dp))
            Text(
                if (unlocked) "${tier.emoji} ${tier.label}" else "🔒 Bloqueada",
                style = MaterialTheme.typography.labelSmall,
                color = if (unlocked) Ink600 else Ink600.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            Text(name, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                description, style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center, color = Ink600, maxLines = 3
            )
        }
    }
}

/** El sello de un escenario tiene dos niveles: bronce (primer desafío) y oro (el más difícil). */
enum class StampTier(val label: String, val metal: Color, val metalDark: Color) {
    BRONZE("Bronce", Color(0xFFCD7F32), Color(0xFF8C5426)),
    GOLD("Oro", Color(0xFFF3C24A), Color(0xFFC9941E)),
    SPECIAL("Especial", CityViolet, Blueprint900)
}

/** Deduce el nivel del sello a partir de su id (ver SeedBadgesAndStamps.kt: sufijos _bronce/_oro). */
fun stampTierFor(stampId: String): StampTier = when {
    stampId.endsWith("_bronce") -> StampTier.BRONZE
    stampId.endsWith("_oro") -> StampTier.GOLD
    else -> StampTier.SPECIAL
}

/** Sello de Constructor: placa hexagonal tipo estampilla coleccionable, con el paisaje del escenario. */
@Composable
fun StampCard(name: String, unlocked: Boolean, scenario: ScenarioType?, tier: StampTier, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.size(width = 110.dp, height = 138.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) tier.metal.copy(alpha = 0.14f) else Ink600.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StampIcon(scenario = scenario, tier = tier, locked = !unlocked, modifier = Modifier.size(60.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                if (unlocked) "${tier.label} · $name" else name,
                style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center,
                maxLines = 3, color = if (unlocked) Ink900 else Ink600.copy(alpha = 0.5f)
            )
        }
    }
}

/** Placa hexagonal con borde perforado (como una estampilla) y el paisaje del escenario grabado. */
@Composable
fun StampIcon(scenario: ScenarioType?, tier: StampTier, locked: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val metal = if (locked) Ink600.copy(alpha = 0.4f) else tier.metal
        val metalDark = if (locked) Ink600.copy(alpha = 0.55f) else tier.metalDark
        val face = if (locked) Ink600.copy(alpha = 0.15f) else metal.copy(alpha = 0.22f)
        drawHexPlate(metal, metalDark, face)
        val glyphColor = if (locked) Ink600.copy(alpha = 0.5f) else metalDark
        if (scenario != null) drawScenarioGlyph(scenario, glyphColor) else drawLaurelGlyph(glyphColor)
    }
}

private fun DrawScope.drawHexPlate(metal: Color, metalDark: Color, face: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension / 2f

    fun hexPath(radius: Float): Path = Path().apply {
        for (i in 0 until 6) {
            val angle = Math.toRadians((60 * i - 90).toDouble())
            val x = cx + radius * cos(angle).toFloat()
            val y = cy + radius * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    drawPath(hexPath(r), metalDark)
    drawPath(hexPath(r * 0.88f), face)
    drawPath(hexPath(r * 0.88f), metal, style = Stroke(width = r * 0.06f))

    // borde "perforado" tipo estampilla: pequeños puntos a lo largo del hexágono exterior
    val perforations = 18
    for (i in 0 until perforations) {
        val angle = (2 * Math.PI / perforations * i).toFloat()
        val x = cx + (r * 1.02f) * cos(angle)
        val y = cy + (r * 1.02f) * sin(angle)
        drawCircle(metalDark.copy(alpha = 0.5f), radius = r * 0.045f, center = Offset(x, y))
    }
}

private fun DrawScope.drawScenarioGlyph(scenario: ScenarioType, color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension * 0.32f
    when (scenario) {
        ScenarioType.FOREST -> {
            val p = Path().apply {
                moveTo(cx, cy - r * 0.9f)
                lineTo(cx + r * 0.6f, cy + r * 0.3f)
                lineTo(cx - r * 0.6f, cy + r * 0.3f)
                close()
            }
            drawPath(p, color)
            drawLine(color, Offset(cx, cy + r * 0.3f), Offset(cx, cy + r * 0.75f), strokeWidth = r * 0.14f, cap = StrokeCap.Round)
        }
        ScenarioType.RIVER -> {
            val p = Path().apply {
                moveTo(cx - r * 0.8f, cy)
                quadraticBezierTo(cx - r * 0.4f, cy - r * 0.5f, cx, cy)
                quadraticBezierTo(cx + r * 0.4f, cy + r * 0.5f, cx + r * 0.8f, cy)
            }
            drawPath(p, color, style = Stroke(width = r * 0.16f, cap = StrokeCap.Round))
        }
        ScenarioType.CANYON -> {
            val p = Path().apply {
                moveTo(cx - r * 0.8f, cy + r * 0.5f)
                lineTo(cx - r * 0.3f, cy - r * 0.6f)
                lineTo(cx, cy)
                lineTo(cx + r * 0.3f, cy - r * 0.6f)
                lineTo(cx + r * 0.8f, cy + r * 0.5f)
            }
            drawPath(p, color, style = Stroke(width = r * 0.15f))
        }
        ScenarioType.MOUNTAIN -> {
            val p = Path().apply {
                moveTo(cx - r * 0.85f, cy + r * 0.5f)
                lineTo(cx - r * 0.2f, cy - r * 0.75f)
                lineTo(cx + r * 0.15f, cy - r * 0.1f)
                lineTo(cx + r * 0.45f, cy - r * 0.4f)
                lineTo(cx + r * 0.9f, cy + r * 0.5f)
                close()
            }
            drawPath(p, color)
        }
        ScenarioType.CITY -> {
            drawRect(color, topLeft = Offset(cx - r * 0.75f, cy - r * 0.1f), size = androidx.compose.ui.geometry.Size(r * 0.4f, r * 0.85f))
            drawRect(color, topLeft = Offset(cx - r * 0.15f, cy - r * 0.6f), size = androidx.compose.ui.geometry.Size(r * 0.4f, r * 1.35f))
            drawRect(color, topLeft = Offset(cx + r * 0.35f, cy + r * 0.15f), size = androidx.compose.ui.geometry.Size(r * 0.4f, r * 0.6f))
        }
    }
}

/** Laurel simplificado para los dos sellos especiales (maestría estructural y veteranía). */
private fun DrawScope.drawLaurelGlyph(color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension * 0.32f
    drawCircle(color, radius = r * 0.14f, center = Offset(cx, cy))
    for (side in listOf(-1f, 1f)) {
        for (i in 0 until 4) {
            val t = i / 3f
            val leafCx = cx + side * (r * 0.3f + t * r * 0.45f)
            val leafCy = cy + r * 0.55f - t * r * 0.9f
            drawCircle(color, radius = r * 0.13f, center = Offset(leafCx, leafCy))
        }
    }
}
