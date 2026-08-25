package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.domain.model.BadgeId
import com.educalab.puentelab.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/** Insignia ilustrada. Cada BadgeId tiene una silueta propia dibujada con Canvas. */
@Composable
fun BadgeIcon(badgeId: BadgeId, modifier: Modifier = Modifier.size(56.dp), locked: Boolean = false) {
    Canvas(modifier = modifier) {
        val base = if (locked) Ink600.copy(alpha = 0.35f) else colorFor(badgeId)
        drawMedallion(base, locked)
        if (!locked) drawGlyphFor(badgeId)
    }
}

private fun colorFor(id: BadgeId): androidx.compose.ui.graphics.Color = when (id) {
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

private fun DrawScope.drawMedallion(color: androidx.compose.ui.graphics.Color, locked: Boolean) {
    val r = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    drawCircle(color, radius = r, center = center)
    drawCircle(if (locked) Ink600.copy(alpha = 0.5f) else White.copy(alpha = 0.85f), radius = r * 0.78f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.08f))
}

private fun DrawScope.drawGlyphFor(id: BadgeId) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension / 2f
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
