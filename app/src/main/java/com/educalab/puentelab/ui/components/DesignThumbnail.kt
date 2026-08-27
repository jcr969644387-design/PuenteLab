package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.educalab.puentelab.domain.model.BridgeDesignSpec
import com.educalab.puentelab.domain.model.GridPoint
import com.educalab.puentelab.domain.model.MemberRole
import com.educalab.puentelab.ui.theme.*

/**
 * Miniatura del puente guardado: dibuja sus barras y nodos reales (no un ícono genérico),
 * encuadrando automáticamente según el propio diseño en vez de depender del tamaño del desafío.
 */
@Composable
fun DesignThumbnail(design: BridgeDesignSpec, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (design.nodes.isEmpty()) return@Canvas
        val minX = design.nodes.minOf { it.point.x }
        val maxX = design.nodes.maxOf { it.point.x }
        val minY = design.nodes.minOf { it.point.y }
        val maxY = design.nodes.maxOf { it.point.y }
        val spanX = (maxX - minX).coerceAtLeast(1.0).toFloat()
        val spanY = (maxY - minY).coerceAtLeast(1.0).toFloat()

        val padding = size.minDimension * 0.16f
        val usableW = (size.width - padding * 2).coerceAtLeast(1f)
        val usableH = (size.height - padding * 2).coerceAtLeast(1f)
        val scale = minOf(usableW / spanX, usableH / spanY)
        val drawW = spanX * scale
        val drawH = spanY * scale
        val offsetX = padding + (usableW - drawW) / 2f
        val offsetY = padding + (usableH - drawH) / 2f

        fun toOffset(p: GridPoint): Offset = Offset(
            offsetX + (p.x - minX).toFloat() * scale,
            offsetY + (maxY - p.y).toFloat() * scale
        )

        val nodesById = design.nodes.associateBy { it.id }
        for (member in design.members) {
            val a = nodesById[member.nodeAId]?.point ?: continue
            val b = nodesById[member.nodeBId]?.point ?: continue
            drawLine(
                color = colorForRole(member.role),
                start = toOffset(a), end = toOffset(b),
                strokeWidth = if (member.role == MemberRole.DECK) size.minDimension * 0.05f else size.minDimension * 0.028f,
                cap = StrokeCap.Round
            )
        }
        for (node in design.nodes) {
            drawCircle(Blueprint900.copy(alpha = 0.55f), radius = size.minDimension * 0.025f, center = toOffset(node.point))
        }
    }
}

private fun colorForRole(role: MemberRole): Color = when (role) {
    MemberRole.DECK -> SiteOrange
    MemberRole.BRACE -> Blueprint500
    MemberRole.CABLE -> RiverTeal
    MemberRole.TOWER -> MountainSlate
}
