package com.elsharif.dailyseventy.presentation.home.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.presentation.components.AutoScrollCarouselList
import com.elsharif.dailyseventy.presentation.components.Movement
import com.elsharif.dailyseventy.util.getAdaptiveGradient

// قائمة الأذكار
internal val remembrances = listOf(
    "المُحبّ ينبغي أن لا يتركَ وردَ الصَّلاة والسّلام على سيدنا رسولِ الله صلى الله عليه وسلّم...",
    "سبحان الله",
    "الحمد لله",
    "لا إله إلا الله",
    "الله أكبر",
    "أستغفر الله",
    "اللهم صل وسلم على نبينا محمد",
    "لا حول ولا قوة إلا بالله",
    "سبحان الله وبحمده، سبحان الله العظيم",
    "اللهم اغفر لي",
    "اللهم اجعلني من التوابين"
)

// لإنشاء gradient للكارد
fun getGradient(startColor: Color, endColor: Color): Brush {
    return Brush.horizontalGradient(
        colors = listOf(startColor, endColor)
    )
}

// الكاردس المتحركة
@Composable
fun ZekrSection() {
    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)

    AutoScrollCarouselList(
        items = remembrances,
        movement = Movement.Horizontal.Left, // حركة أفقية لليسار
        itemSpacing = 12.dp,
        scrollSpeedPxPerMillis = 0.07f, // سرعة الحركة
        itemContent = { index, zekr ->
            ZekrCard(zekr, gradient)
        }
    )
}

// الكارد نفسه
@Composable
fun ZekrCard(zekr: String, color: Brush) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(color)
            .padding(top= 3.dp)
        ,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = zekr,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
    }
}
