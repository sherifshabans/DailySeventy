package com.elsharif.dailyseventy.util.Navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.core.presentationSensor.HomeScreen
import com.elsharif.dailyseventy.presentaion.Qibla.QiblaScreen
import com.elsharif.dailyseventy.presentaion.hijriCalendar.HijriCalendar
import com.elsharif.dailyseventy.presentaion.azkarcategories.CategoryScreen
import com.elsharif.dailyseventy.presentaion.colorselection.ColorPicker
import com.elsharif.dailyseventy.presentaion.home.view.HomePage
import com.elsharif.dailyseventy.presentaion.prayertimes.PrayerTimesPage
import com.elsharif.dailyseventy.presentaion.settings.SettingsScreen
import com.elsharif.dailyseventy.presentaion.tasbeeh.CustomizableSebhaPage
import com.elsharif.dailyseventy.presentaion.tasbeeh.TasbeehPage
import com.elsharif.dailyseventy.presentaion.tasbeeh.TasbeehScreen
import com.elsharif.dailyseventy.presentaion.zekr.ZekkrScreen
import com.elsharif.dailyseventy.ui.theme.ThemeViewModel
import com.elsharif.dailyseventy.util.Screen
import java.time.chrono.HijrahDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController,context: Context,themeViewModel: ThemeViewModel) {

    NavHost(navController, startDestination = Screen.HomeScreen.route) {
        composable(Screen.HomeScreen.route) {
            HomePage(navController)
        }
        composable("zekkr_screen/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            ZekkrScreen(navController, category)
        }
        composable(Screen.PrayerTimes.route) {
            PrayerTimesPage()

        }
        composable(Screen.Azkar.route) {
            CategoryScreen(navController)

        }
        composable(Screen.Hijri.route) {
            var hijrahDate by remember { mutableStateOf(HijrahDate.now()) }

            HijriCalendar(  selectedDate = hijrahDate, navController = navController) {
                hijrahDate = it
            }
        }
        composable(Screen.Qible.route) {
          //
            QiblaScreen(navController)

        }
        composable(Screen.Settings.route) {

            SettingsScreen(navController)

        }
        composable(Screen.Tasbeeh.route) {

            //TasbeehPage(navController)

            CustomizableSebhaPage()
            //TasbeehScreen()
        }
        composable(Screen.ColorPicker.route) {
            ColorPicker(navController = navController) { selectedColor ->
                themeViewModel.updateColor(selectedColor)
                navController.popBackStack() // go back after picking
            }

        }


    }
}
