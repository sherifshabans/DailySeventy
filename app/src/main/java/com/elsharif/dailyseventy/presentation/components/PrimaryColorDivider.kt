package com.elsharif.dailyseventy.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryColorDivider(horizontalPadding: Dp = 20.dp) =
    Divider(
        Modifier
            .fillMaxWidth()
            .height(2.dp)
            .padding(horizontal = horizontalPadding),
        color = MaterialTheme.colorScheme.primary
    )
