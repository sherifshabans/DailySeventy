package com.elsharif.dailyseventy.presentation.tasbeeh

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen

@Composable
fun ImageSebhaPage(viewModel: TasbeehViewModel,navController: NavController)
{
    val tasbeehImagesList = listOf(
        com.elsharif.dailyseventy.R.drawable.ic_subhan_allah,
        com.elsharif.dailyseventy.R.drawable.ic_alhamduillah,
        com.elsharif.dailyseventy.R.drawable.ic_allah_akbar,
        com.elsharif.dailyseventy.R.drawable.ic_la_ilah_ila_allah
    )
    val count by viewModel.getTasbeehCount().collectAsState(initial = 0)

    val repetitionCount = 33
    val currentIdx = (count / repetitionCount) % tasbeehImagesList.size
    Scaffold(
        topBar = { DashboardScreenTopBar(Screen.TasbeehImages.titleRes, navController) }
    ) { paddingValues ->

    CustomizableSebhaPage(
        count = count,
        zikrImage = tasbeehImagesList[currentIdx],
        onIncrease = { viewModel.increaseTasbeeh() },
        onReset = { viewModel.resetTasbeeh() },
        paddingValues = paddingValues
    )
    }
}
