package com.elsharif.dailyseventy.presentaion.tasbeeh

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentaion.common.DecoratedPage



@Composable
fun TasbeehPage(navController: NavController) {
    val viewModel: TasbeehViewModel = hiltViewModel()
    val tasbeehCounter by viewModel.getTasbeehCount().collectAsState(initial = 0)

    TasbeehPageViews(
        tasbeehCounter = tasbeehCounter,
        onTasbeehClick = { viewModel.increaseTasbeeh() },
        onResetClick = { viewModel.resetTasbeeh() }
    )
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TasbeehPageViews(tasbeehCounter: Int, onTasbeehClick: (Int) -> Unit, onResetClick: () -> Unit) {
    DecoratedPage {
        val tasbeehImagesList = listOf(
            R.drawable.ic_subhan_allah,
            R.drawable.ic_alhamduillah,
            R.drawable.ic_la_ilah_ila_allah,
            R.drawable.ic_allah_akbar
        )
        val repetitionCount = 33
        val currentDisplayingIdx = (tasbeehCounter / repetitionCount) % tasbeehImagesList.size
        Box(Modifier.fillMaxSize()) {
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

            Image(
                painter = painterResource(id = R.drawable.sebhah), contentDescription = null, modifier = Modifier
                    .align(Alignment.Center),
                colorFilter = ColorFilter.tint(MaterialTheme .colorScheme.primary) // 🎨 user chosen color

            )

            Text(
                text = "$tasbeehCounter", textAlign = TextAlign.Center,
                fontWeight = FontWeight(700), fontSize = 34.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 105.dp)
            )

            OutlinedButton(
                onClick = { onTasbeehClick(tasbeehCounter) },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 130.dp)
                    .size(80.dp)
                    .clip(CircleShape)
            ) {}

            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 35.dp, start = 98.dp)
                    .size(25.dp)
                    .clip(CircleShape)
            ) {

            }
        }
    }

}

@Preview
@Composable
private fun TasbeehPagePreview() {
    var tasbeehCounter by remember { mutableStateOf(0) }
    TasbeehPageViews(tasbeehCounter = tasbeehCounter, { tasbeehCounter++ }, { tasbeehCounter = 0 })
}