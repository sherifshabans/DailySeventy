package com.elsharif.dailyseventy.util.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elsharif.dailyseventy.presentaion.components.HijriCalendar
import com.elsharif.dailyseventy.presentaion.home.CategoryScreen
import com.elsharif.dailyseventy.presentaion.zekr.ZekkrScreen
import com.elsharif.dailyseventy.util.Screen
import java.time.chrono.HijrahDate

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(navController, startDestination = "الرئيسية") {
        composable(Screen.Home.route) {
            CategoryScreen(navController)
        }
        composable("zekkr_screen/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ZekkrScreen(navController, category)
        }
        composable(Screen.Morning.route) {
            CategoryScreen(navController)
        }
        composable(Screen.Hijri.route) {
            var hijrahDate by remember { mutableStateOf(HijrahDate.now()) }

            HijriCalendar(hijrahDate) {
                hijrahDate = it
            }
        }

    }
}
