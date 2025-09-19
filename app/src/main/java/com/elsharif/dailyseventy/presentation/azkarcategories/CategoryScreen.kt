package com.elsharif.dailyseventy.presentation.azkarcategories

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.elsharif.dailyseventy.ui.theme.ubuntuFontFamily
import com.elsharif.dailyseventy.presentation.components.CurvedScrollView
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CategoryScreen(navController: NavController) {


    Scaffold(
        topBar = { DashboardScreenTopBar(Screen.Azkar.titleRes,navController) }
    ) { //paddingValues ->

        CurvedScroll(navController)


    }
}


@Composable
fun CurvedScroll(navController: NavController) {

    val categories = listOf("أذكار الصباح", "أذكار المساء", "أذكار بعد السلام من الصلاة المفروضة",
        "تسابيح" ,"أذكار النوم" ,"أذكار الاستيقاظ" ,"أدعية قرآنية" ,"أدعية الأنبياء","الرُّقية الشرعية من القرآن الكريم","التسبيح، التحميد، التهليل، التكبير","أذكار متنوعة")


    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme .colorScheme.background)
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
                            text = stringResource(getCategoryResId(categories[category])), // العرض حسب اللغة
                            modifier = Modifier.fillMaxWidth(), // Make the text span full width
                            textAlign = TextAlign.Center,       // Center-align the text
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = ubuntuFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
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
