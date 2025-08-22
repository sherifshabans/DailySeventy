package com.elsharif.dailyseventy.presentaion.home.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentaion.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.ui.theme.BlueEnd
import com.elsharif.dailyseventy.ui.theme.BlueStart

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage(navController: NavController) {
    Scaffold(
        topBar =
            {
                DashboardScreenTopBar(
                    navController = navController
                )
            },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            TopAddressSection()
            ZekrSection()
            Spacer(modifier = Modifier.height(2.dp))
            CategorySection(navController)
            PrayerTimesSection()
        }
    }
}
