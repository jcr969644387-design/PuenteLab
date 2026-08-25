package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.ui.theme.*

/**
 * PIVOT, el robot topógrafo del estudio. Personaje guía dibujado íntegramente con Canvas
 * (sin imágenes externas): cuerpo redondeado, antena de plomada, "ojo" de nivel de burbuja.
 */
@Composable
fun PivotCharacter(modifier: Modifier = Modifier, mood: PivotMood = PivotMood.NEUTRAL) {
    Canvas(modifier = modifier.size(96.dp)) {
        val w = size.width
        val h = size.height
        val bodyColor = SiteOrange
        val darkColor = Blueprint900

        // Antena + plomada
        drawLine(darkColor, Offset(w * 0.5f, h * 0.02f), Offset(w * 0.5f, h * 0.18f), strokeWidth = w * 0.03f)
        drawCircle(SiteAmber, radius = w * 0.045f, center = Offset(w * 0.5f, h * 0.14f))

        // Cabeza (cuerpo principal, forma de casco redondeado)
        drawRoundRect(
            color = bodyColor,
            topLeft = Offset(w * 0.18f, h * 0.18f),
            size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.55f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.28f, w * 0.28f)
        )
        // Visor / ojo de nivel
        drawRoundRect(
            color = Blueprint900,
            topLeft = Offset(w * 0.28f, h * 0.34f),
            size = androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.1f, w * 0.1f)
        )
        val eyeColor = when (mood) {
            PivotMood.HAPPY -> SuccessGreen
            PivotMood.THINKING -> SiteAmber
            PivotMood.NEUTRAL -> RiverTeal
        }
        drawCircle(eyeColor, radius = w * 0.07f, center = Offset(w * 0.5f, h * 0.44f))

        // Antenas laterales (orejas técnicas)
        drawCircle(darkColor, radius = w * 0.035f, center = Offset(w * 0.18f, h * 0.42f))
        drawCircle(darkColor, radius = w * 0.035f, center = Offset(w * 0.82f, h * 0.42f))

        // Cuerpo inferior (base con orugas simples)
        drawRoundRect(
            color = Blueprint700,
            topLeft = Offset(w * 0.26f, h * 0.72f),
            size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f, w * 0.08f)
        )
        drawLine(SiteAmber, Offset(w * 0.34f, h * 0.82f), Offset(w * 0.66f, h * 0.82f), strokeWidth = w * 0.025f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

enum class PivotMood { NEUTRAL, HAPPY, THINKING }
