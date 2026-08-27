package com.educalab.puentelab.ui.navigation

object Destinations {
    const val ONBOARDING = "onboarding"
    const val PROFILE_SETUP = "profile_setup"
    const val ACADEMY = "academy"
    const val SCENARIOS = "scenarios"
    const val MATERIALS = "materials"
    const val BUILDER = "builder/{challengeId}"
    const val DESIGNS = "designs"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"

    fun builder(challengeId: String) = "builder/$challengeId"
}
