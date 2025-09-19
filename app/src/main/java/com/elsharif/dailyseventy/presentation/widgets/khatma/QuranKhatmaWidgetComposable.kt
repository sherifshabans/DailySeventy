package com.elsharif.dailyseventy.presentation.widgets.khatma

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.ui.theme.DailySeventyTheme


@GlanceComposable
@Composable
fun QuranKhatmaWidgetStateless(
    soraName: String,
    quranBody: String,
    onNextClickedClass: Class<out ActionCallback>,
    onPreviousClickedClass: Class<out ActionCallback>,
) {
    Box(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.leftcorner),
                contentDescription = null
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(R.drawable.rightcorner),
                contentDescription = null
            )
        }

        Column(
            modifier = GlanceModifier.padding(top = 16.dp).fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.padding(horizontal = 16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.ic_arrow_back),
                    contentDescription = "Previous",
                    onClick = actionRunCallback(onPreviousClickedClass),
                    modifier = GlanceModifier.size(28.dp),
                    backgroundColor = GlanceTheme.colors.primary,
                    contentColor = GlanceTheme.colors.onPrimary
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    soraName,
                    modifier = GlanceModifier.padding(horizontal = 10.dp),
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )

                )
                Spacer(GlanceModifier.defaultWeight())

                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.ic_arrow_forward),
                    contentDescription = "Previous",
                    onClick = actionRunCallback(onNextClickedClass),
                    modifier = GlanceModifier.size(28.dp),
                    backgroundColor = GlanceTheme.colors.primary,
                    contentColor = GlanceTheme.colors.onPrimary
                )

            }
            Text(
                quranBody,
                modifier = GlanceModifier.padding(horizontal = 10.dp).fillMaxSize(),
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )

        }
    }

}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun QuranKhatmaWidgetPreview() {

    DailySeventyTheme {
        QuranKhatmaWidgetStateless(
            soraName = "Al-Fatiha",
            quranBody = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            onNextClickedClass = DummyAction::class.java,
            onPreviousClickedClass = DummyAction::class.java
        )
    }

}


private object DummyAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    }

}