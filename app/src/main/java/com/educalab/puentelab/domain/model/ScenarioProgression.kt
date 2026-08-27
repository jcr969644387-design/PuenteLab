package com.educalab.puentelab.domain.model

/**
 * Orden fijo en el que se desbloquean los escenarios (distinto del orden interno del enum
 * ScenarioType). Un escenario se desbloquea al completar el último nivel del anterior.
 */
object ScenarioProgression {
    val ORDER = listOf(
        ScenarioType.FOREST,
        ScenarioType.RIVER,
        ScenarioType.CANYON,
        ScenarioType.MOUNTAIN,
        ScenarioType.CITY
    )

    fun next(scenario: ScenarioType): ScenarioType? {
        val i = ORDER.indexOf(scenario)
        return if (i in 0 until ORDER.lastIndex) ORDER[i + 1] else null
    }

    fun isFirst(scenario: ScenarioType): Boolean = ORDER.firstOrNull() == scenario
}
