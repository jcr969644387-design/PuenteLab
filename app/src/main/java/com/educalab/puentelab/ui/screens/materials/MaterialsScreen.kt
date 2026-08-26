package com.educalab.puentelab.ui.screens.materials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.data.local.entity.MaterialEntity
import com.educalab.puentelab.data.local.entity.VehicleEntity
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.MaterialsViewModel

@Composable
fun MaterialsScreen(viewModel: MaterialsViewModel) {
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Materiales", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
                Text("Cada material tiene un balance distinto de resistencia, costo y peso.", style = MaterialTheme.typography.bodyMedium, color = Ink600)
                Spacer(Modifier.height(8.dp))
            }
            items(materials) { MaterialCard(it) }
            item {
                Spacer(Modifier.height(16.dp))
                Text("Vehículos de prueba", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
                Spacer(Modifier.height(8.dp))
            }
            items(vehicles) { vehicle ->
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        VehicleIcon(vehicle, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(vehicle.name, style = MaterialTheme.typography.titleMedium)
                            Text(vehicle.description, style = MaterialTheme.typography.bodyMedium, color = Ink600)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialCard(material: MaterialEntity) {
    val color = runCatching { Color(android.graphics.Color.parseColor(material.colorHex)) }.getOrDefault(Blueprint500)
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(material.name, style = MaterialTheme.typography.titleMedium)
                Text(material.description, style = MaterialTheme.typography.bodyMedium, color = Ink600)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatPill("Resist.", material.strength.toInt().toString(), SuccessGreen)
                    StatPill("Costo", material.costPerUnit.toInt().toString(), SiteOrange)
                    StatPill("Peso", material.weightFactor.toString(), MountainSlate)
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("$label $value", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** Silueta simple de vehículo (carrocería + cabina + ruedas), coloreada según su escenario. */
@Composable
private fun VehicleIcon(vehicle: VehicleEntity, modifier: Modifier = Modifier) {
    val accent = colorForScenario(vehicle.themeScenario)
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(6.dp)) {
            val w = size.width; val h = size.height
            val bodyTop = h * 0.32f
            val bodyBottom = h * 0.62f
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.08f, bodyTop),
                size = Size(w * 0.84f, bodyBottom - bodyTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f, w * 0.12f)
            )
            drawRoundRect(
                color = accent,
                topLeft = Offset(w * 0.32f, h * 0.14f),
                size = Size(w * 0.4f, bodyTop - h * 0.14f + 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f, w * 0.08f)
            )
            drawCircle(Blueprint900, radius = w * 0.11f, center = Offset(w * 0.28f, bodyBottom))
            drawCircle(Blueprint900, radius = w * 0.11f, center = Offset(w * 0.72f, bodyBottom))
        }
    }
}

private fun colorForScenario(scenario: ScenarioType): Color = when (scenario) {
    ScenarioType.RIVER -> RiverTeal
    ScenarioType.CANYON -> CanyonTerracotta
    ScenarioType.FOREST -> ForestGreen
    ScenarioType.CITY -> CityViolet
    ScenarioType.MOUNTAIN -> MountainSlate
}
