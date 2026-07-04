package com.example.panichelper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.panichelper.ui.screens.breathing.BreathingScreen
import com.example.panichelper.ui.screens.distraction.DistractionScreen
import com.example.panichelper.ui.screens.help.HelpScreen
import com.example.panichelper.ui.screens.home.HomeScreen

/**
 * Definiert alle Routen (Screens) der App an einer zentralen Stelle.
 * Vermeidet "magische Strings" im restlichen Code.
 */
object PanicHelperRoutes {
    const val HOME = "home"
    const val BREATHING = "breathing"
    const val DISTRACTION = "distraction"
    const val HELP = "help"
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
    }
}