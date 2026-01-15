package com.permissionmonitor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.permissionmonitor.ui.screen.AppDetailScreen
import com.permissionmonitor.ui.screen.AppListScreen

sealed class Screen(val route: String) {
    data object AppList : Screen("app_list")
    data object AppDetail : Screen("app_detail/{packageName}") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.AppList.route
    ) {
        composable(Screen.AppList.route) {
            AppListScreen(
                onAppClick = { packageName ->
                    navController.navigate(Screen.AppDetail.createRoute(packageName))
                }
            )
        }
        
        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            AppDetailScreen(
                packageName = packageName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
