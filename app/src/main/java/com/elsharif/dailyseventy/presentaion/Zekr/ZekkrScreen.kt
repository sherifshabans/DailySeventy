package com.elsharif.dailyseventy.presentaion.zekr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentaion.components.CountCard
import com.elsharif.dailyseventy.presentaion.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.getAdaptiveGradient

@Composable
fun ZekkrScreen(
    navController: NavController,
    category:String
) {

    /*
    * عايز أضيف إمكانية الشير للنص
    * عايز أغير لون التيكست التاني
    * عايز أغير باك جراوند الصفجة الرئيسية
    *
    * */


    val viewModel: ZekkrViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val count by viewModel.count.collectAsStateWithLifecycle()




    LaunchedEffect(category) {
        viewModel.onEvent(ZekkrEvent.SelectCategory(category)) // ✅ Fetch azkaar for category
    }
    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)


    Scaffold(
        //snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {DashboardScreenTopBar(
            name = category,
            navController =navController
        )}
    ) {paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(gradient)
        ) {
            items(state.azkaar.size) { index ->
                val zekr = state.azkaar[index]
                CountCard(
                    zekkr = zekr,
                )
            }


        }
    }

}

