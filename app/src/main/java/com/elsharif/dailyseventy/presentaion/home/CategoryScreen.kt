package com.elsharif.dailyseventy.presentaion.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CategoryScreen(navController: NavController) {

    val categories = listOf("أذكار الصباح", "أذكار المساء", "أذكار بعد السلام من الصلاة المفروضة",
        "تسابيح" ,"أذكار النوم" ,"أذكار الاستيقاظ" ,"أدعية قرآنية" ,"أدعية الأنبياء")

    Scaffold(
        topBar = { DashboardScreenTopBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            categories.forEach { category ->
                Button(
                    onClick = {
                        navController.navigate("zekkr_screen/$category") // ✅ Navigate with category
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(text = category, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenTopBar () {

    CenterAlignedTopAppBar(
        title = {
            Text(text = "Mind Crafted", style = MaterialTheme.typography.headlineMedium)
        }
    )

}
