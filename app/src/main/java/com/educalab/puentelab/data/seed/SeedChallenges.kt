package com.educalab.puentelab.data.seed

import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.domain.model.DemandLevel
import com.educalab.puentelab.domain.model.GridPoint
import com.educalab.puentelab.domain.model.ScenarioType
import com.educalab.puentelab.domain.model.StructureType
import kotlin.math.roundToInt

/**
 * Generador de los 45 desafíos de PuenteLab (9 por escenario x 5 escenarios).
 *
 * La curva de dificultad NO es aleatoria: es una fórmula fija por nivel (span, presupuesto,
 * pendiente máxima y elevación de los apoyos fijos) anclada a un caso verificado manualmente
 * con BridgeEngine fuera de Gradle (ver docs/BUILD_REPORT.md): el nivel más difícil (span 13.6,
 * demanda ALTA, sin apoyos adicionales de pago) se comprobó resoluble con un diseño de arco +
 * cercha que combina acero y fibra de carbono, con margen de presupuesto >= 220. La fórmula usa
 * un multiplicador de seguridad (8.2 en vez de 8.0) para no quedar por debajo de ese mínimo
 * verificado en ningún escenario.
 *
 * Todos los escenarios comparten el mismo modelo mecánico (span/apoyos/presupuesto/demanda/
 * pendiente); solo cambian narrativa, multiplicador de presupuesto temático e identidad visual.
 * Esta es una simplificación deliberada y documentada (no oculta): ver MANUAL_TECNICO.md.
 */
object SeedChallenges {

    private data class ScenarioFlavor(
        val scenario: ScenarioType,
        val idPrefix: String,
        val budgetMultiplier: Double, // siempre >= 1.0 respecto al caso verificado
        val introPool: List<String>,
        val successPool: List<String>
    )

    private val flavors = listOf(
        ScenarioFlavor(
            ScenarioType.RIVER, "river", 1.00,
            listOf(
                "El río creció esta semana y la balsa de siempre ya no alcanza. Diseña un cruce firme.",
                "La corriente es fuerte en este tramo. PIVOT sugiere reforzar bien los apoyos.",
                "Un grupo de excursionistas espera al otro lado. Necesitan un puente confiable, no uno bonito."
            ),
            listOf(
                "¡El vehículo cruzó el río sin mojarse las ruedas! El estudio anota tu diseño en el archivo.",
                "Cruce exitoso. El agua sigue corriendo abajo, tranquila, mientras tu puente aguanta arriba.",
                "PIVOT registra otro cruce limpio. El río ya no separa nada."
            )
        ),
        ScenarioFlavor(
            ScenarioType.CANYON, "canyon", 1.08,
            listOf(
                "El Cañón Rojo es profundo y seco: no hay agua abajo, solo roca y viento.",
                "Los apoyos naturales de piedra están listos. Tu trabajo es unirlos con algo que no ceda.",
                "El eco del cañón repite cada golpe de martillo. Aquí un fallo se nota rápido."
            ),
            listOf(
                "El vehículo asomó al borde, cruzó y siguió su camino por el otro lado del cañón.",
                "El cañón sigue tan profundo como siempre, pero ahora tiene un puente confiable encima.",
                "Otro cruce logrado sobre el vacío rojo. PIVOT silba, impresionado."
            )
        ),
        ScenarioFlavor(
            ScenarioType.FOREST, "forest", 1.02,
            listOf(
                "Entre los árboles altos del Bosque Profundo hay un barranco cubierto de niebla.",
                "La madera local es barata aquí, pero el terreno es irregular. Piensa bien los apoyos.",
                "Un sendero de guardabosques necesita un cruce seguro para las próximas lluvias."
            ),
            listOf(
                "El vehículo desapareció entre los árboles del otro lado, dejando el puente firme detrás.",
                "El bosque guarda un secreto nuevo: un cruce que no se mueve ni con la lluvia.",
                "Cruce exitoso bajo las copas de los árboles. La niebla no pudo con tu diseño."
            )
        ),
        ScenarioFlavor(
            ScenarioType.CITY, "city", 1.12,
            listOf(
                "La Ciudad Elevada necesita un cruce entre dos azoteas industriales.",
                "El tránsito no puede detenerse. El nuevo cruce debe ser preciso y elegante.",
                "Los ingenieros de la ciudad ya probaron dos diseños y fallaron. Es tu turno."
            ),
            listOf(
                "El vehículo cruzó entre los edificios sin un solo tropiezo. La ciudad respira aliviada.",
                "Otro cruce urbano resuelto. Abajo, la ciudad sigue su ritmo sin enterarse del reto que fue.",
                "PIVOT actualiza el mapa de la ciudad con tu nuevo puente."
            )
        ),
        ScenarioFlavor(
            ScenarioType.MOUNTAIN, "mountain", 1.10,
            listOf(
                "El Paso de Montaña es angosto y el viento sopla fuerte entre las rocas.",
                "A esta altura el frío es constante. El estudio necesita un cruce que no falle.",
                "Los exploradores de montaña llevan equipo pesado. El puente debe estar a la altura."
            ),
            listOf(
                "El vehículo coronó el paso de montaña y siguió cuesta arriba, sin sobresaltos.",
                "El viento sigue soplando, pero el puente ni se inmuta. Cruce logrado.",
                "Otro paso de montaña conquistado por la ingeniería del estudio."
            )
        )
    )

    fun buildAll(): List<BridgeChallengeEntity> =
        flavors.flatMap { flavor -> (1..9).map { i -> buildLevel(flavor, i) } }

    private fun buildLevel(flavor: ScenarioFlavor, i: Int): BridgeChallengeEntity {
        val span = 4.0 + (i - 1) * 1.2
        val demand = when {
            i <= 3 -> DemandLevel.LOW
            i <= 6 -> DemandLevel.MEDIUM
            else -> DemandLevel.HIGH
        }
        val maxSlope = 0.8 - (i - 1) * 0.03125
        val archY = if (i <= 2) 0.0 else -(0.6 + (i - 2) * 0.18)
        val marginFactor = 4.0 - (i - 1) * 0.25
        val rawBudget = span * 8.2 * marginFactor * flavor.budgetMultiplier
        val budget = (rawBudget / 5.0).roundToInt() * 5.0 // redondeo a múltiplos de 5

        val recommended = when {
            i <= 2 -> StructureType.BEAM
            i <= 5 -> StructureType.TRUSS
            i <= 7 -> StructureType.ARCH
            else -> StructureType.SUSPENSION
        }

        val intro = flavor.introPool[(i - 1) % flavor.introPool.size]
        val success = flavor.successPool[(i - 1) % flavor.successPool.size]

        return BridgeChallengeEntity(
            id = "${flavor.idPrefix}_%02d".format(i),
            scenario = flavor.scenario,
            orderIndex = i,
            name = "${flavor.scenario.displayName} · Nivel $i",
            spanUnits = span,
            leftBankX = 0.0,
            leftBankY = 0.0,
            rightBankX = span,
            rightBankY = 0.0,
            fixedSupports = listOf(
                GridPoint(span / 3.0, archY),
                GridPoint(2.0 * span / 3.0, archY)
            ),
            budget = budget,
            demand = demand,
            maxSlope = maxSlope,
            budgetMarginFor2Stars = 0.10,
            budgetMarginFor3Stars = 0.25,
            maxStressFor3Stars = 0.75,
            recommendedStructure = recommended,
            narrativeIntro = intro,
            narrativeSuccess = success
        )
    }
}
