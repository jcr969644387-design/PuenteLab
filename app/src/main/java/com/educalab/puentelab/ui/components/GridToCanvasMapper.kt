package com.educalab.puentelab.ui.components

import androidx.compose.ui.geometry.Offset
import com.educalab.puentelab.domain.model.GridPoint

/**
 * Convierte entre coordenadas de dominio (unidades de cuadrícula, y negativo = arriba) y
 * píxeles de pantalla (y positivo = abajo), aplicando una escala y desplazamiento fijos por
 * lienzo. Se mantiene como función pura para poder testearla sin Compose.
 */
class GridToCanvasMapper(
    private val unitPx: Float,
    private val originXPx: Float,
    private val originYPx: Float
) {
    fun toOffset(point: GridPoint): Offset =
        Offset(originXPx + point.x.toFloat() * unitPx, originYPx - point.y.toFloat() * unitPx)

    fun toGridPoint(offset: Offset, snap: Float = 0.5f): GridPoint {
        val gx = (offset.x - originXPx) / unitPx
        val gy = -(offset.y - originYPx) / unitPx
        val snappedX = kotlin.math.round(gx / snap) * snap
        val snappedY = kotlin.math.round(gy / snap) * snap
        return GridPoint(snappedX.toDouble(), snappedY.toDouble())
    }
}
