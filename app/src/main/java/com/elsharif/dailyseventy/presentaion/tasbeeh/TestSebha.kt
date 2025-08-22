package com.elsharif.dailyseventy.presentaion.tasbeeh

import android.R.attr.centerX
import android.R.attr.centerY
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.getAdaptiveGradient

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun CustomizableSebhaPage(
    primaryColor: Color = MaterialTheme.colorScheme.primary, // لون برتقالي ذهبي
    modifier: Modifier = Modifier
) {

    val tasbeehImagesList = listOf(
        com.elsharif.dailyseventy.R.drawable.ic_subhan_allah,
        com.elsharif.dailyseventy.R.drawable.ic_alhamduillah,
        com.elsharif.dailyseventy.R.drawable.ic_la_ilah_ila_allah,
        R.drawable.ic_allah_akbar
    )




    val density = LocalDensity.current

    var count by remember { mutableStateOf(0) }
    val canvasW = 220.dp
    val canvasH = 320.dp
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val repetitionCount = 33
    val currentDisplayingIdx = (count / repetitionCount) % tasbeehImagesList.size

    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        AnimatedContent(
            targetState = currentDisplayingIdx,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
                .size(246.dp, 144.dp),
            label = "0",
        ) {
            Image(
                painter = painterResource(id = tasbeehImagesList[currentDisplayingIdx]), contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }


        Canvas(
            modifier = Modifier
                .size(canvasW, canvasH)
                .onSizeChanged { canvasSize = it }
                .pointerInput(count) {
                    detectTapGestures { pos ->
                        val centerX = canvasSize.width / 2f
                        val centerY = canvasSize.height / 2f

                        // الزر الرئيسي الكبير (العد)
                        val mainCenter = Offset(
                            centerX,
                            centerY + with(density) { 70.dp.toPx() }
                        )
                        val mainRadius = with(density) { 35.dp.toPx() }

                        // زر التصفير الصغير
                        val resetCenter = Offset(
                            centerX + with(density) { 45.dp.toPx() },
                            centerY + with(density) { 35.dp.toPx() }
                        )
                        val resetRadius = with(density) { 15.dp.toPx() }

                        when {
                            (pos - resetCenter).getDistance() <= resetRadius -> {
                                count = 0
                            }
                            (pos - mainCenter).getDistance() <= mainRadius -> {
                                count++
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // الشاشة الداخلية (شاشة العرض) مع تدرج لوني واقعي
            val screenGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0D1F0D), // أخضر غامق جداً
                    Color(0xFF123312), // أخضر LCD داكن
                    Color(0xFF0A140A)  // أسود مائل للأخضر
                ),
                start = Offset(centerX, centerY - 70.dp.toPx()),
                end = Offset(centerX, centerY - 35.dp.toPx())
            )
            // جسم المسبحة البيضاوي مع تدرج لوني
            val bodyGradient = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.9f),
                    primaryColor,
                    primaryColor.copy(alpha = 0.8f)
                ),
                center = Offset(centerX - 20.dp.toPx(), centerY - 10.dp.toPx()),
                radius = 120.dp.toPx()
            )

            drawOval(
                brush = bodyGradient,
                topLeft = Offset(centerX - 90.dp.toPx(), centerY - 40.dp.toPx()),
                size = Size(180.dp.toPx(), 220.dp.toPx())
            )

            // إطار جسم المسبحة
            drawOval(
                brush = gradient,
                topLeft = Offset(centerX - 90.dp.toPx(), centerY - 40.dp.toPx()),
                size = Size(180.dp.toPx(), 220.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())
            )

            // الشاشة الخارجية (إطار أسود)
            drawRoundRect(
                color = Color(0xFF1A1A1A),
                topLeft = Offset(centerX - 55.dp.toPx(), centerY - 80.dp.toPx()),
                size = Size(110.dp.toPx(), 55.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            // إطار الشاشة الداخلي
            drawRoundRect(
                brush = screenGradient,
                topLeft = Offset(centerX - 52.dp.toPx(), centerY - 77.dp.toPx()),
                size = Size(104.dp.toPx(), 49.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // الشاشة الداخلية (شاشة العرض)
            drawRoundRect(
                color = Color(0xFF0F2A0F),
                topLeft = Offset(centerX - 45.dp.toPx(), centerY - 70.dp.toPx()),
                size = Size(90.dp.toPx(), 35.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // الزر الرئيسي الكبير مع تأثير ثلاثي الأبعاد
            val buttonGradient = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFF5F5F5),
                    Color(0xFFE0E0E0),
                    Color(0xFFCCCCCC)
                ),
                center = Offset(centerX - 10.dp.toPx(), centerY + 60.dp.toPx()),
                radius = 35.dp.toPx()
            )

            drawCircle(
                brush = buttonGradient,
                center = Offset(centerX, centerY + 70.dp.toPx()),
                radius = 35.dp.toPx()
            )

            // إطار الزر الرئيسي
            drawCircle(
                color = Color(0xFF999999),
                center = Offset(centerX, centerY + 70.dp.toPx()),
                radius = 35.dp.toPx(),
                style = Stroke(width = 3.dp.toPx())
            )

            // الجزء الداخلي للزر الرئيسي
            drawCircle(
                color = Color(0xFFFFFFFF),
                center = Offset(centerX, centerY + 70.dp.toPx()),
                radius = 25.dp.toPx()
            )

            // ظل داخلي للزر
            drawCircle(
                color = Color(0xFFE8E8E8),
                center = Offset(centerX, centerY + 70.dp.toPx()),
                radius = 25.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            // زر التصفير الصغير
            val resetButtonGradient = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFF0F0F0),
                    Color(0xFFDDDDDD),
                    Color(0xFFBBBBBB)
                ),
                center = Offset(centerX + 40.dp.toPx(), centerY + 30.dp.toPx()),
                radius = 15.dp.toPx()
            )

            drawCircle(
                brush = resetButtonGradient,
                center = Offset(centerX + 45.dp.toPx(), centerY + 35.dp.toPx()),
                radius = 15.dp.toPx()
            )

            // إطار زر التصفير
            drawCircle(
                color = Color(0xFF888888),
                center = Offset(centerX + 45.dp.toPx(), centerY + 35.dp.toPx()),
                radius = 15.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )

            // الجزء الداخلي لزر التصفير
            drawCircle(
                color = Color(0xFFFFFFFF),
                center = Offset(centerX + 45.dp.toPx(), centerY + 35.dp.toPx()),
                radius = 10.dp.toPx()
            )
        }

        // النص داخل الشاشة
        Box(
            modifier = Modifier
                .offset(y = (-52).dp)
                .size(90.dp, 35.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 18.sp,
                    color = Color(0xFF00FF41), // أخضر فاتح مميز
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "COUNT",
                        fontSize = 6.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RESET",
                        fontSize = 6.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}