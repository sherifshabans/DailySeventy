package com.elsharif.dailyseventy.presentation.widgets.khatma

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.ui.theme.Black
import com.elsharif.dailyseventy.ui.theme.White
import com.elsharif.dailyseventy.util.cornerRadiusCompat


@SuppressLint("RestrictedApi")
@GlanceComposable
@Composable
fun QuranKhatmaWidgetStateless(
    soraName: String,
    quranBody: String,
    onNextClickedClass: Class<out ActionCallback>,
    onPreviousClickedClass: Class<out ActionCallback>,
) {

    val cornerRadius = 2
    val backgroundAlpha = 0.01f


    Box(
        modifier = GlanceModifier.fillMaxSize()
            .cornerRadiusCompat(cornerRadius, color = White, backgroundAlpha = 1f),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.rightcorner),
                contentDescription = null
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(R.drawable.leftcorner),
                contentDescription = null
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        Column(
            modifier = GlanceModifier.padding( 16.dp).fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.padding(  16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.ic_arrow_back),
                    contentDescription = "Previous",
                    onClick = actionRunCallback(onPreviousClickedClass),
                    modifier = GlanceModifier.size(28.dp).padding(4.dp),
                    backgroundColor = ColorProvider(Black),
                    contentColor = ColorProvider(White)
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    soraName,
                    modifier = GlanceModifier.padding(horizontal = 10.dp),
                    style = TextStyle(
                        color = ColorProvider(Black),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )

                )
                Spacer(GlanceModifier.defaultWeight())

                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.ic_arrow_forward),
                    contentDescription = "Previous",
                    onClick = actionRunCallback(onNextClickedClass),
                    modifier = GlanceModifier.size(28.dp).padding(4.dp),
                    backgroundColor = ColorProvider(Black),
                    contentColor = ColorProvider(White)

                )

            }

            Text(
                quranBody,
                modifier = GlanceModifier.padding(horizontal = 10.dp).fillMaxSize(),
                style = TextStyle(
                    color = ColorProvider(Black),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )

        }
    }

}
