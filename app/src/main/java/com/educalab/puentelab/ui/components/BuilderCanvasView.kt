package com.educalab.puentelab.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.educalab.puentelab.domain.model.*
import com.educalab.puentelab.ui.theme.*

/**
 * Lienzo interactivo del constructor. Modo normal: un toque sobre un nodo existente lo
 * selecciona/conecta; un toque sobre una celda vacía coloca un nodo libre (o un apoyo de pago
 * si pierMode está activo). Modo borrar: un toque sobre un nodo libre o una barra la elimina.
 */
@Composable
fun BuilderCanvasView(
    design: BridgeDesignSpec,
    materialsById: Map<String, MaterialSpec>,
    pendingNodeId: String?,
    spanUnits: Double,
    deleteMode: Boolean = false,
    fadingNodeId: String? = null,
    fadingMemberId: String? = null,
    fadingAlpha: Float = 1f,
    onTapNode: (String) -> Unit,
    onTapEmpty: (GridPoint) -> Unit,
    onDeleteNode: (String) -> Unit = {},
    onDeleteMember: (String) -> Unit = {},
    onDeleteBlocked: () -> Unit = {},
    modifier: Modifier = Modifier.fillMaxSize()
) {
    // Brillo suave y constante que resalta los puntos donde SÍ se puede formar una barra nueva.
    val pulse by rememberInfiniteTransition(label = "canvasPulse").animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulseAlpha"
    )

    Canvas(
        modifier = modifier.pointerInput(design.nodes, design.members, deleteMode) {
            detectTapGestures { tapOffset ->
                val mapper = mapperFor(size.width.toFloat(), size.height.toFloat(), spanUnits)
                val nodesById = design.nodes.associateBy { it.id }
                val hitNode = design.nodes.minByOrNull { mapper.toOffset(it.point).distanceTo(tapOffset) }
                    ?.takeIf { mapper.toOffset(it.point).distanceTo(tapOffset) < 60f }

                if (deleteMode) {
                    if (hitNode != null) {
                        if (hitNode.isFixedByLevel) onDeleteBlocked() else onDeleteNode(hitNode.id)
                        return@detectTapGestures
                    }
                    val hitMember = design.members.mapNotNull { m ->
                        val a = nodesById[m.nodeAId]?.point
                        val b = nodesById[m.nodeBId]?.point
                        if (a == null || b == null) null
                        else m to distanceToSegment(tapOffset, mapper.toOffset(a), mapper.toOffset(b))
                    }.minByOrNull { it.second }?.takeIf { it.second < 26f }?.first
                    if (hitMember != null) onDeleteMember(hitMember.id) else onDeleteBlocked()
                } else {
                    if (hitNode != null) onTapNode(hitNode.id)
                    else onTapEmpty(mapper.toGridPoint(tapOffset))
                }
            }
        }
    ) {
        val mapper = mapperFor(size.width, size.height, spanUnits)

        // cuadrícula de referencia: un poco más marcada para que se lea bien sin distraer
        val gridColor = Blueprint300.copy(alpha = 0.35f)
        var gx = 0.0
        while (gx <= spanUnits) {
            drawLine(gridColor, mapper.toOffset(GridPoint(gx, -4.0)), mapper.toOffset(GridPoint(gx, 4.0)), strokeWidth = 1.3f)
            gx += 1.0
        }
        var gy = -4.0
        while (gy <= 4.0) {
            drawLine(gridColor, mapper.toOffset(GridPoint(0.0, gy)), mapper.toOffset(GridPoint(spanUnits, gy)), strokeWidth = 1.3f)
            gy += 1.0
        }

        // barras
        val nodesById = design.nodes.associateBy { it.id }
        for (member in design.members) {
            val a = nodesById[member.nodeAId]?.point ?: continue
            val b = nodesById[member.nodeBId]?.point ?: continue
            val fading = member.id == fadingMemberId
            val color = colorForRole(member.role).copy(alpha = if (fading) fadingAlpha else 1f)
            drawLine(
                color = color,
                start = mapper.toOffset(a), end = mapper.toOffset(b),
                strokeWidth = if (member.role == MemberRole.DECK) 15f else 8f,
                cap = StrokeCap.Round
            )
        }

        // nodos: cada tipo tiene su propia forma (no solo color), para no confundirlos entre
        // sí ni con las burbujas de color de los materiales en la barra de abajo
        for (node in design.nodes) {
            val offset = mapper.toOffset(node.point)
            val isPending = node.id == pendingNodeId
            val fading = node.id == fadingNodeId
            val nodeAlpha = if (fading) fadingAlpha else 1f
            val baseColor = when {
                node.anchorSide != AnchorSide.NONE -> Blueprint900
                node.isFixedByLevel -> SiteAmber
                node.isUserPier -> CanyonTerracotta
                else -> SiteOrange
            }.copy(alpha = nodeAlpha)
            val whiteHere = Color.White.copy(alpha = nodeAlpha)

            // resalta los 4 puntos de anclaje/apoyo del nivel: son la base de toda la estrategia
            if (node.isFixedByLevel || node.anchorSide != AnchorSide.NONE) {
                drawCircle(Blueprint300.copy(alpha = 0.30f * nodeAlpha), radius = 24f, center = offset)
            }

            // mientras hay un nodo pendiente, los puntos donde SÍ se puede formar una barra nueva
            // brillan con un pulso suave (no ya conectados entre sí, ni el mismo nodo)
            if (!fading && pendingNodeId != null && node.id != pendingNodeId) {
                val alreadyConnected = design.members.any {
                    (it.nodeAId == pendingNodeId && it.nodeBId == node.id) ||
                        (it.nodeAId == node.id && it.nodeBId == pendingNodeId)
                }
                if (!alreadyConnected) {
                    drawCircle(SuccessGreen.copy(alpha = 0.28f * pulse), radius = 26f, center = offset)
                    drawCircle(SuccessGreen.copy(alpha = 0.9f * pulse), radius = 18f, center = offset, style = Stroke(width = 2.5f))
                }
            }

            if (isPending) {
                drawCircle(SuccessGreen.copy(alpha = 0.35f * nodeAlpha), radius = 28f, center = offset)
            }
            drawCircle(baseColor, radius = 16f, center = offset)
            drawCircle(whiteHere, radius = 16f, center = offset, style = Stroke(width = 3f))
            when {
                // orilla fija: cuadrado blanco (como un poste clavado)
                node.anchorSide != AnchorSide.NONE -> drawRect(
                    color = whiteHere,
                    topLeft = Offset(offset.x - 5f, offset.y - 5f),
                    size = androidx.compose.ui.geometry.Size(10f, 10f)
                )
                // apoyo del nivel: triángulo blanco (como una roca de apoyo)
                node.isFixedByLevel -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(offset.x, offset.y - 6f)
                        lineTo(offset.x + 6f, offset.y + 5f)
                        lineTo(offset.x - 6f, offset.y + 5f)
                        close()
                    }
                    drawPath(path, whiteHere)
                }
                // apoyo pagado por el jugador: signo "$"
                node.isUserPier -> drawCircle(whiteHere, radius = 3f, center = offset)
                // nodo libre: cruz blanca (punto para conectar barras)
                else -> {
                    drawLine(whiteHere, Offset(offset.x - 5f, offset.y), Offset(offset.x + 5f, offset.y), strokeWidth = 3f)
                    drawLine(whiteHere, Offset(offset.x, offset.y - 5f), Offset(offset.x, offset.y + 5f), strokeWidth = 3f)
                }
            }
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
    val unitPx = (usableWidth / spanUnits.toFloat()).coerceAtLeast(26f)
    return GridToCanvasMapper(unitPx = unitPx, originXPx = margin, originYPx = heightPx * 0.6f)
}

private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x; val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

/** Distancia de un punto al segmento a-b, para detectar toques sobre una barra dibujada. */
private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
    val abx = b.x - a.x
    val aby = b.y - a.y
    val lenSq = abx * abx + aby * aby
    if (lenSq == 0f) return p.distanceTo(a)
    val t = (((p.x - a.x) * abx + (p.y - a.y) * aby) / lenSq).coerceIn(0f, 1f)
    val proj = Offset(a.x + t * abx, a.y + t * aby)
    return p.distanceTo(proj)
}
