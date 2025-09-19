package com.elsharif.dailyseventy.presentation.tasbeeh

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.components.CircularScrollView
import com.elsharif.dailyseventy.presentation.components.CurvedScrollView
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.ui.theme.ubuntuFontFamily
import com.elsharif.dailyseventy.util.Screen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TasbeehLandingPage(navController: NavController) {
    Scaffold(
        topBar = { DashboardScreenTopBar(Screen.Tasbeeh.titleRes, navController) }
    ) { paddingValues ->

        CurvedSebhaScroll(navController,paddingValues)
    }
}

@Composable
fun CurvedSebhaScroll(navController: NavController,paddingValues: PaddingValues) {

    val sebhaTypes = listOf(
        Screen.TasbeehImages.titleRes to Screen.TasbeehImages.route,
        Screen.TasbeehList.titleRes to Screen.TasbeehList.route,
        Screen.TasbeehCustom.titleRes to Screen.TasbeehCustom.route
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.type_of_sebhha),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularScrollView(
                itemCount = sebhaTypes.size,
                fullCircle = false // 👈 نص دايرة (لو عايز كاملة خليه true)
            ) { index ->
                val (titleRes, route) = sebhaTypes[index]

                Column(modifier = Modifier.wrapContentSize()) {
                    Button(
                        onClick = { navController.navigate(route) },
                        modifier = Modifier
                            .width(120.dp)
                            .size(100.dp)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = stringResource(id = titleRes),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = ubuntuFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // الصورة اللي في النص
            Image(
                painter = painterResource(id = R.drawable.doaa),
                contentDescription = "Sebha Logo Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

    }
}
