package com.elsharif.dailyseventy.presentation.tasbeeh

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.util.getAdaptiveGradient

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun CustomizableSebhaPage(
    count: Int,
    onIncrease: () -> Unit,
    onReset: () -> Unit,
    zikrText: String? = null,   // لو عايز تعرض نص
    zikrImage: Int? = null,     // لو عايز تعرض صورة
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues
) {


    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)

    Column( // 👈 بدل Box
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 📌 عرض الذكر (إما نص أو صورة)
        AnimatedContent(
            targetState = zikrText ?: zikrImage,
            modifier = Modifier
                // .align(Alignment.TopCenter)
                .padding(paddingValues)
                .fillMaxWidth()
                .wrapContentHeight(), // 👈 بدل الحجم الثابت
            //.size(246.dp, 144.dp),
            label = "zikr"
        ) { target ->
            when (target) {
                is String -> Text(
                    text = target,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                is Int -> Image(
                    painter = painterResource(id = target),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(160.dp) // الصورة حجم ثابت معقول
                    )
            }
        }
        //Spacer(modifier = Modifier.height(40.dp))

        // 🎨 رسم السبحة (نفس Canvas بتاعك) مع زر العد والتصفير
        Box(contentAlignment = Alignment.Center) {

            Canvas(
                modifier = Modifier
                    .size(220.dp, 320.dp)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(count) {
                        detectTapGestures { pos ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val mainCenter = Offset(centerX, centerY + 70.dp.toPx())
                            val mainRadius = 35.dp.toPx()
                            val resetCenter = Offset(centerX + 45.dp.toPx(), centerY + 35.dp.toPx())
                            val resetRadius = 15.dp.toPx()

                            when {
                                (pos - resetCenter).getDistance() <= resetRadius -> onReset()
                                (pos - mainCenter).getDistance() <= mainRadius -> onIncrease()
                            }
                        }
                    }
            ) {

                val centerX = canvasSize.width / 2f
                val centerY = canvasSize.height / 2f


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


                // جسم السبحة
                val bodyGradient = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.9f),
                        primary,
                        primary.copy(alpha = 0.8f)
                    ),
                    center = Offset(centerX - 20.dp.toPx(), centerY - 10.dp.toPx()),
                    radius = 120.dp.toPx()
                )

                drawOval(
                    brush = bodyGradient,
                    topLeft = Offset(centerX - 90.dp.toPx(), centerY - 40.dp.toPx()),
                    size = Size(180.dp.toPx(), 220.dp.toPx())
                )
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


            // 🟢 شاشة العداد
            Box(
                modifier = Modifier.offset(y = (-52).dp).size(90.dp, 35.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = count.toString(), fontSize = 18.sp, color = Color(0xFF00FF41))
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("COUNT", fontSize = 6.sp, color = Color.Gray)
                        Text("RESET", fontSize = 6.sp, color = Color.Gray)
                    }
                }
            }
        }

    }
}
