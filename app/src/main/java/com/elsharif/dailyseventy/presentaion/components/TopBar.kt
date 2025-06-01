package com.elsharif.dailyseventy.presentaion.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenTopBar () {

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "سبعون مرة",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White // Optional: change text color
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF294878) // Example: dark golden brown
        )
    )

}