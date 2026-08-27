package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.educalab.puentelab.ui.theme.*

/** Página 1: dos chicos construyendo un puente de juguete en un taller, con planos sobre la mesa. */
@Composable
fun WorkshopIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height

        // mesa de trabajo
        drawRoundRect(Color(0xFFB4783C), Offset(w * 0.05f, h * 0.66f), Size(w * 0.9f, h * 0.08f), CornerRadius(6f, 6f))
        drawRect(Color(0xFF8C5E2C), Offset(w * 0.1f, h * 0.74f), Size(w * 0.08f, h * 0.18f))
        drawRect(Color(0xFF8C5E2C), Offset(w * 0.82f, h * 0.74f), Size(w * 0.08f, h * 0.18f))

        // plano (blueprint) sobre la mesa
        drawRoundRect(Blueprint700, Offset(w * 0.36f, h * 0.56f), Size(w * 0.28f, h * 0.16f), CornerRadius(4f, 4f))
        val gridColor = Blueprint300.copy(alpha = 0.6f)
        var gx = 0.4f
        while (gx < 0.62f) { drawLine(gridColor, Offset(w * gx, h * 0.57f), Offset(w * gx, h * 0.7f), strokeWidth = 1f); gx += 0.06f }

        // puentecito de juguete sobre el plano
        drawLine(SiteOrange, Offset(w * 0.4f, h * 0.6f), Offset(w * 0.6f, h * 0.6f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawLine(MountainSlate, Offset(w * 0.44f, h * 0.6f), Offset(w * 0.44f, h * 0.68f), strokeWidth = 4f)
        drawLine(MountainSlate, Offset(w * 0.56f, h * 0.6f), Offset(w * 0.56f, h * 0.68f), strokeWidth = 4f)

        // niña (izquierda): casco naranja
        drawChild(this, cx = w * 0.24f, groundY = h * 0.66f, scale = w * 0.22f, helmet = SiteOrange, skin = Color(0xFFE8B08A), armsUp = true)
        // niño (derecha): casco azul
        drawChild(this, cx = w * 0.78f, groundY = h * 0.66f, scale = w * 0.2f, helmet = Blueprint500, skin = Color(0xFF8D5A3C), armsUp = false)

        // herramientas flotando (destornillador simple) para dar ambiente creativo
        drawLine(SiteAmber, Offset(w * 0.14f, h * 0.5f), Offset(w * 0.2f, h * 0.42f), strokeWidth = 4f, cap = StrokeCap.Round)
    }
}

private fun drawChild(scope: androidx.compose.ui.graphics.drawscope.DrawScope, cx: Float, groundY: Float, scale: Float, helmet: Color, skin: Color, armsUp: Boolean) {
    with(scope) {
        val headR = scale * 0.22f
        val headY = groundY - scale * 0.85f
        // cuerpo (chaleco)
        drawRoundRect(
            SiteOrange, topLeft = Offset(cx - scale * 0.18f, headY + headR * 0.6f),
            size = Size(scale * 0.36f, scale * 0.4f), cornerRadius = CornerRadius(scale * 0.08f)
        )
        // brazos
        val armY = if (armsUp) headY else headY + headR * 1.2f
        drawLine(skin, Offset(cx - scale * 0.18f, headY + headR), Offset(cx - scale * 0.32f, armY), strokeWidth = scale * 0.07f, cap = StrokeCap.Round)
        drawLine(skin, Offset(cx + scale * 0.18f, headY + headR), Offset(cx + scale * 0.32f, armY), strokeWidth = scale * 0.07f, cap = StrokeCap.Round)
        // piernas
        drawLine(Blueprint900, Offset(cx - scale * 0.08f, groundY), Offset(cx - scale * 0.1f, groundY + scale * 0.02f), strokeWidth = scale * 0.09f, cap = StrokeCap.Round)
        drawLine(Blueprint900, Offset(cx + scale * 0.08f, groundY), Offset(cx + scale * 0.1f, groundY + scale * 0.02f), strokeWidth = scale * 0.09f, cap = StrokeCap.Round)
        // cabeza
        drawCircle(skin, radius = headR, center = Offset(cx, headY))
        // casco
        drawArc(helmet, 180f, 180f, true, topLeft = Offset(cx - headR * 1.15f, headY - headR * 1.25f), size = Size(headR * 2.3f, headR * 1.7f))
        drawRoundRect(helmet, Offset(cx - headR * 1.2f, headY - headR * 0.3f), Size(headR * 2.4f, headR * 0.35f), CornerRadius(headR * 0.15f))
        // sonrisa
        val smile = Path().apply { moveTo(cx - headR * 0.35f, headY + headR * 0.15f); quadraticTo(cx, headY + headR * 0.5f, cx + headR * 0.35f, headY + headR * 0.15f) }
        drawPath(smile, Blueprint900, style = Stroke(width = headR * 0.12f))
    }
}

/** Página 3: puente terminado mostrando cada parte (calzada, torres, cables, riostras, nodos). */
@Composable
fun ExampleBridgeIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val deckY = h * 0.62f
        val towerX1 = w * 0.3f; val towerX2 = w * 0.7f
        val towerTopY = h * 0.2f

        // orillas
        drawRect(ForestGreen.copy(alpha = 0.3f), Offset(0f, deckY), Size(w * 0.12f, h * 0.2f))
        drawRect(ForestGreen.copy(alpha = 0.3f), Offset(w * 0.88f, deckY), Size(w * 0.12f, h * 0.2f))

        // torres
        drawRect(MountainSlate, Offset(towerX1 - w * 0.02f, towerTopY), Size(w * 0.04f, deckY - towerTopY))
        drawRect(MountainSlate, Offset(towerX2 - w * 0.02f, towerTopY), Size(w * 0.04f, deckY - towerTopY))

        // cables (desde la punta de cada torre hasta varios puntos de la calzada)
        val cableColor = RiverTeal
        listOf(0.08f, 0.3f).forEach { d -> drawLine(cableColor, Offset(towerX1, towerTopY), Offset(towerX1 - w * d, deckY), strokeWidth = 2.5f) }
        listOf(0.08f, 0.3f).forEach { d -> drawLine(cableColor, Offset(towerX1, towerTopY), Offset(towerX1 + w * d, deckY), strokeWidth = 2.5f) }
        listOf(0.08f, 0.3f).forEach { d -> drawLine(cableColor, Offset(towerX2, towerTopY), Offset(towerX2 - w * d, deckY), strokeWidth = 2.5f) }
        listOf(0.08f, 0.3f).forEach { d -> drawLine(cableColor, Offset(towerX2, towerTopY), Offset(towerX2 + w * d, deckY), strokeWidth = 2.5f) }

        // riostras diagonales bajo la calzada
        val braceColor = Blueprint500
        var bx = w * 0.14f
        while (bx < w * 0.86f) {
            drawLine(braceColor, Offset(bx, deckY), Offset(bx + w * 0.08f, deckY + h * 0.08f), strokeWidth = 3f)
            bx += w * 0.16f
        }

        // calzada
        drawLine(SiteOrange, Offset(w * 0.06f, deckY), Offset(w * 0.94f, deckY), strokeWidth = 10f, cap = StrokeCap.Round)

        // nodos visibles a lo largo de la calzada y en las torres
        val nodeXs = listOf(0.06f, 0.22f, 0.38f, 0.5f, 0.62f, 0.78f, 0.94f)
        nodeXs.forEach { nx ->
            drawCircle(Blueprint900, radius = 6f, center = Offset(w * nx, deckY))
            drawCircle(Color.White, radius = 6f, center = Offset(w * nx, deckY), style = Stroke(width = 2f))
        }
        drawCircle(Blueprint900, radius = 6f, center = Offset(towerX1, towerTopY))
        drawCircle(Color.White, radius = 6f, center = Offset(towerX1, towerTopY), style = Stroke(width = 2f))
        drawCircle(Blueprint900, radius = 6f, center = Offset(towerX2, towerTopY))
        drawCircle(Color.White, radius = 6f, center = Offset(towerX2, towerTopY), style = Stroke(width = 2f))
    }
}

/** Página 4: escudo con candado, para transmitir seguridad y privacidad. */
@Composable
fun SecurityIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val cx = w * 0.5f

        val shield = Path().apply {
            moveTo(cx, h * 0.06f)
            lineTo(w * 0.82f, h * 0.2f)
            lineTo(w * 0.82f, h * 0.55f)
            quadraticTo(w * 0.82f, h * 0.82f, cx, h * 0.96f)
            quadraticTo(w * 0.18f, h * 0.82f, w * 0.18f, h * 0.55f)
            lineTo(w * 0.18f, h * 0.2f)
            close()
        }
        drawPath(shield, Blueprint700)
        drawPath(shield, White, style = Stroke(width = 4f))

        // candado dentro del escudo
        val lockW = w * 0.26f; val lockH = h * 0.2f
        val lockLeft = cx - lockW / 2f
        val lockTop = h * 0.5f
        drawArc(
            color = SiteAmber, startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(lockLeft + lockW * 0.12f, lockTop - lockH * 0.55f),
            size = Size(lockW * 0.76f, lockH * 0.7f),
            style = Stroke(width = w * 0.035f)
        )
        drawRoundRect(SiteAmber, Offset(lockLeft, lockTop), Size(lockW, lockH), CornerRadius(w * 0.03f))
        drawCircle(Blueprint900, radius = w * 0.02f, center = Offset(cx, lockTop + lockH * 0.45f))

        // "check" de confianza arriba del escudo
        val check = Path().apply {
            moveTo(cx - w * 0.06f, h * 0.32f)
            lineTo(cx - w * 0.01f, h * 0.38f)
            lineTo(cx + w * 0.09f, h * 0.24f)
        }
        drawPath(check, SuccessGreen, style = Stroke(width = w * 0.035f, cap = StrokeCap.Round))
    }
}
