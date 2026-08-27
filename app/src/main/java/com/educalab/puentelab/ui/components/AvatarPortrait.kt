package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.data.seed.AvatarOption
import com.educalab.puentelab.data.seed.HairStyle
import com.educalab.puentelab.ui.theme.Blueprint900
import com.educalab.puentelab.ui.theme.PaperBg
import com.educalab.puentelab.ui.theme.SiteAmber

/**
 * Retrato de niño/a ingeniero: casco de color propio, pelo, cara sonriente y chaleco de
 * seguridad. Dibujado íntegramente con Canvas (sin imágenes externas), para que cada avatar
 * tenga personalidad propia en vez de ser un círculo con un cuadrado de color.
 */
@Composable
fun AvatarPortrait(avatar: AvatarOption, modifier: Modifier = Modifier) {
    val helmet = parseColorOrDefault(avatar.helmetHex, SiteAmber)
    val skin = parseColorOrDefault(avatar.skinHex, Color(0xFFE8B08A))
    val hair = parseColorOrDefault(avatar.hairHex, Blueprint900)

    Canvas(modifier.clip(CircleShape).background(PaperBg)) {
        val w = size.width; val h = size.height

        // chaleco de seguridad (naranja, con franja reflectante)
        drawRoundRect(
            color = Color(0xFFF2994A),
            topLeft = Offset(w * 0.2f, h * 0.72f),
            size = Size(w * 0.6f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
        )
        drawLine(Color.White, Offset(w * 0.3f, h * 0.8f), Offset(w * 0.3f, h * 1.0f), strokeWidth = w * 0.04f)
        drawLine(Color.White, Offset(w * 0.7f, h * 0.8f), Offset(w * 0.7f, h * 1.0f), strokeWidth = w * 0.04f)

        // cuello
        drawRoundRect(skin, topLeft = Offset(w * 0.42f, h * 0.62f), size = Size(w * 0.16f, h * 0.16f), cornerRadius = CornerRadius(w * 0.04f))

        // cara
        drawCircle(skin, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.46f))

        // pelo (detrás/alrededor de la cara, antes del casco para que el casco lo tape arriba)
        drawHair(avatar.hairStyle, hair, w, h)

        // casco
        drawArc(
            color = helmet,
            startAngle = 180f, sweepAngle = 180f, useCenter = true,
            topLeft = Offset(w * 0.18f, h * 0.06f), size = Size(w * 0.64f, h * 0.5f)
        )
        drawRoundRect(
            color = helmet,
            topLeft = Offset(w * 0.14f, h * 0.28f),
            size = Size(w * 0.72f, h * 0.09f),
            cornerRadius = CornerRadius(w * 0.04f)
        )
        // luz frontal del casco
        drawCircle(SiteAmber, radius = w * 0.035f, center = Offset(w * 0.5f, h * 0.18f))

        // ojos y sonrisa (expresión alegre)
        drawCircle(Blueprint900, radius = w * 0.025f, center = Offset(w * 0.42f, h * 0.46f))
        drawCircle(Blueprint900, radius = w * 0.025f, center = Offset(w * 0.58f, h * 0.46f))
        val smile = Path().apply {
            moveTo(w * 0.42f, h * 0.54f)
            quadraticBezierTo(w * 0.5f, h * 0.6f, w * 0.58f, h * 0.54f)
        }
        drawPath(smile, Blueprint900, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.02f))
        // mejillas
        drawCircle(Color(0xFFF2994A).copy(alpha = 0.35f), radius = w * 0.045f, center = Offset(w * 0.34f, h * 0.5f))
        drawCircle(Color(0xFFF2994A).copy(alpha = 0.35f), radius = w * 0.045f, center = Offset(w * 0.66f, h * 0.5f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHair(style: HairStyle, color: Color, w: Float, h: Float) {
    when (style) {
        HairStyle.SHORT -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.2f, h * 0.24f), size = Size(w * 0.6f, h * 0.24f))
        }
        HairStyle.BUZZCUT -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.22f, h * 0.27f), size = Size(w * 0.56f, h * 0.14f))
        }
        HairStyle.CURLY -> {
            val positions = listOf(0.28f to 0.32f, 0.4f to 0.26f, 0.5f to 0.24f, 0.6f to 0.26f, 0.72f to 0.32f)
            positions.forEach { (dx, dy) -> drawCircle(color, radius = w * 0.07f, center = Offset(w * dx, h * dy)) }
        }
        HairStyle.PONYTAIL -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.2f, h * 0.24f), size = Size(w * 0.6f, h * 0.22f))
            drawOval(color, topLeft = Offset(w * 0.72f, h * 0.32f), size = Size(w * 0.12f, h * 0.22f))
        }
    }
}

private fun parseColorOrDefault(hex: String, default: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(default)
