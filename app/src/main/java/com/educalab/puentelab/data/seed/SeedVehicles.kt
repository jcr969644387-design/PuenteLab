package com.educalab.puentelab.data.seed

import com.educalab.puentelab.data.local.entity.VehicleEntity
import com.educalab.puentelab.domain.model.ScenarioType

object SeedVehicles {
    val all: List<VehicleEntity> = listOf(
        VehicleEntity("van_explorer", "Carreta de Exploración", "Ligera y ágil, perfecta para las primeras pruebas.", "vehicle_cart", ScenarioType.RIVER, 1.0, 1),
        VehicleEntity("buggy", "Buggy Todoterreno", "Ruedas grandes para cruzar cañones irregulares.", "vehicle_buggy", ScenarioType.CANYON, 1.1, 1),
        VehicleEntity("forest_truck", "Camión Forestal", "Transporta troncos: pesa más de lo que parece.", "vehicle_truck", ScenarioType.FOREST, 1.3, 1),
        VehicleEntity("tram", "Tranvía Urbano", "Lleva pasajeros por la ciudad elevada, exige calzadas suaves.", "vehicle_tram", ScenarioType.CITY, 1.4, 1),
        VehicleEntity("expedition_truck", "Camión de Expedición", "Equipo pesado de montaña para el paso más alto.", "vehicle_expedition", ScenarioType.MOUNTAIN, 1.5, 1),
        VehicleEntity("pivot_hauler", "Transporte de PIVOT", "El vehículo especial del estudio. Solo para ingenieros de nivel alto.", "vehicle_pivot", ScenarioType.RIVER, 1.8, 5)
    )
}
