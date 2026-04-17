package com.elsharif.dailyseventy.util.Navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elsharif.dailyseventy.MainActivity
import com.elsharif.dailyseventy.presentation.azkarcategories.CategoryScreen
import com.elsharif.dailyseventy.presentation.comingsoon.ComingSoonPage
import com.elsharif.dailyseventy.presentation.garden.DhikrGardenScreen
import com.elsharif.dailyseventy.presentation.hijriCalendar.HijriCalendar
import com.elsharif.dailyseventy.presentation.home.view.HomePage
import com.elsharif.dailyseventy.presentation.mohamedlovers.MohamedLoversScreen
import com.elsharif.dailyseventy.presentation.home.view.SplashScreen
import com.elsharif.dailyseventy.presentation.prayertimes.MonthlyPrayerTimesPage
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimesPage
import com.elsharif.dailyseventy.presentation.privacypolicy.PrivacyPolicy
import com.elsharif.dailyseventy.presentation.problems.FormWebViewScreen
import com.elsharif.dailyseventy.presentation.qibla.QiblaPage
import com.elsharif.dailyseventy.presentation.qibla.QiblaPageWithSplash
import com.elsharif.dailyseventy.presentation.sensor.StepAlarmScreen
import com.elsharif.dailyseventy.presentation.sensor.StepAlarmViewModel
import com.elsharif.dailyseventy.presentation.settings.SettingsScreen
import com.elsharif.dailyseventy.presentation.tasbeeh.CustomZikrSebhaPage
import com.elsharif.dailyseventy.presentation.tasbeeh.ImageSebhaPage
import com.elsharif.dailyseventy.presentation.tasbeeh.TasbeehLandingPage
import com.elsharif.dailyseventy.presentation.tasbeeh.TasbeehViewModel
import com.elsharif.dailyseventy.presentation.tasbeeh.ZikrListSebhaPage
import com.elsharif.dailyseventy.presentation.travel.TravelScreen
import com.elsharif.dailyseventy.presentation.tree.TreeScreen
import com.elsharif.dailyseventy.presentation.zekr.ZekkrArcScreen
import com.elsharif.dailyseventy.ui.theme.ThemeViewModel
import com.elsharif.dailyseventy.util.Screen
import java.time.chrono.HijrahDate


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController,context: Context,themeViewModel: ThemeViewModel,prayerTimeViewModel: PrayerTimeViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    // التحقق من Intent للمنبه
    val startCategoryFromIntent = (context as? MainActivity)?.intent?.getStringExtra("category") ?: ""
    val stepAlarmCategory = (context as? MainActivity)?.intent?.getStringExtra("step_alarm") ?: ""
    val navigateTo = (context as? MainActivity)?.intent?.getStringExtra("navigate_to")


    val startDestination = when {
        stepAlarmCategory == "step_alarm"  -> Screen.AalarmRoute.route
        startCategoryFromIntent.isNotEmpty() -> "zekkr_screen/$startCategoryFromIntent"
        navigateTo == "tree"               -> Screen.TreeScreenRoute.route
        navigateTo == "garden"             -> Screen.GardenScreenRoute.route
        navigateTo == "travel"               -> Screen.TravelScreenRoute.route
        else                               -> Screen.HomeScreen.route
    }

    if (showSplash) {
        SplashScreen(
            onSplashFinished = { showSplash = false }
        )
    } else {
    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(Screen.HomeScreen.route) {
            HomePage(context,navController,prayerTimeViewModel)
        }
        composable("zekkr_screen/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            //ZekkrScreen(navController, category)
            ZekkrArcScreen( navController,category)
        }
        composable(Screen.PrayerTimes.route) {
            PrayerTimesPage(navController,prayerTimeViewModel)

        }
        composable(Screen.MonthlyPrayerTimes.route) {
            MonthlyPrayerTimesPage(navController)

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

           QiblaPage(navController)
        }
        composable(Screen.Settings.route) {
            val viewModel: StepAlarmViewModel = hiltViewModel()

            SettingsScreen(navController,themeViewModel, context, prayerTimeViewModel,viewModel)

        }
        composable(Screen.ComingSoon.route) {

            ComingSoonPage(navController)

        }
        composable(Screen.Tasbeeh.route) {

            TasbeehLandingPage(navController)

        }
        composable(Screen.AalarmRoute.route) {
            val viewModel: StepAlarmViewModel = hiltViewModel()

            StepAlarmScreen(viewModel,navController)
        }
        composable(Screen.TasbeehImages.route) {
            val viewModel: TasbeehViewModel = hiltViewModel()

            ImageSebhaPage(viewModel,navController)
        }
        composable(Screen.TasbeehList.route) {
            val viewModel: TasbeehViewModel = hiltViewModel()

            ZikrListSebhaPage(viewModel,navController)

        }
        composable(Screen.TasbeehCustom.route) {
            val viewModel: TasbeehViewModel = hiltViewModel()

            CustomZikrSebhaPage(viewModel,navController)
        }
        composable(Screen.AnimatedQibla.route) {

            QiblaPageWithSplash(navController)
        }
        composable(Screen.FeedbackScreen.route) {

            FormWebViewScreen(navController)
        }
        composable(Screen.PrivacyPolicyScreen.route) {

            PrivacyPolicy(navController)
        }

        composable(Screen.TreeScreenRoute.route) {
            TreeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TravelScreenRoute.route) {
            TravelScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.GardenScreenRoute.route) {
            DhikrGardenScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.MohamedLoversRoute.route) {
            MohamedLoversScreen(
                onBackClick = { navController.navigateUp() }
            )
        }


    }
    }
}
