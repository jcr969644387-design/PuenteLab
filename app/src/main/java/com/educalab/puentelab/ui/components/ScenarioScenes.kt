package com.educalab.puentelab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.ui.theme.*

@Composable
fun ScenarioScene(scenario: ScenarioType, modifier: Modifier = Modifier.fillMaxSize()) {
    Canvas(modifier = modifier) {
        when (scenario) {
            ScenarioType.RIVER -> drawRiverScene()
            ScenarioType.CANYON -> drawCanyonScene()
            ScenarioType.FOREST -> drawForestScene()
            ScenarioType.CITY -> drawCityScene()
            ScenarioType.MOUNTAIN -> drawMountainScene()
        }
    }
}

private fun DrawScope.drawRiverScene() {
    val w = size.width; val h = size.height
    drawRect(Blueprint100, size = size)
    // colinas lejanas
    val hillPath = Path().apply {
        moveTo(0f, h * 0.55f)
        quadraticBezierTo(w * 0.25f, h * 0.4f, w * 0.5f, h * 0.55f)
        quadraticBezierTo(w * 0.75f, h * 0.7f, w, h * 0.5f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(hillPath, ForestGreen.copy(alpha = 0.5f))
    // río
    val riverPath = Path().apply {
        moveTo(0f, h * 0.72f)
        quadraticBezierTo(w * 0.3f, h * 0.62f, w * 0.55f, h * 0.75f)
        quadraticBezierTo(w * 0.8f, h * 0.85f, w, h * 0.7f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(riverPath, RiverTeal)
    // reflejos
    repeat(3) { i ->
        drawLine(
            White.copy(alpha = 0.4f),
            Offset(w * (0.2f + i * 0.22f), h * 0.8f),
            Offset(w * (0.3f + i * 0.22f), h * 0.8f),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawCanyonScene() {
    val w = size.width; val h = size.height
    drawRect(SiteAmber.copy(alpha = 0.25f), size = size)
    // paredes de cañón (trapecios rojizos)
    val leftWall = Path().apply {
        moveTo(0f, h * 0.3f); lineTo(w * 0.38f, h * 0.55f); lineTo(w * 0.3f, h); lineTo(0f, h); close()
    }
    val rightWall = Path().apply {
        moveTo(w, h * 0.25f); lineTo(w * 0.6f, h * 0.5f); lineTo(w * 0.68f, h); lineTo(w, h); close()
    }
    drawPath(leftWall, CanyonTerracotta)
    drawPath(rightWall, CanyonTerracotta.copy(alpha = 0.85f))
    // vetas de roca
    repeat(4) { i ->
        drawLine(Blueprint900.copy(alpha = 0.15f), Offset(w * 0.05f, h * (0.4f + i * 0.1f)), Offset(w * 0.32f, h * (0.5f + i * 0.09f)), strokeWidth = 2f)
    }
}

private fun DrawScope.drawForestScene() {
    val w = size.width; val h = size.height
    drawRect(Blueprint100.copy(alpha = 0.6f), size = size)
    drawRect(ForestGreen.copy(alpha = 0.15f), topLeft = Offset(0f, h * 0.55f), size = androidx.compose.ui.geometry.Size(w, h * 0.45f))
    fun tree(cx: Float, baseY: Float, scale: Float) {
        drawRect(CanyonTerracotta, topLeft = Offset(cx - 3f * scale, baseY - 10f * scale), size = androidx.compose.ui.geometry.Size(6f * scale, 14f * scale))
        val crown = Path().apply {
            moveTo(cx, baseY - 55f * scale)
            lineTo(cx - 22f * scale, baseY - 15f * scale)
            lineTo(cx + 22f * scale, baseY - 15f * scale)
            close()
        }
        drawPath(crown, ForestGreen)
    }
    tree(w * 0.12f, h * 0.85f, 1.4f)
    tree(w * 0.28f, h * 0.9f, 1.1f)
    tree(w * 0.75f, h * 0.88f, 1.3f)
    tree(w * 0.9f, h * 0.82f, 1.6f)
    // barranco central
    val gapPath = Path().apply {
        moveTo(w * 0.35f, h); lineTo(w * 0.42f, h * 0.6f); lineTo(w * 0.58f, h * 0.6f); lineTo(w * 0.65f, h); close()
    }
    drawPath(gapPath, MountainSlate.copy(alpha = 0.35f))
}

private fun DrawScope.drawCityScene() {
    val w = size.width; val h = size.height
    drawRect(CityViolet.copy(alpha = 0.12f), size = size)
    fun building(x: Float, bw: Float, bh: Float, color: androidx.compose.ui.graphics.Color) {
        drawRect(color, topLeft = Offset(x, h - bh), size = androidx.compose.ui.geometry.Size(bw, bh))
        var wy = h - bh + 10f
        while (wy < h - 14f) {
            var wx = x + 8f
            while (wx < x + bw - 10f) {
                drawRect(SiteAmber.copy(alpha = 0.7f), topLeft = Offset(wx, wy), size = androidx.compose.ui.geometry.Size(6f, 8f))
                wx += 16f
            }
            wy += 16f
        }
    }
    building(w * 0.02f, w * 0.22f, h * 0.55f, Blueprint700)
    building(w * 0.26f, w * 0.14f, h * 0.4f, CityViolet)
    building(w * 0.76f, w * 0.22f, h * 0.6f, Blueprint700)
    building(w * 0.6f, w * 0.14f, h * 0.42f, CityViolet)
}

private fun DrawScope.drawMountainScene() {
    val w = size.width; val h = size.height
    drawRect(Blueprint100, size = size)
    fun peak(cx: Float, baseY: Float, height: Float, width: Float, color: androidx.compose.ui.graphics.Color) {
        val p = Path().apply {
            moveTo(cx, baseY - height)
            lineTo(cx - width / 2f, baseY)
            lineTo(cx + width / 2f, baseY)
            close()
        }
        drawPath(p, color)
        // nieve
        val snow = Path().apply {
            moveTo(cx, baseY - height)
            lineTo(cx - width * 0.18f, baseY - height * 0.72f)
            lineTo(cx + width * 0.18f, baseY - height * 0.72f)
            close()
        }
        drawPath(snow, White.copy(alpha = 0.85f))
    }
    peak(w * 0.25f, h * 0.85f, h * 0.6f, w * 0.5f, MountainSlate)
    peak(w * 0.62f, h * 0.88f, h * 0.75f, w * 0.62f, MountainSlate.copy(alpha = 0.9f))
    peak(w * 0.9f, h * 0.86f, h * 0.5f, w * 0.4f, MountainSlate.copy(alpha = 0.7f))
}
