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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/** Medallón dibujado con Canvas: un círculo con una estrella/engranaje simplificado dentro. */
@Composable
fun MedallionIcon(unlocked: Boolean, primaryColor: Color = SiteAmber, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val ringColor = if (unlocked) primaryColor else Ink600.copy(alpha = 0.3f)
        drawCircle(ringColor, radius = r, center = center, style = Stroke(width = r * 0.18f))
        drawCircle(if (unlocked) primaryColor.copy(alpha = 0.18f) else Ink600.copy(alpha = 0.08f), radius = r * 0.75f, center = center)

        // estrella de 5 puntas simplificada
        val points = 5
        val outerR = r * 0.5f
        val innerR = r * 0.22f
        val path = androidx.compose.ui.graphics.Path()
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerR else innerR
            val angle = (Math.PI / points * i - Math.PI / 2).toFloat()
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, if (unlocked) primaryColor else Ink600.copy(alpha = 0.35f))
    }
}

@Composable
fun BadgeCard(name: String, description: String, unlocked: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) Blueprint100 else Ink600.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MedallionIcon(unlocked = unlocked)
            Spacer(Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                description, style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center, color = Ink600, maxLines = 3
            )
        }
    }
}

@Composable
fun StampCard(name: String, unlocked: Boolean, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.size(width = 110.dp, height = 130.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) accentColor.copy(alpha = 0.15f) else Ink600.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MedallionIcon(unlocked = unlocked, primaryColor = accentColor, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                name, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center,
                maxLines = 3, color = if (unlocked) Ink900 else Ink600.copy(alpha = 0.5f)
            )
        }
    }
}
