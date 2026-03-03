package com.elsharif.dailyseventy.presentation.home.view

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.presentation.onboarding.OnboardingPrefs
import com.elsharif.dailyseventy.presentation.onboarding.OnboardingScreen
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePage(
    context: Context,
    navController: NavController,
    viewModel: PrayerTimeViewModel
) {


    val ctx = LocalContext.current

    // ✅ بنتحقق من الـ SharedPreferences مرة واحدة عند الفتح
    var showOnboarding by remember {
        mutableStateOf(!OnboardingPrefs.isDone(ctx))
    }

    // ── لو أول مرة → الـ Onboarding ──────────────────────────────────────
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { showOnboarding = false }
        )
        return
    }


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
            TopAddressSection(viewModel)
            ZekrSection()
            Spacer(modifier = Modifier.height(2.dp))
            CategorySection(navController)
            PrayerTimesSection(context = context)
        }
    }
}
