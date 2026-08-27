package com.educalab.puentelab.domain.logic

import com.educalab.puentelab.data.local.entity.BridgeChallengeEntity
import com.educalab.puentelab.data.local.entity.ProgressEntity
import com.educalab.puentelab.domain.model.ModuleState
import com.educalab.puentelab.domain.model.ScenarioProgression
import com.educalab.puentelab.domain.model.ScenarioType

/**
 * Calcula el estado (bloqueado/disponible/...) de cada desafío combinando dos reglas:
 * dentro de un escenario, cada nivel se desbloquea al completar el anterior; y un escenario
 * completo solo se desbloquea al terminar el último nivel del escenario previo en
 * ScenarioProgression.ORDER. Es lógica pura (sin Android/Room) para poder reutilizarla desde
 * cualquier ViewModel sin duplicarla.
 */
object ChallengeProgress {
    data class Item(val challenge: BridgeChallengeEntity, val state: ModuleState, val bestStars: Int)

    fun compute(challenges: List<BridgeChallengeEntity>, progress: List<ProgressEntity>): List<Item> {
        val progressByChallenge = progress.associateBy { it.challengeId }
        val byScenario = challenges.groupBy { it.scenario }

        fun isDone(state: ModuleState?) = state == ModuleState.COMPLETED || state == ModuleState.MASTERED

        fun scenarioCompleted(scenario: ScenarioType): Boolean {
            val last = byScenario[scenario].orEmpty().maxByOrNull { it.orderIndex } ?: return false
            return isDone(progressByChallenge[last.id]?.state)
        }

        fun scenarioUnlocked(scenario: ScenarioType): Boolean {
            if (ScenarioProgression.isFirst(scenario)) return true
            val idx = ScenarioProgression.ORDER.indexOf(scenario)
            return idx > 0 && scenarioCompleted(ScenarioProgression.ORDER[idx - 1])
        }

        return buildList {
            for ((scenario, group) in byScenario) {
                val list = group.sortedBy { it.orderIndex }
                val unlocked = scenarioUnlocked(scenario)
                list.forEachIndexed { index, challenge ->
                    val p = progressByChallenge[challenge.id]
                    val previousCompletedWithinScenario = index == 0 || isDone(progressByChallenge[list[index - 1].id]?.state)
                    val state = when {
                        p != null -> p.state
                        !unlocked -> ModuleState.LOCKED
                        previousCompletedWithinScenario -> ModuleState.AVAILABLE
                        else -> ModuleState.LOCKED
                    }
                    add(Item(challenge, state, p?.bestStars ?: 0))
                }
            }
        }
    }
}
