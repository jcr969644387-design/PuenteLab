package com.educalab.puentelab.data.seed

import com.educalab.puentelab.data.local.entity.BadgeEntity
import com.educalab.puentelab.data.local.entity.BuilderStampEntity
import com.educalab.puentelab.domain.logic.BadgeEngine
import com.educalab.puentelab.domain.model.BadgeId
import com.educalab.puentelab.domain.model.ScenarioType

object SeedBadges {
    val all: List<BadgeEntity> = BadgeEngine.catalog.map { spec ->
        BadgeEntity(
            id = spec.id,
            name = spec.name,
            description = spec.description,
            iconKey = "badge_${spec.id.name.lowercase()}"
        )
    }
}

/**
 * Colección local de "Sellos de Constructor": una placa ilustrada por cada escenario que se
 * desbloquea al completar el primer y el último desafío de ese escenario, más una sello especial
 * por cada insignia de dominio de estructura. Total: 5*2 + 2 = 12 sellos coleccionables.
 */
object SeedStamps {
    val all: List<BuilderStampEntity> = buildList {
        val scenarioNames = mapOf(
            ScenarioType.RIVER to "Río",
            ScenarioType.CANYON to "Cañón",
            ScenarioType.FOREST to "Bosque",
            ScenarioType.CITY to "Ciudad",
            ScenarioType.MOUNTAIN to "Montaña"
        )
        for ((scenario, label) in scenarioNames) {
            val prefix = scenario.name.lowercase()
            add(
                BuilderStampEntity(
                    id = "stamp_${prefix}_bronce",
                    name = "Sello de $label (Bronce)",
                    description = "Se otorga al completar tu primer desafío en $label.",
                    scenario = scenario,
                    iconKey = "stamp_${prefix}_bronze",
                    unlockChallengeId = "${prefix}_01"
                )
            )
            add(
                BuilderStampEntity(
                    id = "stamp_${prefix}_oro",
                    name = "Sello de $label (Oro)",
                    description = "Se otorga al completar el desafío más difícil de $label.",
                    scenario = scenario,
                    iconKey = "stamp_${prefix}_gold",
                    unlockChallengeId = "${prefix}_09"
                )
            )
        }
        add(
            BuilderStampEntity(
                id = "stamp_maestro_estructuras",
                name = "Sello de Maestría Estructural",
                description = "Se otorga junto a la insignia Maestro/a del Arco.",
                scenario = ScenarioType.RIVER,
                iconKey = "stamp_master_arch",
                unlockBadgeId = BadgeId.MAESTRO_ARCO
            )
        )
        add(
            BuilderStampEntity(
                id = "stamp_veterano",
                name = "Sello del Estudio",
                description = "Se otorga junto a la insignia Veterano/a del Estudio.",
                scenario = ScenarioType.RIVER,
                iconKey = "stamp_veteran",
                unlockBadgeId = BadgeId.VETERANO
            )
        )
    }
}
