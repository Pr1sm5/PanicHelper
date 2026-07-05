package com.example.panichelper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.panichelper.ui.screens.breathing.BreathingScreen
import com.example.panichelper.ui.screens.dailyexercise.DailyExerciseScreen
import com.example.panichelper.ui.screens.dailyexercise.DailyExerciseSessionScreen
import com.example.panichelper.ui.screens.distraction.DistractionScreen
import com.example.panichelper.ui.screens.help.HelpScreen
import com.example.panichelper.ui.screens.home.HomeScreen
import com.example.panichelper.ui.screens.settings.SettingsScreen

/**
 * Definiert alle Routen (Screens) der App an einer zentralen Stelle.
 * Vermeidet "magische Strings" im restlichen Code.
 */
object PanicHelperRoutes {
    const val HOME = "home"
    const val BREATHING = "breathing"
    const val DISTRACTION = "distraction"
    const val HELP = "help"
    const val DAILY_EXERCISE = "daily_exercise"
    const val DAILY_EXERCISE_SESSION = "daily_exercise_session/{minutes}/{techniqueId}"
    const val SETTINGS = "settings"

    fun dailyExerciseSession(minutes: Int, techniqueId: String) = "daily_exercise_session/$minutes/$techniqueId"
}

/**
 * Zentraler Navigationsgraph der App.
 * Verbindet HomeScreen mit den drei Hauptfunktionen.
 */
@Composable
fun PanicHelperNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = PanicHelperRoutes.HOME
    ) {
        composable(PanicHelperRoutes.HOME) {
            HomeScreen(
                onNavigateToBreathing = {
                    navController.navigate(PanicHelperRoutes.BREATHING)
                },
                onNavigateToDistraction = {
                    navController.navigate(PanicHelperRoutes.DISTRACTION)
                },
                onNavigateToHelp = {
                    navController.navigate(PanicHelperRoutes.HELP)
                },
                onNavigateToDailyExercise = {
                    navController.navigate(PanicHelperRoutes.DAILY_EXERCISE)
                },
                onNavigateToSettings = {
                    navController.navigate(PanicHelperRoutes.SETTINGS)
                }
            )
        }

        composable(PanicHelperRoutes.BREATHING) {
            BreathingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(PanicHelperRoutes.DISTRACTION) {
            DistractionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(PanicHelperRoutes.HELP) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(PanicHelperRoutes.DAILY_EXERCISE) {
            DailyExerciseScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartSession = { minutes, techniqueId ->
                    navController.navigate(PanicHelperRoutes.dailyExerciseSession(minutes, techniqueId))
                }
            )
        }

        composable(
            route = PanicHelperRoutes.DAILY_EXERCISE_SESSION,
            arguments = listOf(
                navArgument("minutes") { type = NavType.IntType },
                navArgument("techniqueId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val minutes = backStackEntry.arguments?.getInt("minutes") ?: 5
            val techniqueId = backStackEntry.arguments?.getString("techniqueId").orEmpty()
            DailyExerciseSessionScreen(
                minutes = minutes,
                techniqueId = techniqueId,
                onFinish = { navController.popBackStack() }
            )
        }

        composable(PanicHelperRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}