package com.educalab.puentelab.domain.model

/**
 * Restricción especial de un desafío puntual: un límite de piezas o un material vetado, con un
 * texto corto para mostrarle al jugador por qué existe. No es aleatoria: cada una está pensada
 * para un desafío de dificultad media (nivel 5) de cada escenario, con márgenes generosos para
 * no volver imposible un nivel ya balanceado.
 */
data class MissionConstraint(
    val maxMembers: Int? = null,
    val maxCables: Int? = null,
    val maxBraces: Int? = null,
    val maxTowers: Int? = null,
    val bannedMaterialIds: Set<String> = emptySet(),
    val label: String
)

object MissionConstraints {
    val byChallengeId: Map<String, MissionConstraint> = mapOf(
        "forest_05" to MissionConstraint(
            maxMembers = 10,
            label = "🎯 Constrúyelo con 10 barras o menos: aquí se premia la sencillez."
        ),
        "river_05" to MissionConstraint(
            maxTowers = 1,
            label = "🎯 Solo puedes usar 1 Torre: elige bien dónde ponerla."
        ),
        "canyon_05" to MissionConstraint(
            bannedMaterialIds = setOf("stone"),
            label = "🎯 Sin Piedra Tallada: es muy pesada para cruzar un cañón tan ancho."
        ),
        "mountain_05" to MissionConstraint(
            maxBraces = 3,
            label = "🎯 Máximo 3 Riostras: el viento de montaña no perdona el exceso de peso."
        ),
        "city_05" to MissionConstraint(
            maxCables = 3,
            label = "🎯 Máximo 3 Cables: la ciudad premia un diseño eficiente."
        )
    )
}

/** Cuántos vehículos cruzan en la prueba de un desafío: los niveles finales piden un convoy. */
object MissionVehicles {
    fun countFor(orderIndex: Int): Int = when {
        orderIndex >= 9 -> 3
        orderIndex >= 7 -> 2
        else -> 1
    }
}
