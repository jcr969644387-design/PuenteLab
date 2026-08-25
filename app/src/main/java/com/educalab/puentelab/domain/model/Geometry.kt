package com.educalab.puentelab.domain.model

import kotlin.math.sqrt

/**
 * Punto en la cuadrícula de construcción.
 * x crece hacia la orilla derecha. y=0 es el nivel de calzada; y negativo = hacia arriba (torres),
 * y positivo = hacia abajo (apoyos/pilares hundidos en el valle).
 */
data class GridPoint(val x: Double, val y: Double) {
    fun distanceTo(other: GridPoint): Double {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    /** Pendiente absoluta entre este punto y otro (para comprobar si un vehículo puede circular). */
    fun slopeTo(other: GridPoint): Double {
        val dx = other.x - x
        if (dx == 0.0) return Double.POSITIVE_INFINITY
        return kotlin.math.abs((other.y - y) / dx)
    }
}
