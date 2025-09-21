package com.elsharif.dailyseventy.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import com.elsharif.dailyseventy.R

@Composable
fun DecoratedPage(content: @Composable BoxScope.() -> Unit) {

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)

        ) {
            Image(
                painter = painterResource(id = R.drawable.top_left_corner), contentDescription = null,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Image(
                painter = painterResource(id = R.drawable.top_right_corner), contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            Image(
                painter = painterResource(id = R.drawable.bottom_left_corner), contentDescription = null,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            Image(
                painter = painterResource(id = R.drawable.bottom_right_corner), contentDescription = null,
                modifier = Modifier.align(Alignment.BottomEnd)
            )

            content()

        }


    }
}

@Preview
@Composable
private fun DecoratedPagePreview() {
    DecoratedPage {

    }
}