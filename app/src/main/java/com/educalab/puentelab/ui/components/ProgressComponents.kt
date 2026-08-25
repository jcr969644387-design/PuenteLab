package com.educalab.puentelab.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.domain.model.ModuleState
import com.educalab.puentelab.ui.theme.*

@Composable
fun XpProgressBar(progress: Float, modifier: Modifier = Modifier, trackColor: Color = Blueprint100, fillColor: Color = SiteOrange) {
    val animated by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(500), label = "xp")
    Box(
        modifier = modifier
            .height(14.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(50))
                .background(fillColor)
        )
    }
}

@Composable
fun StarRow(stars: Int, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { i ->
            Icon(
                imageVector = if (i < stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (i < stars) "Estrella conseguida" else "Estrella pendiente",
                tint = if (i < stars) SiteAmber else Ink600.copy(alpha = 0.4f),
                modifier = Modifier.size(size)
            )
        }
    }
}

/** Estado del módulo indicado con icono + texto, nunca solo con color (requisito de accesibilidad). */
@Composable
fun ModuleStateChip(state: ModuleState, modifier: Modifier = Modifier) {
    val (icon, label, color) = when (state) {
        ModuleState.LOCKED -> Triple(Icons.Filled.Lock, "Bloqueado", Ink600)
        ModuleState.AVAILABLE -> Triple(Icons.Filled.PlayArrow, "Disponible", Blueprint500)
        ModuleState.STARTED -> Triple(Icons.Filled.Build, "En progreso", SiteOrange)
        ModuleState.COMPLETED -> Triple(Icons.Filled.CheckCircle, "Completado", SuccessGreen)
        ModuleState.MASTERED -> Triple(Icons.Filled.EmojiEvents, "Dominado", SiteAmber)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun LevelBadgeCircle(level: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Blueprint700),
        contentAlignment = Alignment.Center
    ) {
        Text("Nv.$level", color = White, style = MaterialTheme.typography.labelLarge)
    }
}
