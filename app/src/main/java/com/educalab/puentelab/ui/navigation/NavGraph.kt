package com.educalab.puentelab.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.educalab.puentelab.ui.screens.academy.AcademyHomeScreen
import com.educalab.puentelab.ui.screens.builder.BuilderScreen
import com.educalab.puentelab.ui.screens.designs.DesignsScreen
import com.educalab.puentelab.ui.screens.materials.MaterialsScreen
import com.educalab.puentelab.ui.screens.onboarding.OnboardingScreen
import com.educalab.puentelab.ui.screens.profile.ProfileSetupScreen
import com.educalab.puentelab.ui.screens.progress.ProgressScreen
import com.educalab.puentelab.ui.screens.scenarios.ScenarioMissionsScreen
import com.educalab.puentelab.ui.screens.settings.SettingsScreen
import com.educalab.puentelab.ui.viewmodel.*
import com.educalab.puentelab.domain.model.ScenarioType

@Composable
fun PuenteLabNavGraph(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()

    val startDestination = Destinations.ACADEMY

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.ACADEMY) {
            val currentProfile = profile
            when {
                currentProfile == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                !currentProfile.onboardingCompleted -> {
                    OnboardingScreen(onFinished = { navController.navigate(Destinations.PROFILE_SETUP) })
                }
                else -> {
                    val vm: AcademyViewModel = viewModel(factory = viewModelFactory)
                    AcademyHomeScreen(
                        viewModel = vm,
                        onOpenScenario = { scenario -> navController.navigate(Destinations.scenarioDetail(scenario)) },
                        onOpenMaterials = { navController.navigate(Destinations.MATERIALS) },
                        onOpenDesigns = { navController.navigate(Destinations.DESIGNS) },
                        onOpenProgress = { navController.navigate(Destinations.PROGRESS) },
                        onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
                        onContinueChallenge = { id -> navController.navigate(Destinations.builder(id)) }
                    )
                }
            }
        }
        composable(Destinations.PROFILE_SETUP) {
            ProfileSetupScreen(onConfirm = { alias, avatar ->
                profileViewModel.completeOnboarding(alias, avatar)
                navController.navigate(Destinations.ACADEMY) {
                    popUpTo(Destinations.ACADEMY) { inclusive = true }
                }
            })
        }
        composable(
            Destinations.SCENARIO_DETAIL,
            arguments = listOf(navArgument("scenario") { type = NavType.StringType })
        ) { backStackEntry ->
            val scenarioArg = backStackEntry.arguments?.getString("scenario") ?: return@composable
            val scenario = ScenarioType.valueOf(scenarioArg)
            val vm: ScenariosViewModel = viewModel(factory = viewModelFactory)
            ScenarioMissionsScreen(
                scenario = scenario,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenChallenge = { id -> navController.navigate(Destinations.builder(id)) }
            )
        }
        composable(Destinations.MATERIALS) {
            val vm: MaterialsViewModel = viewModel(factory = viewModelFactory)
            MaterialsScreen(viewModel = vm)
        }
        composable(
            Destinations.BUILDER,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            val vm: BuilderViewModel = viewModel(factory = viewModelFactory)
            BuilderScreen(
                challengeId = challengeId,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onNextMission = { nextId ->
                    navController.navigate(Destinations.builder(nextId)) {
                        popUpTo(Destinations.builder(challengeId)) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.DESIGNS) {
            val vm: DesignsViewModel = viewModel(factory = viewModelFactory)
            DesignsScreen(viewModel = vm)
        }
        composable(Destinations.PROGRESS) {
            val vm: ProgressViewModel = viewModel(factory = viewModelFactory)
            ProgressScreen(viewModel = vm)
        }
        composable(Destinations.SETTINGS) {
            SettingsScreen(viewModel = profileViewModel, onBack = { navController.popBackStack() })
        }
    }
}
