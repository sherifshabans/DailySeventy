package com.elsharif.dailyseventy.presentation.widgets.khatma

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import com.elsharif.dailyseventy.domain.UseCaseProvider
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.ui.theme.DailySeventyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG = "QuranKhatmaWidgetReceiv"

class QuranKhatmaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QuranKhatmaWidget
}

object QuranKhatmaWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val isDarkSystem = AppPreferences(context).isDarkModeEnabled()
            val currentQuranPage by AppPreferences(context).currentQuranPage.collectAsState(1)

            val soraName by UseCaseProvider.getSoraByPageNumberUseCase(currentQuranPage)
                .map { it.arabicName }
                .collectAsState("")

            val quranBody by UseCaseProvider.getQuranPageAyaWithTafseerUseCase(currentQuranPage)
                .map { it.map { it.aya }.joinToString("") { aya -> aya.ayaText } }
                .collectAsState("")

            DailySeventyTheme(isDarkSystem) {
                QuranKhatmaWidgetStateless(
                    soraName = soraName,
                    quranBody = quranBody,
                    onNextClickedClass = NextQuranPageAction::class.java,
                    onPreviousClickedClass = PreviousQuranPageAction::class.java,
                )
            }
        }

    }

}

class NextQuranPageAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AppPreferences(context).nextQuranPage()
        val currentQuranPage = AppPreferences(context).currentQuranPage.first()
        Log.d(TAG, "onAction: $currentQuranPage")
        QuranKhatmaWidget.update(context, glanceId)
    }
}

class PreviousQuranPageAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AppPreferences(context).previousQuranPage()
        val currentQuranPage = AppPreferences(context).currentQuranPage.first()
        Log.d(TAG, "onAction: $currentQuranPage")
        QuranKhatmaWidget.update(context, glanceId)
    }
}