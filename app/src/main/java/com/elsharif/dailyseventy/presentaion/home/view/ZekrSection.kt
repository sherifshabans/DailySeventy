package com.elsharif.dailyseventy.presentaion.home.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.util.getAdaptiveGradient


// كدا فاضل هنا إني أخلي الذكر راندوم






internal val remembrances = listOf(
    "المُحبّ ينبغي أن لا يتركَ وردَ الصَّلاة والسّلام على سيدنا رسولِ الله صلى الله عليه وسلّم، فإنّ المحبّ لا يغفل عن حبيبه...",
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




fun getGradient(
    startColor:Color,
    endColor:Color
):Brush {
    return Brush.horizontalGradient(
        colors = listOf(startColor,endColor)
    )
}



@Composable
fun ZekrSection() {

    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)

    // Pick a random zekr every time the composable is recomposed
    val randomZekr = remember { remembrances.random() }

    // Display just the one random zekr
    ZekrItem(randomZekr, gradient)

}

@Composable
fun ZekrItem(
    zekr: String,
    color: Brush
) {
    Box(
        modifier = Modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(color)
            .fillMaxWidth()
            .height(140.dp)
            .clickable {},
        contentAlignment = androidx.compose.ui.Alignment.Center
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
