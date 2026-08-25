package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.educalab.puentelab.domain.model.*
import com.educalab.puentelab.ui.theme.*

/**
 * Lienzo interactivo del constructor. Un toque sobre un nodo existente lo selecciona/conecta;
 * un toque sobre una celda vacía coloca un nodo libre (o un apoyo de pago si pierMode está activo).
 */
@Composable
fun BuilderCanvasView(
    design: BridgeDesignSpec,
    materialsById: Map<String, MaterialSpec>,
    pendingNodeId: String?,
    spanUnits: Double,
    onTapNode: (String) -> Unit,
    onTapEmpty: (GridPoint) -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Canvas(
        modifier = modifier.pointerInput(design.nodes) {
            detectTapGestures { tapOffset ->
                val mapper = mapperFor(size.width.toFloat(), size.height.toFloat(), spanUnits)
                val hitNode = design.nodes.minByOrNull { mapper.toOffset(it.point).distanceTo(tapOffset) }
                    ?.takeIf { mapper.toOffset(it.point).distanceTo(tapOffset) < 60f }
                if (hitNode != null) onTapNode(hitNode.id)
                else onTapEmpty(mapper.toGridPoint(tapOffset))
            }
        }
    ) {
        val mapper = mapperFor(size.width, size.height, spanUnits)

        // cuadrícula de referencia
        val gridColor = Blueprint300.copy(alpha = 0.25f)
        var gx = 0.0
        while (gx <= spanUnits) {
            drawLine(gridColor, mapper.toOffset(GridPoint(gx, -4.0)), mapper.toOffset(GridPoint(gx, 4.0)), strokeWidth = 1f)
            gx += 1.0
        }
        var gy = -4.0
        while (gy <= 4.0) {
            drawLine(gridColor, mapper.toOffset(GridPoint(0.0, gy)), mapper.toOffset(GridPoint(spanUnits, gy)), strokeWidth = 1f)
            gy += 1.0
        }

        // barras
        val nodesById = design.nodes.associateBy { it.id }
        for (member in design.members) {
            val a = nodesById[member.nodeAId]?.point ?: continue
            val b = nodesById[member.nodeBId]?.point ?: continue
            val color = colorForRole(member.role)
            drawLine(
                color = color,
                start = mapper.toOffset(a), end = mapper.toOffset(b),
                strokeWidth = if (member.role == MemberRole.DECK) 10f else 5f,
                cap = StrokeCap.Round
            )
        }

        // nodos
        for (node in design.nodes) {
            val offset = mapper.toOffset(node.point)
            val isPending = node.id == pendingNodeId
            val baseColor = when {
                node.anchorSide != AnchorSide.NONE -> Blueprint900
                node.isFixedByLevel -> SiteAmber
                node.isUserPier -> CanyonTerracotta
                else -> SiteOrange
            }
            if (isPending) {
                drawCircle(SuccessGreen.copy(alpha = 0.35f), radius = 26f, center = offset)
            }
            drawCircle(baseColor, radius = 16f, center = offset)
            drawCircle(Color.White, radius = 16f, center = offset, style = Stroke(width = 3f))
        }
    }
}

private fun colorForRole(role: MemberRole): Color = when (role) {
    MemberRole.DECK -> SiteOrange
    MemberRole.BRACE -> Blueprint500
    MemberRole.CABLE -> RiverTeal
    MemberRole.TOWER -> MountainSlate
}

private fun mapperFor(widthPx: Float, heightPx: Float, spanUnits: Double): GridToCanvasMapper {
    val margin = 40f
    val usableWidth = widthPx - margin * 2
    val unitPx = (usableWidth / spanUnits.toFloat()).coerceAtLeast(20f)
    return GridToCanvasMapper(unitPx = unitPx, originXPx = margin, originYPx = heightPx * 0.6f)
}

private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x; val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}
