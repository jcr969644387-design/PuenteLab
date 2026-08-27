package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.domain.model.BadgeId
import com.educalab.puentelab.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/** Nivel de dificultad de una insignia: define el metal de la medalla (borde y cinta). */
enum class BadgeTier(val label: String, val emoji: String, val metal: Color, val metalDark: Color) {
    BRONZE("Bronce", "🥉", Color(0xFFCD7F32), Color(0xFF8C5426)),
    SILVER("Plata", "🥈", Color(0xFFC7CDD6), Color(0xFF8A919C)),
    GOLD("Oro", "🥇", Color(0xFFF3C24A), Color(0xFFC9941E))
}

fun tierFor(id: BadgeId): BadgeTier = when (id) {
    BadgeId.PRIMER_PUENTE, BadgeId.EXPLORADOR -> BadgeTier.BRONZE
    BadgeId.MAESTRO_ARCO, BadgeId.INGENIERO_CERCHA, BadgeId.MAESTRO_SUSPENSION -> BadgeTier.SILVER
    BadgeId.PRESUPUESTO_DE_ORO, BadgeId.SIN_FALLOS, BadgeId.COLECCIONISTA, BadgeId.VETERANO -> BadgeTier.GOLD
}

/**
 * Insignia ilustrada con forma de medalla real: cinta colgante, borde metálico según el nivel
 * de dificultad (bronce/plata/oro) y una silueta propia por cada BadgeId dibujada con Canvas.
 */
@Composable
fun BadgeIcon(badgeId: BadgeId, modifier: Modifier = Modifier.size(56.dp), locked: Boolean = false) {
    val tier = tierFor(badgeId)
    Canvas(modifier = modifier) {
        val base = if (locked) Ink600.copy(alpha = 0.35f) else colorFor(badgeId)
        val metal = if (locked) Ink600.copy(alpha = 0.4f) else tier.metal
        val metalDark = if (locked) Ink600.copy(alpha = 0.55f) else tier.metalDark
        drawRibbon(metal, metalDark)
        drawMedallion(base, metal, metalDark, locked)
        if (!locked) drawGlyphFor(badgeId)
    }
}

private fun colorFor(id: BadgeId): Color = when (id) {
    BadgeId.PRIMER_PUENTE -> SiteOrange
    BadgeId.EXPLORADOR -> RiverTeal
    BadgeId.MAESTRO_ARCO -> CanyonTerracotta
    BadgeId.INGENIERO_CERCHA -> Blueprint500
    BadgeId.MAESTRO_SUSPENSION -> CityViolet
    BadgeId.PRESUPUESTO_DE_ORO -> SiteAmber
    BadgeId.SIN_FALLOS -> SuccessGreen
    BadgeId.COLECCIONISTA -> MountainSlate
    BadgeId.VETERANO -> Blueprint900
}

/** Dos pequeñas tiras de cinta bajo el medallón, para que se vea colgado como un reconocimiento. */
private fun DrawScope.drawRibbon(metal: Color, metalDark: Color) {
    val r = size.minDimension / 2f
    val cx = size.width / 2f
    val topY = size.height * 0.52f
    val bottomY = size.height * 0.98f
    val leftRibbon = Path().apply {
        moveTo(cx - r * 0.32f, topY)
        lineTo(cx - r * 0.08f, topY)
        lineTo(cx - r * 0.02f, bottomY)
        lineTo(cx - r * 0.42f, bottomY * 0.92f)
        close()
    }
    val rightRibbon = Path().apply {
        moveTo(cx + r * 0.32f, topY)
        lineTo(cx + r * 0.08f, topY)
        lineTo(cx + r * 0.02f, bottomY)
        lineTo(cx + r * 0.42f, bottomY * 0.92f)
        close()
    }
    drawPath(leftRibbon, metalDark)
    drawPath(rightRibbon, metal)
}

/** Disco central con doble borde metálico y pequeñas marcas alrededor, como una moneda acuñada. */
private fun DrawScope.drawMedallion(color: Color, metal: Color, metalDark: Color, locked: Boolean) {
    val r = size.minDimension * 0.42f
    val center = Offset(size.width / 2f, size.height * 0.42f)
    drawCircle(metalDark, radius = r * 1.14f, center = center)
    drawCircle(metal, radius = r * 1.02f, center = center)
    drawCircle(color, radius = r * 0.86f, center = center)
    drawCircle(
        if (locked) Ink600.copy(alpha = 0.5f) else White.copy(alpha = 0.85f),
        radius = r * 0.7f, center = center, style = Stroke(width = r * 0.09f)
    )
    // pequeñas marcas alrededor del borde, como una moneda acuñada
    val notches = 16
    for (i in 0 until notches) {
        val angle = (2 * Math.PI / notches * i).toFloat()
        val nx = center.x + (r * 1.08f) * cos(angle)
        val ny = center.y + (r * 1.08f) * sin(angle)
        drawCircle(metalDark.copy(alpha = 0.6f), radius = r * 0.045f, center = Offset(nx, ny))
    }
}

private fun DrawScope.drawGlyphFor(id: BadgeId) {
    val center = Offset(size.width / 2f, size.height * 0.42f)
    val r = size.minDimension * 0.42f * 0.86f
    val glyphColor = White
    when (id) {
        BadgeId.PRIMER_PUENTE -> {
            // arco simple
            drawArc(glyphColor, 180f, 180f, useCenter = false, topLeft = Offset(center.x - r * 0.45f, center.y - r * 0.1f), size = androidx.compose.ui.geometry.Size(r * 0.9f, r * 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.14f))
        }
        BadgeId.EXPLORADOR -> {
            // brújula
            drawCircle(glyphColor, radius = r * 0.4f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.08f))
            drawLine(glyphColor, Offset(center.x, center.y - r * 0.35f), Offset(center.x, center.y + r * 0.35f), strokeWidth = r * 0.08f)
        }
        BadgeId.MAESTRO_ARCO -> {
            drawArc(glyphColor, 200f, 140f, useCenter = false, topLeft = Offset(center.x - r * 0.4f, center.y - r * 0.3f), size = androidx.compose.ui.geometry.Size(r * 0.8f, r * 0.7f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.13f))
        }
        BadgeId.INGENIERO_CERCHA -> {
            val p = Path().apply {
                moveTo(center.x - r * 0.4f, center.y + r * 0.3f)
                lineTo(center.x, center.y - r * 0.35f)
                lineTo(center.x + r * 0.4f, center.y + r * 0.3f)
            }
            drawPath(p, glyphColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.12f))
        }
        BadgeId.MAESTRO_SUSPENSION -> {
            drawLine(glyphColor, Offset(center.x, center.y - r * 0.45f), Offset(center.x, center.y + r * 0.35f), strokeWidth = r * 0.1f)
            drawArc(glyphColor, 0f, 180f, false, Offset(center.x - r * 0.4f, center.y - r * 0.1f), androidx.compose.ui.geometry.Size(r * 0.8f, r * 0.35f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.08f))
        }
        BadgeId.PRESUPUESTO_DE_ORO -> {
            drawCircle(glyphColor, radius = r * 0.35f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.1f))
        }
        BadgeId.SIN_FALLOS -> {
            val p = Path().apply {
                moveTo(center.x - r * 0.3f, center.y)
                lineTo(center.x - r * 0.08f, center.y + r * 0.25f)
                lineTo(center.x + r * 0.35f, center.y - r * 0.25f)
            }
            drawPath(p, glyphColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.12f))
        }
        BadgeId.COLECCIONISTA -> {
            for (i in 0 until 5) {
                val angle = Math.toRadians((i * 72 - 90).toDouble())
                val x = center.x + (r * 0.4f * cos(angle)).toFloat()
                val y = center.y + (r * 0.4f * sin(angle)).toFloat()
                drawCircle(glyphColor, radius = r * 0.09f, center = Offset(x, y))
            }
        }
        BadgeId.VETERANO -> {
            drawCircle(glyphColor, radius = r * 0.1f, center = center)
            drawArc(glyphColor, -60f, 300f, false, Offset(center.x - r * 0.4f, center.y - r * 0.4f), androidx.compose.ui.geometry.Size(r * 0.8f, r * 0.8f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.09f))
        }
    }
}
