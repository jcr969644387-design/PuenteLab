package com.educalab.puentelab.ui.navigation

import com.educalab.puentelab.domain.model.ScenarioType

object Destinations {
    const val ONBOARDING = "onboarding"
    const val PROFILE_SETUP = "profile_setup"
    const val ACADEMY = "academy"
    const val SCENARIO_DETAIL = "scenario_detail/{scenario}"
    const val MATERIALS = "materials"
    const val BUILDER = "builder/{challengeId}"
    const val DESIGNS = "designs"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"

    fun builder(challengeId: String) = "builder/$challengeId"
    fun scenarioDetail(scenario: ScenarioType) = "scenario_detail/${scenario.name}"
}
