package com.permissionmonitor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.permissionmonitor.ui.screen.AboutScreen
import com.permissionmonitor.ui.screen.AppDetailScreen
import com.permissionmonitor.ui.screen.AppListScreen

object Routes {
    const val APP_LIST = "app_list"
    const val APP_DETAIL = "app_detail/{packageName}"
    const val ABOUT = "about"
    
    fun appDetail(packageName: String) = "app_detail/$packageName"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.APP_LIST
    ) {
        composable(Routes.APP_LIST) {
            AppListScreen(
                onAppClick = { packageName ->
                    navController.navigate(Routes.appDetail(packageName))
                },
                onAboutClick = {
                    navController.navigate(Routes.ABOUT)
                }
            )
        }
        
        composable(
            route = Routes.APP_DETAIL,
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
        
        composable(Routes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
