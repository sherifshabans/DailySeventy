package com.elsharif.dailyseventy.presentaion.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elsharif.dailyseventy.presentaion.zekr.ZekkrScreen

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(navController, startDestination = "category_screen") {
        composable("category_screen") {
            CategoryScreen(navController)
        }
        composable("zekkr_screen/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            //ZekkrScreen(navController, category)
        }
    }
}
