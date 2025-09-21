package com.elsharif.dailyseventy.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TitleContainer(title: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(198.dp)
            .height(39.dp)
            .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(size = 5.dp))

    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight(700),
            color = White,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun TitlePreview() {
    TitleContainer(title = "دُعَاءُ خَتْمِ القُرْآنِ الكَريمِ")
}