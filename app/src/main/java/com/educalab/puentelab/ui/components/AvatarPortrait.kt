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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.data.seed.AvatarOption
import com.educalab.puentelab.data.seed.Expression
import com.educalab.puentelab.data.seed.HairStyle
import com.educalab.puentelab.ui.theme.Blueprint900
import com.educalab.puentelab.ui.theme.PaperBg
import com.educalab.puentelab.ui.theme.SiteAmber

/**
 * Retrato de niño/a ingeniero: casco de color propio, peinado, rasgos y chaleco de seguridad.
 * Dibujado íntegramente con Canvas (sin imágenes externas). Chicos y chicas se diferencian
 * claramente por el peinado (largo/coletas/trenza/melena vs. corto/rizado/ondulado/rapado) y la
 * expresión, nunca solo por el color de la ropa.
 */
@Composable
fun AvatarPortrait(avatar: AvatarOption, modifier: Modifier = Modifier) {
    val helmet = parseColorOrDefault(avatar.helmetHex, SiteAmber)
    val skin = parseColorOrDefault(avatar.skinHex, Color(0xFFE8B08A))
    val hair = parseColorOrDefault(avatar.hairHex, Blueprint900)

    Canvas(modifier.clip(CircleShape).background(PaperBg)) {
        val w = size.width; val h = size.height

        // chaleco de seguridad (color propio del avatar, con franja reflectante)
        drawRoundRect(
            color = helmet,
            topLeft = Offset(w * 0.2f, h * 0.72f),
            size = Size(w * 0.6f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
        )
        drawLine(Color.White, Offset(w * 0.3f, h * 0.8f), Offset(w * 0.3f, h * 1.0f), strokeWidth = w * 0.04f)
        drawLine(Color.White, Offset(w * 0.7f, h * 0.8f), Offset(w * 0.7f, h * 1.0f), strokeWidth = w * 0.04f)

        // cuello
        drawRoundRect(skin, topLeft = Offset(w * 0.42f, h * 0.62f), size = Size(w * 0.16f, h * 0.16f), cornerRadius = CornerRadius(w * 0.04f))

        // pelo largo (detrás de la cara): las coletas, trenza y melena se apoyan sobre los hombros
        drawLongHairBack(avatar.hairStyle, hair, w, h)

        // cara
        drawCircle(skin, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.46f))

        // pelo (alrededor de la cara, antes del casco para que el casco lo tape arriba)
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

        // rostro: cejas, ojos y boca varían según la expresión de cada avatar
        drawFace(avatar.expression, w, h)

        // mejillas
        drawCircle(helmet.copy(alpha = 0.3f), radius = w * 0.045f, center = Offset(w * 0.34f, h * 0.5f))
        drawCircle(helmet.copy(alpha = 0.3f), radius = w * 0.045f, center = Offset(w * 0.66f, h * 0.5f))
    }
}

/** Mechones que caen por detrás de los hombros (coletas, trenza, melena larga): se dibujan antes que la cara. */
private fun DrawScope.drawLongHairBack(style: HairStyle, color: Color, w: Float, h: Float) {
    when (style) {
        HairStyle.LONG -> {
            drawOval(color, topLeft = Offset(w * 0.1f, h * 0.34f), size = Size(w * 0.22f, h * 0.46f))
            drawOval(color, topLeft = Offset(w * 0.68f, h * 0.34f), size = Size(w * 0.22f, h * 0.46f))
        }
        HairStyle.TWIN_TAILS -> {
            drawOval(color, topLeft = Offset(w * 0.08f, h * 0.38f), size = Size(w * 0.16f, h * 0.3f))
            drawOval(color, topLeft = Offset(w * 0.76f, h * 0.38f), size = Size(w * 0.16f, h * 0.3f))
            drawCircle(SiteAmber, radius = w * 0.028f, center = Offset(w * 0.16f, h * 0.41f))
            drawCircle(SiteAmber, radius = w * 0.028f, center = Offset(w * 0.84f, h * 0.41f))
        }
        HairStyle.BRAID -> {
            val braidPositions = listOf(0.78f to 0.4f, 0.75f to 0.48f, 0.78f to 0.56f, 0.75f to 0.64f)
            braidPositions.forEach { (dx, dy) -> drawOval(color, topLeft = Offset(w * dx, h * dy), size = Size(w * 0.11f, h * 0.1f)) }
            drawCircle(SiteAmber, radius = w * 0.022f, center = Offset(w * 0.79f, h * 0.68f))
        }
        else -> Unit
    }
}

private fun DrawScope.drawHair(style: HairStyle, color: Color, w: Float, h: Float) {
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
        HairStyle.WAVY -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.2f, h * 0.24f), size = Size(w * 0.6f, h * 0.22f))
            // mechón ondulado barrido hacia un lado, para distinguirlo del corte corto liso
            val swoosh = Path().apply {
                moveTo(w * 0.24f, h * 0.3f)
                quadraticBezierTo(w * 0.4f, h * 0.18f, w * 0.6f, h * 0.27f)
                quadraticBezierTo(w * 0.46f, h * 0.24f, w * 0.3f, h * 0.35f)
                close()
            }
            drawPath(swoosh, color)
        }
        HairStyle.LONG -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.2f, h * 0.22f), size = Size(w * 0.6f, h * 0.26f))
        }
        HairStyle.TWIN_TAILS -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.2f, h * 0.24f), size = Size(w * 0.6f, h * 0.22f))
        }
        HairStyle.BRAID -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.2f, h * 0.24f), size = Size(w * 0.6f, h * 0.22f))
            // raya lateral marcada, característica de la trenza
            drawLine(color, Offset(w * 0.38f, h * 0.25f), Offset(w * 0.34f, h * 0.34f), strokeWidth = w * 0.015f)
        }
        HairStyle.BOB -> {
            drawArc(color, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.18f, h * 0.24f), size = Size(w * 0.64f, h * 0.28f))
            // puntas que abren hacia afuera a la altura de la mandíbula (media melena)
            drawOval(color, topLeft = Offset(w * 0.13f, h * 0.4f), size = Size(w * 0.15f, h * 0.2f))
            drawOval(color, topLeft = Offset(w * 0.72f, h * 0.4f), size = Size(w * 0.15f, h * 0.2f))
        }
    }
}

/** Cejas, ojos y boca según la expresión: cada avatar tiene su propia personalidad en el gesto. */
private fun DrawScope.drawFace(expression: Expression, w: Float, h: Float) {
    val eyeY = h * 0.46f
    val leftEyeX = w * 0.42f
    val rightEyeX = w * 0.58f
    val ink = Blueprint900

    when (expression) {
        Expression.SMILE -> {
            drawLine(ink, Offset(leftEyeX - w * 0.05f, eyeY - h * 0.08f), Offset(leftEyeX + w * 0.03f, eyeY - h * 0.085f), strokeWidth = w * 0.018f, cap = StrokeCap.Round)
            drawLine(ink, Offset(rightEyeX - w * 0.03f, eyeY - h * 0.085f), Offset(rightEyeX + w * 0.05f, eyeY - h * 0.08f), strokeWidth = w * 0.018f, cap = StrokeCap.Round)
        }
        Expression.GRIN -> {
            drawArc(ink, 200f, 60f, false, Offset(leftEyeX - w * 0.07f, eyeY - h * 0.16f), Size(w * 0.11f, h * 0.07f), style = Stroke(width = w * 0.016f, cap = StrokeCap.Round))
            drawArc(ink, -20f, 60f, false, Offset(rightEyeX - w * 0.04f, eyeY - h * 0.16f), Size(w * 0.11f, h * 0.07f), style = Stroke(width = w * 0.016f, cap = StrokeCap.Round))
        }
        Expression.CHEERFUL -> {
            drawArc(ink, 200f, 40f, false, Offset(leftEyeX - w * 0.06f, eyeY - h * 0.11f), Size(w * 0.09f, h * 0.05f), style = Stroke(width = w * 0.016f, cap = StrokeCap.Round))
            drawArc(ink, -20f, 40f, false, Offset(rightEyeX - w * 0.03f, eyeY - h * 0.11f), Size(w * 0.09f, h * 0.05f), style = Stroke(width = w * 0.016f, cap = StrokeCap.Round))
            // pequeñas pecas: un detalle propio de esta expresión
            listOf(0.31f to 0.5f, 0.35f to 0.52f, 0.69f to 0.5f, 0.65f to 0.52f).forEach { (dx, dy) ->
                drawCircle(ink.copy(alpha = 0.35f), radius = w * 0.006f, center = Offset(w * dx, h * dy))
            }
        }
        Expression.SMIRK -> {
            // una ceja más alta que la otra: gesto seguro y pícaro
            drawLine(ink, Offset(leftEyeX - w * 0.05f, eyeY - h * 0.12f), Offset(leftEyeX + w * 0.04f, eyeY - h * 0.075f), strokeWidth = w * 0.018f, cap = StrokeCap.Round)
            drawLine(ink, Offset(rightEyeX - w * 0.04f, eyeY - h * 0.065f), Offset(rightEyeX + w * 0.05f, eyeY - h * 0.065f), strokeWidth = w * 0.018f, cap = StrokeCap.Round)
        }
    }

    drawCircle(ink, radius = w * 0.025f, center = Offset(leftEyeX, eyeY))
    drawCircle(ink, radius = w * 0.025f, center = Offset(rightEyeX, eyeY))

    when (expression) {
        Expression.SMILE -> {
            val smile = Path().apply {
                moveTo(w * 0.42f, h * 0.54f)
                quadraticBezierTo(w * 0.5f, h * 0.6f, w * 0.58f, h * 0.54f)
            }
            drawPath(smile, ink, style = Stroke(width = w * 0.02f, cap = StrokeCap.Round))
        }
        Expression.GRIN -> {
            val grin = Path().apply {
                moveTo(w * 0.4f, h * 0.53f)
                quadraticBezierTo(w * 0.5f, h * 0.63f, w * 0.6f, h * 0.53f)
                quadraticBezierTo(w * 0.5f, h * 0.58f, w * 0.4f, h * 0.53f)
                close()
            }
            drawPath(grin, ink)
            drawPath(grin, Color.White, style = Stroke(width = w * 0.008f))
        }
        Expression.CHEERFUL -> {
            val smile = Path().apply {
                moveTo(w * 0.41f, h * 0.535f)
                quadraticBezierTo(w * 0.5f, h * 0.605f, w * 0.59f, h * 0.535f)
            }
            drawPath(smile, ink, style = Stroke(width = w * 0.022f, cap = StrokeCap.Round))
        }
        Expression.SMIRK -> {
            val smirk = Path().apply {
                moveTo(w * 0.42f, h * 0.55f)
                quadraticBezierTo(w * 0.52f, h * 0.6f, w * 0.6f, h * 0.51f)
            }
            drawPath(smirk, ink, style = Stroke(width = w * 0.02f, cap = StrokeCap.Round))
        }
    }
}

private fun parseColorOrDefault(hex: String, default: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(default)
