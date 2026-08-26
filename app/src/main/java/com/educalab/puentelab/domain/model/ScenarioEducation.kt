package com.educalab.puentelab.domain.model

/**
 * Contenido pedagógico por escenario: nivel de dificultad general, qué se busca que el chico
 * aprenda, y una combinación de materiales "recomendada" por rol a modo de pista (no obligatoria:
 * el motor de simulación evalúa físicamente cualquier combinación que el jugador elija).
 */
data class ScenarioEducationInfo(
    val difficultyLabel: String,
    val educationalGoal: String,
    val recommendedMaterialByRole: Map<MemberRole, String>
)

object ScenarioEducation {
    val byScenario: Map<ScenarioType, ScenarioEducationInfo> = mapOf(
        ScenarioType.FOREST to ScenarioEducationInfo(
            difficultyLabel = "Fácil",
            educationalGoal = "Aprender las partes básicas de un puente.",
            recommendedMaterialByRole = mapOf(
                MemberRole.DECK to "wood",
                MemberRole.BRACE to "wood",
                MemberRole.CABLE to "rope",
                MemberRole.TOWER to "wood"
            )
        ),
        ScenarioType.RIVER to ScenarioEducationInfo(
            difficultyLabel = "Fácil/Media",
            educationalGoal = "Entender que el puente debe resistir el peso y el entorno.",
            recommendedMaterialByRole = mapOf(
                MemberRole.DECK to "concrete",
                MemberRole.BRACE to "steel",
                MemberRole.CABLE to "steel_cable",
                MemberRole.TOWER to "concrete"
            )
        ),
        ScenarioType.CANYON to ScenarioEducationInfo(
            difficultyLabel = "Media",
            educationalGoal = "Aprender a cubrir una gran distancia con tu puente.",
            recommendedMaterialByRole = mapOf(
                MemberRole.DECK to "steel",
                MemberRole.BRACE to "steel",
                MemberRole.CABLE to "steel_cable",
                MemberRole.TOWER to "steel"
            )
        ),
        ScenarioType.MOUNTAIN to ScenarioEducationInfo(
            difficultyLabel = "Difícil",
            educationalGoal = "Entender por qué la estabilidad importa en terrenos difíciles y con viento.",
            recommendedMaterialByRole = mapOf(
                MemberRole.DECK to "steel",
                MemberRole.BRACE to "steel",
                MemberRole.CABLE to "steel_cable",
                MemberRole.TOWER to "stone"
            )
        ),
        ScenarioType.CITY to ScenarioEducationInfo(
            difficultyLabel = "Experto",
            educationalGoal = "Buscar el equilibrio entre materiales ligeros, resistentes y costosos.",
            recommendedMaterialByRole = mapOf(
                MemberRole.DECK to "concrete",
                MemberRole.BRACE to "aluminum",
                MemberRole.CABLE to "carbon_fiber",
                MemberRole.TOWER to "steel"
            )
        )
    )
}
