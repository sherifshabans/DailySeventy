package com.elsharif.dailyseventy.util.Navigation


import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.ui.theme.ThemeViewModel
import com.elsharif.dailyseventy.util.Screen
import com.elsharif.dailyseventy.util.workmanager.LocationManager


/*
* أضيف صورة وانيميشن للwidget
* */
data class BottomNavigationItem(
    val title:String,
    val selectedIcon:ImageVector,
    val unselectedIcon:ImageVector,
    val hasNews:Boolean,
    val badgeCount:Int?=null
)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UnifiedNavigationScaffold(context: Context) {


    val locationManager = LocationManager(context)


    val navController = rememberNavController()

    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    val mainScreens = listOf(
        Screen.HomeScreen.route,
        Screen.Hijri.route,
        Screen.Qible.route,
    )
    var currentRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route
        }
    }
    val shouldShowBottomBar = currentRoute in mainScreens

    val navBarColor = Color(0xFF294878) // Dark golden brown

    val list = listOf(
        BottomNavigationItem(
            title = "الأذكار",
            selectedIcon =Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false
        ),
        BottomNavigationItem(
            title = "مواقيت الصلاة",
            selectedIcon =Icons.Filled.Mosque, // AccessTime
            unselectedIcon = Icons.Outlined.Mosque,
            hasNews = false,
            badgeCount = 44
        ),
        BottomNavigationItem(
            title = "القبلة",
            selectedIcon =Icons.Filled.MyLocation,
            unselectedIcon = Icons.Outlined.MyLocation,
            hasNews = false,
            badgeCount = 44
        ),
        BottomNavigationItem(
            title = "التاريخ الهجري",
            selectedIcon =Icons.Filled.CalendarToday,
            unselectedIcon = Icons.Outlined.CalendarToday,
            hasNews = false,
            badgeCount = 12
        ),
        BottomNavigationItem(
            title = "الإعدادات",
            selectedIcon =Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            hasNews = true
        ),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
            .background(navBarColor),

        bottomBar = {
          if(shouldShowBottomBar)
            NavigationBar(
                containerColor = navBarColor

            ) {
                list.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            // if we need to navigate to another screen
                            navController.navigate(item.title)
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount != null) {
                                        Badge {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    } else if (item.hasNews) {
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector =
                                    if (index == selectedItemIndex) item.selectedIcon
                                    else item.unselectedIcon,
                                    contentDescription = item.title,
                                //    modifier = Modifier.background(Color(0xFFD8C4A0)),
                                    tint = Color(0xFFD8C4A0)
                                )
                            }
                        },
                        label = {
                            Text(text = item.title)
                        },
                        alwaysShowLabel = false
                    )

                }
            }
        }
    ){
        val themeViewModel :ThemeViewModel = hiltViewModel()
        val prayerTimeViewModel :PrayerTimeViewModel = hiltViewModel()

      AppNavHost(navController,context,themeViewModel,prayerTimeViewModel)

    }

}