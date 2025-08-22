package com.elsharif.dailyseventy.presentaion.colorselection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentaion.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.ui.theme.UserColors
import com.elsharif.dailyseventy.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(
    navController: NavController,
    onColorSelected: (Color) -> Unit
) {

    Scaffold(
        topBar = {
                DashboardScreenTopBar(Screen.ColorPicker.route,navController)


        }
    ) { padding ->

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserColors.forEach { colorSet ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorSet.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp) // ✅ slightly bigger circles
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(color) }
                        )
                    }
                }
            }
        }
    }
    }
}
