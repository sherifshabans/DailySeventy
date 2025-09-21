package com.elsharif.dailyseventy.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedRow(
    modifier: Modifier = Modifier, alignment: Alignment.Vertical = Alignment.CenterVertically, content:
    @Composable RowScope.() ->
    Unit
) {
    Row(
        verticalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .border(
                2.dp, color = MaterialTheme.colorScheme.primary,
                RoundedCornerShape(4.dp)
            )
            .height(56.dp), content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun OutlinedRowPreview() {
    OutlinedRow {

    }
}