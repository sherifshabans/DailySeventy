package com.elsharif.dailyseventy.presentaion.tasbeeh


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R

@Composable
fun TasbeehScreen() {
    var count by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // خلفية عامة (ممكن تشيلها لو عندك خلفية)
        contentAlignment = Alignment.Center
    ) {
        // خلفية المسبحة (الصورة)
        Image(
            painter = painterResource(id = R.drawable.sebhah), // ضع صورتك هنا
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        // زر العد (الكبير)
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-80).dp) // مكان الزر الكبير
                .clip(CircleShape)
                .background(Color(0xFFEF6C00)) // اللون (ممكن تغيره حسب اختيار المستخدم)
                .clickable { count++ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        // زر Reset
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomCenter)
                .offset(x = (-100).dp, y = (-150).dp) // مكان زر reset
                .clip(CircleShape)
                .background(Color.DarkGray)
                .clickable { count = 0 },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↺", // أيقونة إعادة
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
