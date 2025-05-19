package com.elsharif.dailyseventy.presentaion.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.ui.theme.ubuntuFontFamily
import com.elsharif.dailyseventy.presentaion.components.CurvedScrollView

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CategoryScreen(navController: NavController) {

    val categories = listOf("أذكار الصباح", "أذكار المساء", "أذكار بعد السلام من الصلاة المفروضة",
        "تسابيح" ,"أذكار النوم" ,"أذكار الاستيقاظ" ,"أدعية قرآنية" ,"أدعية الأنبياء")

    Scaffold(
        topBar = { DashboardScreenTopBar() }
    ) { //paddingValues ->

        CurvedScroll(navController)

        /*Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            .background(MaterialTheme.colorScheme.surface)
        ) {

            val listState = rememberLazyGridState()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = listState,
                contentPadding = PaddingValues(top = SmallRadius.dp),
            ) {
                header {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(
                                    vertical = 16.dp,
                                    horizontal = 22.dp
                                ),
                            textAlign = TextAlign.Center,
                            text = "اختر نوع الذكر ",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily =ubuntuFontFamily ,
                            fontSize = 20.sp
                        )
                    }

                }
                items(categories.size){ category ->
                    Button(
                        onClick = {
                            navController.navigate("zekkr_screen/${categories[category]}") // ✅ Navigate with category
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(100.dp)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = categories[category],
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = ubuntuFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                            )
                    }
                }


            }

        }*/


/*
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
        }*/

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenTopBar () {

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
@Composable
fun CurvedScroll(navController: NavController) {

    val categories = listOf("أذكار الصباح", "أذكار المساء", "أذكار بعد السلام من الصلاة المفروضة",
        "تسابيح" ,"أذكار النوم" ,"أذكار الاستيقاظ" ,"أدعية قرآنية" ,"أدعية الأنبياء")


    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFFAAC7FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Curved Scrollview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            CurvedScrollView(categories.size) { category ->
                Column(
                    modifier = Modifier.wrapContentSize()
                ) {

                    Button(
                        onClick = {
                            navController.navigate("zekkr_screen/${categories[category]}") // ✅ Navigate with category
                        },
                        modifier = Modifier
                            .width(120.dp)
                            .size(100.dp)
                            .padding(6.dp)
                    ) {

                        Text(
                            text = categories[category],
                            modifier = Modifier.fillMaxWidth(), // Make the text span full width
                            textAlign = TextAlign.Center,       // Center-align the text
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = ubuntuFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                    }


                }
            }

            Image(
                painter = painterResource(id = R.drawable.doaa),
                contentDescription = "Curved Logo Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
    }
}
